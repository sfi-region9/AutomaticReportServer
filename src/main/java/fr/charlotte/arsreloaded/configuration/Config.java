package fr.charlotte.arsreloaded.configuration;

import com.google.gson.Gson;
import fr.charlotte.arsreloaded.databases.Database;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {


    private final String accessToken;
    private final String adminID;
    private final String secretKey;
    private final String verifyToken;
    private final String dbHost;
    private final String dbName;
    private final String dbUser;
    private final String dbUserName;
    private final String dbPassword;
    private final String smtpHost;
    private final String mailUser;
    private final String mailPassword;
    private final String adminMail;
    private final int mailPort;


    public Config(String accessToken, String adminID, String secretKey, String dbHost, String dbName, String dbUser, String dbUserName, String dbPassword, String verifyToken, String smtpHost, String mailUser, String mailPassword, int mailPort, String adminMail) {
        this.accessToken = accessToken;
        this.adminID = adminID;
        this.secretKey = secretKey;
        this.dbHost = dbHost;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.verifyToken = verifyToken;
        this.mailPassword = mailPassword;
        this.mailUser = mailUser;
        this.mailPort = mailPort;
        this.smtpHost = smtpHost;
        this.adminMail = adminMail;
    }

    public String getAdminMail() {
        return adminMail;
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

    public Mailer buildMailer(){
        return MailerBuilder.withSMTPServer(smtpHost, mailPort, mailUser, mailPassword).withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();
    }

    public String getMailUser() {
        return mailUser;
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
