package fr.colin.arsreloaded;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TextMessage;
import com.google.gson.Gson;
import fr.colin.arsreloaded.commands.HelpCommand;
import fr.colin.arsreloaded.commands.PingCommand;
import fr.colin.arsreloaded.commands.SubscribeCommand;
import fr.colin.arsreloaded.commands.WaitingCommand;
import fr.colin.arsreloaded.objects.Users;
import fr.colin.arsreloaded.utils.Database;
import fr.colin.arsreloaded.utils.DatabaseWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static spark.Spark.*;

public class ARSReloaded {


    public static Messenger messenger = null;
    private static Database db;
    private static DatabaseWrapper wrapper;

    public final static String ACCES_TOKEN = "HereGoTheToken";
    public final static String ADMIN_ID = "HereTheAdminID";
    public final static String TOKEN = "VerifWebToken";
    public final static String SECRET = "SecretApp";

    public static Database getDb() {
        return db;
    }

    public static DatabaseWrapper getWrapper() {
        return wrapper;
    }

    public static void main(String... args) {

        db = new Database("your_host", "dataname", "user", "password");


        // new HelpCommand();
        wrapper = new DatabaseWrapper();
        new HelpCommand();
        new PingCommand();

        new WaitingCommand();
        new SubscribeCommand();
        for (Command c : Command.commands.values()) {
            System.out.println("Registred Command : " + c.getName());
        }
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

    static int count;

    static void increaseCound() {
        count++;
    }

    public static void setupRoutes() {
        get("/", (request, response) -> {
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

        post("/submit", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            getWrapper().saveReport(users);
            return "Save";
        });

        post("/syncronize", (request, response) -> {
            String json = request.body();
            Users users = new Gson().fromJson(json, Users.class);
            return getWrapper().getReport(users);
        });

        get("/send", (request, response) -> getWrapper().sendReports());

        get("/hello", (request, response) -> "Hello World");
    }

    public static void parseCommand(String text, String recipientID) {
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

    public static void sendHelp(String recipientID) {
        ArrayList<String> message = new ArrayList<>();

        if (Command.commands.isEmpty()) {
            sendMessage(recipientID, "No commands are registred, please contact the administrator for any further help.");
            return;
        } else {
            message.add("■ Help for Commands, all commands start from '!' character");
            message.add("■ The [] means an required arg and {} means an optional arg");
            message.add("");
            for (Command c : Command.commands.values()) {
                if (!c.hidden) {
                    message.add("● !" + c.getName() + c.args() + " ⇒ " + c.usage());
                }
            }
        }
        sendMultiMessage(recipientID, "ARS Help", message);
    }

    public static void sendMessage(String recipientID, String message) {
        MessagePayload payload = MessagePayload.create(recipientID, MessagingType.RESPONSE, TextMessage.create(message));
        try {
            messenger.send(payload);
        } catch (MessengerApiException | MessengerIOException e) {
            System.out.println("An error occured with message " + message + " with rid " + recipientID + "\n" + e.getMessage());
        }
    }

    public static void sendMultiMessage(String recipientID, String header, List<String> messages) {
        ArrayList<String> finalM = new ArrayList<>();
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        finalM.addAll(messages);
        finalM.add("");
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        String message = StringUtils.join(finalM, "\n");
        sendMessage(recipientID, message);
    }

    public static boolean isTimeToSent() {

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        long last = getWrapper().getLast();
        int month = cal.get(Calendar.MONTH);

        int detectDay = 0;

        if (month != 2) {
            detectDay = 28;
        } else {
            detectDay = 27;
        }

        if (detectDay == day && (last + 1000 * 60 * 60 * 24 * 10) < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

}
