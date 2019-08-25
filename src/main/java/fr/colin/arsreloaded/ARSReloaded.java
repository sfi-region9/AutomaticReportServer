package fr.colin.arsreloaded;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TextMessage;
import com.google.gson.Gson;
import fr.colin.arsreloaded.commands.*;
import fr.colin.arsreloaded.configuration.Config;
import fr.colin.arsreloaded.configuration.ConfigWrapper;
import fr.colin.arsreloaded.objects.CheckCo;
import fr.colin.arsreloaded.objects.CheckVessel;
import fr.colin.arsreloaded.objects.CheckVesselName;
import fr.colin.arsreloaded.objects.Users;
import fr.colin.arsreloaded.utils.Database;
import fr.colin.arsreloaded.utils.DatabaseUserWrapper;
import fr.colin.arsreloaded.utils.DatabaseWrapper;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.*;

/**
 * Main class of the Messenger Bot and Spark HTTP API
 */
public class ARSReloaded {


    public static Messenger messenger = null;
    private static Database db;
    private static Database userDatabase;
    private static DatabaseUserWrapper wrapperD;
    private static DatabaseWrapper wrapper;

    public static String ADMIN_ID = "";
    private static String TOKEN = "";

    public static SimpleDateFormat DATE = new SimpleDateFormat("MMMM YYYY");
    public static SimpleDateFormat DATE_M = new SimpleDateFormat("MMMM");
    public static SimpleDateFormat DATE_Y = new SimpleDateFormat("YYYY");

    public static Database getDb() {
        return db;
    }

    public static DatabaseWrapper getWrapper() {
        return wrapper;
    }

    public static void main(String... args) {
        //Load configuratbion
        Config cf = new ConfigWrapper().getConfig();
        String ACCES_TOKEN = cf.getACCESS_TOKEN();
        ADMIN_ID = cf.getADMIN_ID();
        String SECRET = cf.getSECRET();
        TOKEN = cf.getTOKEN();
        db = new Database(cf.getDB_HOST(), cf.getDB_NAME(), cf.getDB_USER(), cf.getDB_PASSWORD());
        userDatabase = new Database(cf.getDB_HOST(), cf.getDB_USER_NAME(), cf.getDB_USER(), cf.getDB_PASSWORD());
        wrapper = new DatabaseWrapper();
        wrapperD = new DatabaseUserWrapper();
        //Register commands
        new HelpCommand();
        new PingCommand();

        new WaitingCommand();
        new SubscribeCommand();
        new AboutCommand();
        new DefaultCommand();
        new TemplateCommand();
        new LinkCommand();
        for (Command c : Command.commands.values()) {
            System.out.println("Registred Command : " + c.getName());
        }

        //Build messenger
        try {
            messenger = Messenger.create(ACCES_TOKEN, SECRET, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        port(5555);
        setupRoutes();

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
            Users users = new Gson().fromJson(json, Users.class);
            getWrapper().register(users);
            return "User successfully uploaded";
        });

        post("/destroy_user", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            getWrapper().destroyUser(users);
            return "user successfully removed";
        });

        post("/submit", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            getWrapper().saveReport(users);
            return "Save";
        });

        get("/verify_token",(request, response) -> {
            if(request.queryParams().isEmpty())
                return "Error please send params";
            if(request.queryParams().size() < 2)
                return "Error please send params";
            String scc = request.queryParams("scc");
            String token = request.queryParams("token");
            return getWrapper().verifyToken(scc, token);
        });

        post("/check_co", (request, response) -> {
            String json = request.body();
            CheckCo co = new Gson().fromJson(json, CheckCo.class);
            return co.process();
        });

        post("/update_template", (request, response) -> {
            String json = request.body();
            CheckVessel v = new Gson().fromJson(json, CheckVessel.class);
            return v.update();
        });

        post("/switch_vessel", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            return getWrapper().switchVessel(users, users.getVesselid());
        });

        get("/send", (request, response) -> {
            if (request.queryParams().isEmpty())
                return false;
            if (request.queryParams("password").contains("accessgranted"))
                new DatabaseWrapper().sendReports();
            return "congratulations";
        });

        post("/update_name", (request, response) -> {
            String json = request.body();
            CheckVesselName ns = new Gson().fromJson(json, CheckVesselName.class);
            return ns.update();
        });

        post("/syncronize", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            return getWrapper().getReport(users);
        });

        get("/allvessels", (request, response) -> new Gson().toJson(getWrapper().allVessels()));


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
        String command = text.substring(1).split(" ")[0];
        String[] args = Arrays.copyOfRange(text.substring(1).split(" "), 1, text.substring(1).split(" ").length);
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
                if (!c.hidden) {
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

    public static DatabaseUserWrapper getWrapperD() {
        return wrapperD;
    }
}
