package fr.charlotte.arsreloaded.configuration;

import com.google.gson.Gson;
import fr.charlotte.arsreloaded.databases.Database;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {


    private String accessToken;
    private String adminID;
    private String secretKey;
    private String verifyToken;
    private String dbHost;
    private String dbName;
    private String dbUser;
    private String dbUserName;
    private String dbPassword;


    public Config(String accessToken, String adminID, String secretKey, String dbHost, String dbName, String dbUser, String dbUserName, String dbPassword, String verifyToken) {
        this.accessToken = accessToken;
        this.adminID = adminID;
        this.secretKey = secretKey;
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.verifyToken = verifyToken;
    }

    public Database setupMainDatabase() {
        return new Database(dbHost, dbName, dbUser, dbPassword);
    }

    public Database setupUserDatabase() {
        return new Database(dbHost, dbUserName, dbUser, dbPassword);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAdminID() {
        return adminID;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public static Config loadConfiguration() throws IllegalAccessException {
        InputStream inputStream = Config.class.getResourceAsStream("/config.json");
        if (inputStream == null)
            throw new IllegalAccessException("Please provide a configuration file");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String rawConfig = StringUtils.join(reader.lines().toArray());
        return new Gson().fromJson(rawConfig, Config.class);
    }

}
