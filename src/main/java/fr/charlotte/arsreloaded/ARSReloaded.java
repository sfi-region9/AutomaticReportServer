package fr.charlotte.arsreloaded;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TextMessage;
import com.google.gson.Gson;

import fr.charlotte.arsreloaded.configuration.Config;
import fr.charlotte.arsreloaded.databases.Database;
import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.plugins.ProcessAllReports;
import fr.charlotte.arsreloaded.plugins.ReportProcessing;
import fr.charlotte.arsreloaded.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.*;

/**
 * Main class of the Messenger Bot and Spark HTTP API
 */
public class ARSReloaded {


    private static Messenger messenger = null;
    private static Database userDatabase;
    private static Database arsDatabase;
    private static DatabaseUserWrapper wrapperD;
    private static DatabaseWrapper wrapper;

    public static HashMap<Integer, Integer> vesselByIdCache = null;
    public static ArrayList<Vessel> vesselsCache = null;

    public static String ADMIN_ID = "";
    private static String TOKEN = "";
    private static String ACCESS_TOKEN = "";
    private static String SECRET = "";

    public static HashMap<String, ReportProcessing> processingHashMap = new HashMap<>();
    public static HashMap<String, ProcessAllReports> processing = new HashMap<>();
    private static TreeMap<String, Integer> trackedReports = new TreeMap<>();

    public static SimpleDateFormat DATE = new SimpleDateFormat("MM/YYYY");
    public static SimpleDateFormat DATE_M = new SimpleDateFormat("MM");
    public static SimpleDateFormat DATE_Y = new SimpleDateFormat("YYYY");


    private final static Gson GSON = new Gson();

    public static PluginManager plugins;

    public static DatabaseWrapper getWrapper() {
        return wrapper;
    }

    public static void main(String... args) throws InterruptedException, SQLException {
        loadARS();
        trackedReports = getWrapper().getTrackedReports();
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

        arsDatabase = config.setupMainDatabase();
        userDatabase = config.setupUserDatabase();

        wrapper = new DatabaseWrapper(arsDatabase);
        wrapperD = new DatabaseUserWrapper(userDatabase);
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
            if (!getWrapper().isCo(processing.getVesselID(), processing.getID()))
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
                if (!getWrapper().isCo(reports.getVesselID(), reports.getID()))
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
        System.out.println("Welcome in ARS v1.7");
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
        loadConfig();
        loadPlugins();
        loadSpark();


        Thread verifier = new Thread(new AutoSender());
        verifier.start();
    }

    /**
     * Setup all the route in a separated method
     */
    private static void setupRoutes() {
        get("/", (request, response) -> {
            if (request.queryParams().isEmpty())
                return "Invalid";
            String sentToken = request.queryParams("hub.verify_token");
            if (sentToken.equalsIgnoreCase(TOKEN))
                return request.queryParams("hub.challenge");
            return "Invalid";
        });

        get("/vessel_by_regions", (request, response) -> {
            if (vesselByIdCache == null)
                return GSON.toJson(getWrapper().getVesselByRegions());
            return GSON.toJson(vesselByIdCache);
        });

        get("/reports_by_date", (request, response) -> GSON.toJson(trackedReports));

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
        post("/register_user", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            getWrapper().register(users);
            return "User successfully uploaded";
        });

        post("/destroy_user", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return getWrapper().destroyUser(users);
        });

        post("/submit", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return getWrapper().saveReport(users);
        });

        get("/verify_token", (request, response) -> {
            if (request.queryParams().isEmpty())
                return "Error please send params";
            if (request.queryParams().size() < 2)
                return "Error please send params";
            String scc = request.queryParams("scc");
            String token = request.queryParams("token");
            return getWrapper().verifyToken(scc, token);
        });

        post("/check_co", (request, response) -> {
            String json = request.body();
            CheckCo co = GSON.fromJson(json, CheckCo.class);
            return co.process();
        });

        post("/update_template", (request, response) -> {
            String json = request.body();
            CheckVessel v = GSON.fromJson(json, CheckVessel.class);
            return v.update();
        });

        post("/switch_vessel", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return getWrapper().switchVessel(users, users.getVesselID());
        });

        get("/send", (request, response) -> {
            if (request.queryParams().isEmpty())
                return false;
            if (request.queryParams("password").contains("accessgranted"))
                getWrapper().sendReports();
            return "congratulations";
        });

        post("/update_name", (request, response) -> {
            String json = request.body();
            CheckVesselName ns = GSON.fromJson(json, CheckVesselName.class);
            return ns.update();
        });

        post("/synchronize", (request, response) -> {
            String json = request.body();
            Users users = GSON.fromJson(json, Users.class);
            return getWrapper().getReport(users);
        });

        post("/synchronize_user", (request, response) -> {
            String json = request.body();
            Users s = GSON.fromJson(json, Users.class);
            return GSON.toJson(getWrapper().synchronizeUser(s));
        });

        get("/allvessels", (request, response) -> {
            if (vesselsCache != null)
                return GSON.toJson(vesselsCache);
            return GSON.toJson(getWrapper().getAllVessels());
        });


        get("/send", (request, response) -> getWrapper().sendReports());

        get("/hello", (request, response) -> "Hello World");
    }


    /**
     * Simple method to parse commands sent by messenger to the server
     *
     * @param text        Raw text receive from messenger
     * @param recipientID Messenger ID of the sender of the command
     */
    private static void parseCommand(String text, String recipientID) {
        if (!text.startsWith("!")) {
            sendHelp(recipientID);
            return;
        }
        String[] rawCommand = text.substring(1).split(" ");
        String command = rawCommand[0];
        String[] args = Arrays.copyOfRange(rawCommand, 1, rawCommand.length);
        if (!Command.commands.containsKey(command) && !Command.alias.containsKey(command)) {
            sendHelp(recipientID);
            return;
        }
        Command c;
        if (Command.commands.containsKey(command))
            c = Command.commands.get(command);
        else
            c = Command.alias.get(command);
        c.onCommand(recipientID, text, args);
    }

    /**
     * Method to send the help ( all commands and their usage ) to a user.
     *
     * @param recipientID ID of the recipient.
     */
    public static void sendHelp(String recipientID) {
        ArrayList<String> message = new ArrayList<>();

        if (Command.commands.isEmpty()) {
            sendMessage(recipientID, "No commands are registred, please contact the administrator for any further help.");
            return;
        } else {
            message.add("■ Help for Commands, all commands start with '!' character");
            message.add("■ [] => required argument\n" +
                    "■ {} => optional argument");
            message.add("");
            for (Command c : Command.commands.values()) {
                if (!c.isHidden()) {
                    message.add("● !" + c.getName() + c.args() + " ⇒ " + c.usage());
                }
            }
        }
        sendMultiMessage(recipientID, "ARS Help", message);
    }

    /**
     * Method to send a messenger message to a person
     *
     * @param recipientID ID of the recipient of the message
     * @param message     The message
     */
    public static void sendMessage(String recipientID, String message) {
        MessagePayload payload = MessagePayload.create(recipientID, MessagingType.RESPONSE, TextMessage.create(message));
        try {
            messenger.send(payload);
        } catch (MessengerApiException | MessengerIOException e) {
            System.out.println("An error occured with message " + message + " with rid " + recipientID + "\n" + e.getMessage());
        }
    }

    /**
     * Method to send an organized message in multi line
     *
     * @param recipientID ID of the recipient of the message
     * @param header      Header in the upper and lower bars
     * @param messages    The message
     */
    public static void sendMultiMessage(String recipientID, String header, List<String> messages) {
        ArrayList<String> finalM = new ArrayList<>();
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        finalM.addAll(messages);
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        String message = StringUtils.join(finalM, "\n");
        sendMessage(recipientID, message);
    }

    public static Database getUserDatabase() {
        return userDatabase;
    }

    public static Database getArsDatabase() {
        return arsDatabase;
    }

    public static DatabaseUserWrapper getWrapperD() {
        return wrapperD;
    }
}
