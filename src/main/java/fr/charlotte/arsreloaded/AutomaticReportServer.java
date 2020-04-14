package fr.charlotte.arsreloaded;

import com.github.messenger4j.Messenger;
import com.google.gson.Gson;
import fr.charlotte.arsreloaded.configuration.Config;
import fr.charlotte.arsreloaded.databases.Database;
import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.plugins.ProcessAllReports;
import fr.charlotte.arsreloaded.plugins.ReportProcessing;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import fr.charlotte.arsreloaded.utils.Users;
import fr.charlotte.arsreloaded.verifiers.implementations.CommandingOfficerVerifier;
import fr.charlotte.arsreloaded.verifiers.implementations.VesselNameVerifier;
import fr.charlotte.arsreloaded.verifiers.implementations.VesselTemplateVerifier;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.simplejavamail.mailer.Mailer;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.*;

/**
 * Main class of the Messenger Bot and Spark HTTP API
 */
public class AutomaticReportServer {


    private static Messenger messenger = null;
    private static Database userDatabase;
    private static Database arsDatabase;
    private static DatabaseWrapper wrapper;


    private static String ADMIN_ID = "";
    private static String ADMIN_MAIL = "";
    private static String TOKEN = "";
    private static String ACCESS_TOKEN = "";
    private static String SECRET = "";
    private static String ARSMAIL = "";

    private static final HashMap<String, ReportProcessing> processingHashMap = new HashMap<>();
    private static final HashMap<String, ProcessAllReports> processing = new HashMap<>();
    private static TreeMap<String, Integer> trackedReports = new TreeMap<>();

    public static SimpleDateFormat DATE = new SimpleDateFormat("MM/yyyy");
    public static SimpleDateFormat DATE_M = new SimpleDateFormat("MM");
    public static SimpleDateFormat DATE_Y = new SimpleDateFormat("yyyy");

    private static Mailer mailer;


    private final static Gson GSON = new Gson();

    private static PluginManager plugins;

    public static void main(String... args) throws InterruptedException, SQLException {
        loadARS();
        trackedReports = wrapper.getTrackedReports();
    }

    private static void loadConfig() {
        Config config = null;
        try {
            config = Config.loadConfiguration();
        } catch (IllegalAccessException e) {
            System.exit(0);
        }
        if (config == null)
            System.exit(0);

        ACCESS_TOKEN = config.getAccessToken();
        SECRET = config.getSecretKey();
        ADMIN_ID = config.getAdminID();
        TOKEN = config.getVerifyToken();
        ARSMAIL = config.getMailUser();
        ADMIN_MAIL = config.getAdminMail();

        arsDatabase = config.setupMainDatabase();
        userDatabase = config.setupUserDatabase();

        wrapper = new DatabaseWrapper(arsDatabase, processingHashMap, processing, new MessengerUtils(messenger, mailer, ARSMAIL, ADMIN_MAIL));
        mailer = config.buildMailer();
    }


    private static void loadPlugins() throws SQLException {
        processingHashMap.clear();
        File file = new File("plugins/");
        if (!file.exists())
            file.mkdir();
        System.out.println(file.getAbsolutePath());
        plugins = new JarPluginManager(file.toPath());
        plugins.loadPlugins();
        plugins.startPlugins();

        for (ReportProcessing processing : plugins.getExtensions(ReportProcessing.class)) {
            if (!wrapper.isCo(processing.getVesselID(), processing.getID()))
                continue;
            if (processingHashMap.containsKey(processing.getVesselID()))
                continue;
            processingHashMap.put(processing.getVesselID(), processing);
            System.out.println(processing.getVesselID() + " User report plugin was added to the server");
        }

        plugins.getExtensions(Command.class).forEach(command -> {
            command.register();
            System.out.println("Register command : " + command.getName() + " from " + command.getClass().getName());
        });


        try {
            for (ProcessAllReports reports : plugins.getExtensions(ProcessAllReports.class)) {
                if (!wrapper.isCo(reports.getVesselID(), reports.getID()))
                    continue;
                if (processing.containsKey(reports.getVesselID()))
                    continue;
                processing.put(reports.getVesselID(), reports);
                System.out.println(reports.getVesselID() + " Post user report processing plugin was added");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void loadSpark() throws InterruptedException {
        Thread.sleep(100);
        System.out.println("   ");
        String ARS_VERSION = "v3.0";
        System.out.println("Welcome in ARS " + ARS_VERSION);
        Locale.setDefault(Locale.FRANCE);
        System.out.println("Start Time : " + new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(new Date(System.currentTimeMillis())));
        System.out.println("   ");
        Thread.sleep(100);
        //Build messenger
        try {
            messenger = Messenger.create(ACCESS_TOKEN, SECRET, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ipAddress("127.0.0.1");
        port(5555);
        setupRoutes();
    }

    private static void loadARS() throws InterruptedException, SQLException {

        loadPlugins();
        loadSpark();
        loadConfig();
        wrapper.getAllVessels();

        Thread verifier = new Thread(new AutoSender(wrapper));
        verifier.start();
    }

    /**
     * Setup all the route in a separated method
     */

    private static void setupRootRoute() {
        get("/", (request, response) -> {
            if (request.queryParams().isEmpty())
                return "Invalid";
            String sentToken = request.queryParams("hub.verify_token");
            if (sentToken == null)
                return "Invalid";
            if (sentToken.equalsIgnoreCase(TOKEN))
                return request.queryParams("hub.challenge");
            return "Invalid";
        });
        post("/", (request, response) -> {
            String payload = request.body();
            messenger.onReceiveEvents(payload, java.util.Optional.ofNullable(request.headers(Messenger.SIGNATURE_HEADER_NAME)), event -> {
                if (event.isTextMessageEvent()) {
                    String text = event.asTextMessageEvent().text();
                    parseCommand(text, event.senderId());
                }
            });
            return "H";
        });
    }

    private static void setupMetricsRoutes() {
        get("/vessel_by_regions", (request, response) -> {
            response.type("application/json");
            if (wrapper.getVesselByIdCache() == null)
                return GSON.toJson(wrapper.getVesselByRegions());
            return GSON.toJson(wrapper.getVesselByIdCache());
        });

        get("/allvessels", (request, response) -> {
            response.type("application/json");
            if (wrapper.getVesselsCache() != null)
                return GSON.toJson(wrapper.getVesselsCache());
            return GSON.toJson(wrapper.getAllVessels());
        });

        get("/reports_by_date", (request, response) -> {
            response.type("application/json");
            return GSON.toJson(trackedReports);
        });
    }

    private static void setupUserRoutes() {
        post("/register_user", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            wrapper.register(users);
            return "User successfully uploaded";
        });

        post("/destroy_user", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return wrapper.destroyUser(users);
        });
    }

    private static void setupVerificationsRoutes() {
        get("/verify_token", (request, response) -> {
            if (request.queryParams().isEmpty())
                return "Error please send params";
            if (request.queryParams().size() < 2)
                return "Error please send params";
            String scc = request.queryParams("scc");
            String token = request.queryParams("token");
            return wrapper.verifyToken(scc, token);
        });

        post("/check_co", (request, response) -> {
            String json = request.body();
            CommandingOfficerVerifier co = GSON.fromJson(json, CommandingOfficerVerifier.class);
            return co.update();
        });
    }

    private static void setupSynchronizedRoutes() {
        post("/synchronize", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return wrapper.getReport(users);
        });

        post("/synchronize_user", (request, response) -> {
            response.type("application/json");
            String json = request.body();
            Users s = GSON.fromJson(json, Users.class);
            return GSON.toJson(wrapper.synchronizeUser(s));
        });
    }

    private static void setupSubmitRoutes() {
        post("/submit", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return wrapper.saveReport(users);
        });
        post("/update_template", (request, response) -> {
            String json = request.body();
            VesselTemplateVerifier v = GSON.fromJson(json, VesselTemplateVerifier.class);
            return v.update();
        });
    }

    private static void setupUpdateRoute() {
        post("/switch_vessel", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return wrapper.switchVessel(users, users.getVesselID());
        });

        post("/update_name", (request, response) -> {
            String json = request.body();
            VesselNameVerifier ns = GSON.fromJson(json, VesselNameVerifier.class);
            System.out.println(ns.update());
            return ns.update();
        });
    }

    private static void setupRoutes() {
        setupRootRoute();
        setupMetricsRoutes();
        setupUserRoutes();
        setupVerificationsRoutes();
        setupSynchronizedRoutes();
        setupSubmitRoutes();
        setupUpdateRoute();

        get("/send", (request, response) -> wrapper.sendReports());
        get("/hello", (request, response) -> "Hello World");
    }


    /**
     * Simple method to parse commands sent by messenger to the server
     *
     * @param text        Raw text receive from messenger
     * @param recipientID Messenger ID of the sender of the command
     */
    private static void parseCommand(String text, String recipientID) {
        MessengerUtils utils = new MessengerUtils(messenger, mailer, ARSMAIL, ADMIN_MAIL);
        if (!text.startsWith("&")) {
            utils.sendHelp(recipientID);
            return;
        }
        String[] rawCommand = text.substring(1).split(" ");
        String command = rawCommand[0];
        String[] args = Arrays.copyOfRange(rawCommand, 1, rawCommand.length);
        if (!Command.commands.containsKey(command) && !Command.alias.containsKey(command)) {
            utils.sendHelp(recipientID);
            return;
        }
        Command c;
        if (Command.commands.containsKey(command))
            c = Command.commands.get(command);
        else
            c = Command.alias.get(command);
        c.execute(recipientID, text, args, wrapper, utils, new DatabaseUserWrapper(userDatabase), ADMIN_ID);
    }

}
