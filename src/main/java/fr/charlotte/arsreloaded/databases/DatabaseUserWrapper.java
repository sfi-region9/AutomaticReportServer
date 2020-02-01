package fr.charlotte.arsreloaded.databases;


import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUserWrapper {

    private Database arsUserDatabase;

    public DatabaseUserWrapper(Database arsUserDatabase) {
        this.arsUserDatabase = arsUserDatabase;
    }

    private boolean exist(String username) {
        ResultSet rs = arsUserDatabase.getResult("SELECT * FROM users WHERE username='" + username + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean match(String username, String uuid) {
        if (!exist(username))
            return false;
        ResultSet rs = arsUserDatabase.getResult("SELECT * FROM users WHERE username='" + username + "' AND uuid='" + uuid + "'");
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
        arsUserDatabase.update("UPDATE users SET messengerid ='" + id + "' WHERE username='" + username + "'");
        return true;
    }

}
