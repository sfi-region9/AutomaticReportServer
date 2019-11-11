package fr.charlotte.arsreloaded.configuration;

import org.apache.commons.lang3.StringUtils;

public class Config {

    private String ACCESS_TOKEN;
    private String ADMIN_ID;
    private String SECRET;
    private String TOKEN;
    private String DB_HOST;
    private String DB_NAME;
    private String DB_USER_NAME;
    private String DB_USER;

    public String getACCESS_TOKEN() {
        return ACCESS_TOKEN;
    }

    public String getADMIN_ID() {
        return ADMIN_ID;
    }

    public String getSECRET() {
        return SECRET;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public String getDB_HOST() {
        return DB_HOST;
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public String getDB_USER() {
        return DB_USER;
    }

    public String getDB_PASSWORD() {
        return DB_PASSWORD;
    }

    public String getDB_USER_NAME() {
        return DB_USER_NAME;
    }

    private String DB_PASSWORD;

    public Config(String ACCESS_TOKEN, String ADMIN_ID, String SECRET, String TOKEN, String DB_HOST, String DB_NAME, String DB_USER, String DB_PASSWORD, String DB_USER_NAME) {
        this.ACCESS_TOKEN = ACCESS_TOKEN;
        this.ADMIN_ID = ADMIN_ID;
        this.SECRET = SECRET;
        this.TOKEN = TOKEN;
        this.DB_HOST = DB_HOST;
        this.DB_NAME = DB_NAME;
        this.DB_USER = DB_USER;
        this.DB_PASSWORD = DB_PASSWORD;
        this.DB_USER_NAME =DB_USER_NAME;
    }
}
