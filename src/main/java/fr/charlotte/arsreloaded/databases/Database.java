package fr.charlotte.arsreloaded.databases;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo
 */
public class Database {

    private Connection connection;

    private String host;
    private String database;
    private String user;
    private String pass;

    private void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + database, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error");
        }
    }

    /**
     * Create a database instance and connect
     *
     * @param host     Host IP
     * @param database Database name
     * @param user     Database user
     * @param pass     User password
     */
    public Database(String host, String database, String user, String pass) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.pass = pass;
    }

    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Returns a value in the database
     *
     * @param query MySQL writted request
     * @param get   Field to get
     * @return
     */
    public Object read(String query, String get) {
        Object request = null;

        while (connection == null)
            connect();

        try {
            PreparedStatement sts = this.connection.prepareStatement(query);
            ResultSet result = sts.executeQuery();
            while (result.next())
                request = result.getObject(get);
            sts.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return request;
    }

    /**
     * Returns a list of values in the database
     *
     * @param query MySQL writted request
     * @param get   Field to get
     * @return
     */
    public List<Object> readList(String query, String get) {
        List<Object> request = new ArrayList();

        try {
            PreparedStatement sts = this.connection.prepareStatement(query);
            ResultSet result = sts.executeQuery();
            while (result.next())
                request.add(result.getObject(get));
            sts.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return request;
    }

    /**
     * Update or Remove or Set a row in database
     *
     * @param query MySQL writted request
     */
    public void update(String query) {

        while (connection == null)
            connect();

        try {
            PreparedStatement sts = this.connection.prepareStatement(query);
            sts.executeUpdate();
            sts.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection = null;
    }

    /**
     * @param query MySQL writted request
     * @return
     */
    public ResultSet getResult(String query) {

        while (connection == null)
            connect();

        Object request = null;
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            return pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (ResultSet) request;
    }

}