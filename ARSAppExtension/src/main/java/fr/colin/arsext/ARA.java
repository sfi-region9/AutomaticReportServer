package fr.colin.arsext;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sun.org.apache.bcel.internal.generic.FALOAD;
import fr.colin.arsext.utils.Database;
import fr.colin.arsext.utils.DatabaseWrapper;
import fr.colin.arssdk.ARSdk;
import fr.colin.arssdk.objects.User;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ARA extends Thread {

    private static ServerSocket server;
    private static Database database;
    private static ARSdk sdk;
    private static DatabaseWrapper wrapper;
    private String RAW_MESSAGE;
    public Socket socket;

    public static void main(String[] args) throws IOException {
        database = new Database("pma.nwa2coco.fr", "uvrim_web", "dev", "GABYUMUF9zBr1Ln2");
        wrapper = new DatabaseWrapper(database);
        sdk = ARSdk.DEFAULT_INSTANCE;


        try {
            server = new ServerSocket(12345);
        } catch (IOException e) {
            System.out.println("Error");
            return;
        }

        System.out.println("Lancement du servueur");

        while (true) {
            Socket client = server.accept();
            ARA sd = new ARA(client);
            sd.start();
        }


    }


    public static Database getDatabase() {
        return database;
    }

    public static DatabaseWrapper getWrapper() {
        return wrapper;
    }

    public ARA(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        traitements();
    }

    public void traitements() {
        //TODO : MessageTraitement
        try {

            String rawMessage = readFromInputStream(socket.getInputStream());
            RAW_MESSAGE = rawMessage;
            String answer = "";
            if (rawMessage.startsWith("REG")) {
                answer = processRegister();
            } else if (rawMessage.startsWith("LOG")) {
                answer = processLogin();
            } else if (rawMessage.startsWith("HELLO")) {
                answer = "Hello World";
            }
            socket.getOutputStream().write(answer.getBytes());
            socket.getOutputStream().flush();
            socket.getOutputStream().close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String processLogin() throws SQLException {
        System.out.println("Received login of " + socket.getInetAddress().getHostAddress());
        String[] args = RAW_MESSAGE.split("}_}");
        if (args.length < 3) {
            return "Error while login, there is 3 required arguments !(|=(}Error";
        }
        String user = args[1];
        String password = args[2];
        String[] log = login(user, password);
        if (Boolean.parseBoolean(log[0])) {
            System.out.println("User " + socket.getInetAddress().getHostAddress() + " successfully login");
            //TODO : RETURN ALL THE VALUES
            return log[1];

        } else {
            System.out.println("Error while login " + socket.getInetAddress().getHostAddress());
            return "Error while login, please try again or on the website https://reports.nwa2coco.fr " + log[1];
        }
    }

    private String[] login(String user, String password) throws SQLException {
        ResultSet rs = database.getResult("SELECT * FROM users WHERE username='" + user + "'");
        if (!rs.next())
            return new String[]{"false", "Unknow username"};
        String pass = (String) database.read("SELECT * FROM users WHERE username='" + user + "'", "password");
        if (BCrypt.verifyer().verify(password.toCharArray(), pass.toCharArray()).verified) {
            String username = rs.getString("username");
            String scc = rs.getString("scc");
            String vesselid = rs.getString("vesselid");
            String name = rs.getString("name");
            String messengerid = rs.getString("messengerid");
            String uuid = rs.getString("uuid");
            String[] s = {username, scc, vesselid, name, messengerid, uuid};
            return new String[]{"true", StringUtils.join(s, "}_}")};
        } else {
            return new String[]{"false", "bad password"};
        }
    }

    public String processRegister() throws IOException, SQLException {
        System.out.println("Received registration of " + socket.getInetAddress().getHostAddress());
        String[] args = RAW_MESSAGE.split("}_}");
        if (args.length < 7) {
            return "Error while register, there is 7 required arguments";
        }
        String name = args[1];
        String user = args[2];
        String password = args[3];
        String vaisseau = args[4];
        String email = args[5];
        String scc = args[6];
        String[] sd = register(name, user, password, vaisseau, email, scc);
        if (Boolean.parseBoolean(sd[0])) {
            sdk.registerUser(new User(name, scc, vaisseau, ""));
            System.out.println("User " + socket.getInetAddress().getHostAddress() + " succesfully registred.");
            return "You are succesfully registred in the database :) !, You can now login with the app or the website";
        } else {
            System.out.println("Error while register, " + socket.getInetAddress().getHostAddress());
            return "Error while register, " + sd[0];
        }

    }


    public static String readFromInputStream(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(r);
        return reader.readLine();
    }

    public String[] register(String name, String user, String password, String vessel, String email, String scc) throws SQLException {

        if (name.length() < 4)
            return new String[]{"false", "Name too short"};
        if (user.length() < 4)
            return new String[]{"false", "User too short"};

        ResultSet rs = database.getResult("SELECT * FROM users WHERE username='" + user + "'");
        if (rs.next()) {
            return new String[]{"false", "Username already exist"};
        }
        rs.close();

        ResultSet sd = database.getResult("SELECT * FROM users WHERE mail='" + email + "'");
        if (sd.next())
            return new String[]{"false", "email already exist"};
        sd.close();

        ResultSet sdf = database.getResult("SELECT * FROM users WHERE scc='" + scc + "'");
        if (sdf.next())
            return new String[]{"false", "scc already exist"};
        sd.close();

        database.update(String.format("INSERT INTO users(username,scc,vesselid,name,mail,password,uuid,messengerid) VALUES('%s','%s','%s','%s','%s','%s','%s','undefined')", user, scc, vessel, name, email, password, UUID.randomUUID().toString()));

        return new String[]{"true", "Success !"};
    }

    public static BufferedReader readerFromInput(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

}
