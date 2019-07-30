package fr.colin.arsreloaded.utils;

import fr.colin.arsreloaded.ARSReloaded;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUserWrapper {

    Database db = ARSReloaded.getUserDatabase();

    public DatabaseUserWrapper() {

    }

    public boolean exist(String username) {
        ResultSet rs = db.getResult("SELECT * FROM users WHERE username='" + username + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean match(String username, String uuid) {
        if (!exist(username))
            return false;
        ResultSet rs = db.getResult("SELECT * FROM users WHERE username='" + username + "' AND uuid='" + uuid + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateId(String username, String uuid, String id) {
        if (!match(username, uuid))
            return false;
        db.update("UPDATE users SET messengerid ='" + id + "' WHERE username='" + username + "'");
        return true;
    }

}
