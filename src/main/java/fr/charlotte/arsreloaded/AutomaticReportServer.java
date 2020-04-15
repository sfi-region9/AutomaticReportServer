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
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Main class of the Messenger Bot and Spark HTTP API
 */
public class AutomaticReportServer {


    private static Messenger messenger = null;
    private static Database userDatabase;
    private static DatabaseWrapper wrapper;


    private static String adminID = "";
    private static String adminMail = "";
    private static String token = "";
    private static String accessToken = "";
    private static String secret = "";
    private static String arsMail = "";

    private static final HashMap<String, ReportProcessing> processingHashMap = new HashMap<>();
    private static final HashMap<String, ProcessAllReports> processing = new HashMap<>();
    private static TreeMap<String, Integer> trackedReports = new TreeMap<>();

    public static final SimpleDateFormat date = new SimpleDateFormat("MM/yyyy");
    public static final SimpleDateFormat dateMonth = new SimpleDateFormat("MM");
    public static final SimpleDateFormat dateYear = new SimpleDateFormat("yyyy");

    private static final String JSON = "application/json";
    private static Mailer mailer;

    private static final Logger logger = Logger.getLogger("Automatic Report Server");

    public static Logger getLogger() {
        return logger;
    }

    private static final Gson GSON = new Gson();

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

        accessToken = config.getAccessToken();
        secret = config.getSecretKey();
        adminID = config.getAdminID();
        token = config.getVerifyToken();
        arsMail = config.getMailUser();
        adminMail = config.getAdminMail();

        Database arsDatabase = config.setupMainDatabase();
        userDatabase = config.setupUserDatabase();

        wrapper = new DatabaseWrapper(arsDatabase, processingHashMap, processing, new MessengerUtils(messenger, mailer, arsMail, adminMail));
        mailer = config.buildMailer();
    }


    private static void loadPlugins() throws SQLException {
        processingHashMap.clear();
        File file = new File("plugins/");
        if (!file.exists())
            file.mkdir();
        logger.log(Level.INFO, file.getAbsolutePath());
        PluginManager plugins = new JarPluginManager(file.toPath());
        plugins.loadPlugins();
        plugins.startPlugins();

        for (ReportProcessing processing : plugins.getExtensions(ReportProcessing.class)) {
            if (!wrapper.isCo(processing.getVesselID(), processing.getID()) || processingHashMap.containsKey(processing.getVesselID()))
                continue;
            processingHashMap.put(processing.getVesselID(), processing);
            logger.log(Level.INFO, processing.getVesselID() + " User report plugin was added to the server");
        }

        plugins.getExtensions(Command.class).forEach(command -> {
            command.register();
            logger.log(Level.INFO, "Register command : " + command.getName() + " from " + command.getClass().getName());
        });


        try {
            for (ProcessAllReports reports : plugins.getExtensions(ProcessAllReports.class)) {
                if (!wrapper.isCo(reports.getVesselID(), reports.getID()) || processing.containsKey(reports.getVesselID()))
                    continue;
                processing.put(reports.getVesselID(), reports);
                logger.log(Level.INFO, reports.getVesselID() + " Post user report processing plugin was added");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }


    }

    private static void loadSpark() throws InterruptedException {
        Thread.sleep(100);
        logger.log(Level.INFO, "   ");
        logger.log(Level.INFO, "Welcome in ARS v3.0");
        Locale.setDefault(Locale.FRANCE);
        logger.log(Level.INFO, "Start Time : {0}", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(System.currentTimeMillis())));
        logger.log(Level.INFO, "   ");
        Thread.sleep(100);
        //Build messenger
        try {
            messenger = Messenger.create(accessToken, secret, token);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
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
        String invalid = "Invalid";
        get("/", (request, response) -> {
            if (request.queryParams().isEmpty())
                return invalid;
            String sentToken = request.queryParams("hub.verify_token");
            if (sentToken == null)
                return invalid;
            if (sentToken.equalsIgnoreCase(token))
                return request.queryParams("hub.challenge");
            return invalid;
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
            response.type(JSON);
            if (wrapper.getVesselByIdCache() == null)
                return GSON.toJson(wrapper.getVesselByRegions());
            return GSON.toJson(wrapper.getVesselByIdCache());
        });

        get("/allvessels", (request, response) -> {
            response.type(JSON);
            if (wrapper.getVesselsCache() != null)
                return GSON.toJson(wrapper.getVesselsCache());
            return GSON.toJson(wrapper.getAllVessels());
        });

        get("/reports_by_date", (request, response) -> {
            response.type(JSON);
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
            response.type(JSON);
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
            logger.log(Level.INFO, "{0}", ns.update());
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
        MessengerUtils utils = new MessengerUtils(messenger, mailer, arsMail, adminMail);
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
        c.execute(recipientID, text, args, wrapper, utils, new DatabaseUserWrapper(userDatabase), adminID);
    }

}
