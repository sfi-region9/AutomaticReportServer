package fr.charlotte.arsreloaded.databases;

import fr.charlotte.arsreloaded.ARSReloaded;
import fr.charlotte.arsreloaded.plugins.ProcessAllReports;
import fr.charlotte.arsreloaded.plugins.ReportProcessing;
import fr.charlotte.arsreloaded.utils.Users;
import fr.charlotte.arsreloaded.utils.Vessel;
import fr.charlotte.arsreloaded.utils.VesselNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class to wrap the database into easy-to-use methods
 */

public class DatabaseWrapper {

    private static SimpleDateFormat ukDateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.UK);
    private Database arsDatabase;

    /**
     * Main constructor for the Wrapper of the Database interactions
     *
     * @param arsDatabase The Database
     */
    public DatabaseWrapper(Database arsDatabase) {
        this.arsDatabase = arsDatabase;
    }

    /**
     * Method to get all pending vessels in order to accept/deny them
     *
     * @return An ArrayList with all the pending vessel ( name,co and id )
     */
    public ArrayList<String> getPendingWaiting() throws SQLException {
        ArrayList<String> a = new ArrayList<>();
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `waiting`");
        a.add("");
        while (rs.next()) {
            a.add("● " + rs.getString("vesselname") + " CO : " + rs.getString("COID") + " ID : " + rs.getString("vesselid"));
        }
        if (a.size() == 1) {
            a.clear();
        }
        rs.close();
        arsDatabase.closeConnection();
        return a;
    }

    /**
     * Method to check if a co exist (when a co apply to register his chapter)
     *
     * @param senderID The ID of the CO
     * @return if the co exist or not in the database
     **/
    private boolean coExist(String senderID) throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `vessels` WHERE coid = '" + senderID + "'");
        if (rs.next())
            return true;
        else {
            ResultSet s = arsDatabase.getResult("SELECT * FROM `waiting` WHERE coid='" + senderID + "'");
            if (s.next())
                return true;
        }
        return false;
    }

    /**
     * Method to register a new vessel in the waiting table
     *
     * @param senderID The ID of the CO
     * @param vessel   The name of the Vessel
     * @param scc      The SCC of the CO
     * @return if the register is successful
     */
    public boolean addWaiting(String senderID, String vessel, String scc, int region) throws SQLException {
        if (coExist(senderID)) {
            return false;
        }
        arsDatabase.update(String.format("INSERT INTO `waiting`(coid,vesselid,coscc, vesselname, date, region) VALUES('%s','%s','%s', '%s', '%s', %s)", senderID, vesselNameToID(vessel), scc, vessel, System.currentTimeMillis(), region));
        arsDatabase.closeConnection();
        return true;
    }

    /**
     * Get the CO ID of a pending vessel registry
     *
     * @param vesselID The ID of the CO Vessel
     * @return The CO ID
     */
    public String getPendingCoId(String vesselID) throws SQLException, VesselNotFoundException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next())
            throw new VesselNotFoundException();
        arsDatabase.closeConnection();
        return rs.getString("coid");
    }

    /**
     * Method to turn a pending vessel in a full vessel
     *
     * @param vesselID The ID of the Vessel
     * @return if the switch is successful
     */
    public boolean switchPending(String vesselID) throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        String coID = rs.getString("coid");
        String vesselName = rs.getString("vesselname");
        String date = rs.getString("date");
        int region = rs.getInt("region");

        if (ARSReloaded.vesselByIdCache == null)
            getVesselByRegions();
        int regionSize = ARSReloaded.vesselByIdCache.get(region);
        regionSize++;

        ARSReloaded.vesselByIdCache.remove(region);
        ARSReloaded.vesselByIdCache.put(region, regionSize);
        ARSReloaded.vesselsCache.add(new Vessel(vesselName, vesselID, coID, "Name : %name%\\nDate : %date%\\nSCC : %scc%\\n", "#Nothing to report"));

        arsDatabase.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        arsDatabase.update(String.format("INSERT INTO `vessels`(name,vesselid,coid,template,default_text,date,region) VALUES('%s','%s','%s','%s','%s', '%s', %s)", vesselName, vesselID, coID, "Name : %name%\\nDate : %date%\\nSCC : %scc%\\n", "#Nothing to report", date, region));
        rs.close();
        arsDatabase.closeConnection();
        return true;
    }

    /**
     * Method to deny a pending vessel and delete it from database
     *
     * @param vesselID The ID of the Vessel
     * @return if the deleting is successful
     */
    public boolean deletePending(String vesselID) throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        arsDatabase.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        rs.close();
        arsDatabase.closeConnection();
        return true;
    }

    /**
     * A method to get if a member is a CO of a vessel.
     *
     * @param coID The ID of the member
     * @return If the CO is a CO
     */
    public boolean isCo(String coID) {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM vessels WHERE coid='" + coID + "'");
        try {
            if(!rs.next()) {
                rs.close();
                arsDatabase.closeConnection();
                return false;
            }
            rs.close();
            arsDatabase.closeConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        arsDatabase.closeConnection();
        return false;
    }

    /**
     * A method to get a Vessel object of the vessel associated with the given CO
     *
     * @param coID The ID of the CO
     * @return A Vessel object of the vessel associated with the CO
     * @throws SQLException
     */
    public Vessel getVesselWithCo(String coID) throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM vessels WHERE coid='" + coID + "'");
        if(!rs.next()){
            rs.close();
            arsDatabase.closeConnection();
            return new Vessel("USS Somevessel", "usssomevessel", "noone", "notemplate", "");
        }
        Vessel vessel = new Vessel(rs.getString("name").replace("_", " "), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text"));
        rs.close();
        arsDatabase.closeConnection();
        return vessel;
    }

    /**
     * A method to verify if the given ID is the CO of the given vessel
     *
     * @param vesselid The ID of the vessel
     * @param coid     The ID of the member
     * @return if the ID is the CO of the given vessel
     * @throws SQLException
     */
    public boolean isCo(String vesselid, String coid) throws SQLException {
        boolean b = arsDatabase.getResult("SELECT * FROM vessels WHERE coid='" + coid + "' AND vesselid='" + vesselid + "'").next();
        arsDatabase.closeConnection();
        return b;
    }

    /**
     * A method to change the default template of the given Vessel's reports.
     *
     * @param vessel   The Vessel object associated with the vessel
     * @param template The new template.
     */
    public void changeVesselTemplate(Vessel vessel, String template) {
        arsDatabase.update("UPDATE vessels SET template='" + template + "' WHERE vesselid='" + vessel.getVesselID() + "'");
        arsDatabase.closeConnection();
    }

    /**
     * A method to change the default template of the given Vessel's reports.
     *
     * @param vesselid The id of the Vessel
     * @param template The new template.
     */
    public void changeVesselTemplate(String vesselid, String template) {
        arsDatabase.update("UPDATE vessels SET template='" + template + "' WHERE vesselid='" + vesselid + "'");
        arsDatabase.closeConnection();
    }

    /**
     * A method to change the default reports of the given Vessel.
     *
     * @param vessel        The Vessel object associated with the vessel
     * @param defaultReport The new default report
     */
    public void changeVesselDefaultReport(Vessel vessel, String defaultReport) {
        arsDatabase.update("UPDATE vessels SET default_text='" + defaultReport + "' WHERE vesselid='" + vessel.getVesselID() + "'");
        arsDatabase.closeConnection();
    }

    /**
     * A method to change the default reports of the given Vessel.
     *
     * @param vesselid      The ID of the vessel
     * @param defaultReport
     */
    public void changeVesselDefaultReport(String vesselid, String defaultReport) {
        arsDatabase.update("UPDATE vessels SET default_text='" + defaultReport + "' WHERE vesselid='" + vesselid + "'");
        arsDatabase.closeConnection();
    }

    /**
     * A method to get all the vessels in the database
     *
     * @return An ArrayList of all the vessels in the database
     * @throws SQLException
     */
    public ArrayList<Vessel> getAllVessels() throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM vessels");
        ArrayList<Vessel> v = new ArrayList<>();

        while (rs.next()) {
            v.add(new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text")));
        }
        ARSReloaded.vesselsCache = v;
        arsDatabase.closeConnection();
        return v;
    }

    /**
     * A method to get the Vessel object associated with the ID
     *
     * @param vesselID The ID of the Vessel
     * @return The Vessel object associated with the ID
     * @throws SQLException
     */
    private Vessel getVesselById(String vesselID) throws SQLException {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM vessels WHERE vesselid='" + vesselID + "'");
        Vessel v = null;
        while (rs.next()) {
            v = new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text"));
        }
        return v;
    }

    /**
     * A method to convert the name of a Vessel into a one-word ID
     *
     * @param vesselName The Name to convert
     * @return The ID from the name
     */
    private String vesselNameToID(String vesselName) {
        String tempVesselName = vesselName;
        tempVesselName = tempVesselName.replace("_", "");
        tempVesselName = tempVesselName.toLowerCase();
        return tempVesselName;
    }

    /**
     * Check if a user exist in the database by his SCC
     *
     * @param user The User object
     * @return if the user exist or not
     */
    private boolean exist(Users user) {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM `users` WHERE scc='" + user.getScc() + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Register a new user in the database
     *
     * @param user The User object to register
     */
    public void register(Users user) {
        if (exist(user))
            return;
        Vessel v;
        try {
            v = getVesselById(user.getVesselID());
        } catch (SQLException e) {
            return;
        }
        if (user.getUuid().equalsIgnoreCase("defaultuuid"))
            return;

        arsDatabase.update(String.format("INSERT INTO `users`(name,scc,vesselid,report,uuid) VALUES('%s','%s','%s','%s','%s')", user.getName(), user.getScc(), user.getVesselID(), v.constructNewReport(), user.getUuid()));
        arsDatabase.closeConnection();
    }

    /**
     * A method to change the vessel of an user
     *
     * @param users    The User
     * @param vesselID The ID of the New Vessel
     * @return if the change was successfull
     */
    public boolean switchVessel(Users users, String vesselID) {
        if (!exist(users))
            return false;
        if (!verifyToken(users.getScc(), users.getUuid())) {
            return false;
        }
        arsDatabase.update("UPDATE users SET vesselid='" + vesselID + "' WHERE scc='" + users.getScc() + "'");
        arsDatabase.closeConnection();
        return true;
    }

    /**
     * A method to destroy the account of a user
     *
     * @param user The user
     * @return A string of the state after the action
     */
    public String destroyUser(Users user) {
        if (!verifyToken(user.getScc(), user.getUuid())) {
            return "ID Invalid";
        }

        arsDatabase.update("DELETE FROM users WHERE scc='" + user.getScc() + "'");
        arsDatabase.closeConnection();
        return "User removed";
    }

    /**
     * Save the report for the given user ( from the HTTP Call )
     *
     * @param users The User object with the report
     */
    public String saveReport(Users users) {
        if (!exist(users)) {
            register(users);
        }
        if (!verifyToken(users.getScc(), users.getUuid())) {
            return "ID Invalid";
        }
        arsDatabase.update("UPDATE `users` SET report='" + users.getReport().replace("\n", "\\n") + "' WHERE scc='" + users.getScc() + "'");
        arsDatabase.closeConnection();
        return "Save";
    }


    /**
     * A method to verify the auth token of a user to perform security task
     *
     * @param scc   The SCC of the user
     * @param token The given token
     * @return If the token is valid
     */
    public boolean verifyToken(String scc, String token) {
        String storedToken = "";
        ResultSet rs = arsDatabase.getResult("SELECT * FROM users WHERE scc='" + scc + "'");
        try {
            if (!rs.next())
                return false;
            storedToken = rs.getString("uuid");
            arsDatabase.closeConnection();
            return storedToken.equals(token);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get report of the given user ( for the synchronize HTTP Call )
     *
     * @param users The User object
     * @return The current report
     */
    public String getReport(Users users) {
        if (!exist(users)) {
            register(users);
            return "Nothing to report";
        }
        if (!verifyToken(users.getScc(), users.getUuid())) {
            return "ID Invalid";
        }
        String s = ((String) arsDatabase.read("SELECT * FROM `users` WHERE scc='" + users.getScc() + "'", "report")).replace("\n", "\\n");
        arsDatabase.closeConnection();
        return s;
    }

    /**
     * A method to synchronize the specified Users with the database
     *
     * @param users The Users
     * @return The updated Users
     */
    public Users synchronizeUser(Users users) {
        if (!exist(users)) {
            register(users);
            return users;
        }
        if (!verifyToken(users.getScc(), users.getUuid())) {
            return new Users("invalidID", "", "", "");
        }
        ResultSet rs = arsDatabase.getResult("SELECT * FROM users WHERE scc='" + users.getScc() + "'");
        try {
            if(!rs.next()){
                rs.close();
                arsDatabase.closeConnection();
                return new Users("invalidID", "invalidSCC", "", "");
            }
            String r = rs.getString("report");
            Vessel v = getVesselById(users.getVesselID());
            if (r.equalsIgnoreCase(v.constructNewReport())) {
                r = v.constructFullReport();
            }
            Users user = new Users(rs.getString("name"), rs.getString("scc"), rs.getString("vesselid"), r, rs.getString("uuid"));
            rs.close();
            arsDatabase.closeConnection();
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Users("invalidID", "invalidSCC", "", "");
    }

    /**
     * A method to get an HashMap of all the vessels sorted by Region
     *
     * @return an HashMap of all the vessels sorted by Region
     */
    public HashMap<Integer, Integer> getVesselByRegions() {
        HashMap<Integer, Integer> sd = new HashMap<>();
        for (int i = 0; i < 21; i++) {
            int s = 0;
            ResultSet rs = arsDatabase.getResult("SELECT * FROM vessels WHERE region=" + i);
            while (true) {
                try {
                    if (!rs.next()) break;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                s++;
            }
            sd.put(i, s);
        }
        arsDatabase.closeConnection();
        if (ARSReloaded.vesselByIdCache == null)
            ARSReloaded.vesselByIdCache = sd;
        return sd;
    }

    private void keepTrackReports(int i) {
        ukDateFormat.setNumberFormat(NumberFormat.getNumberInstance(Locale.US));
        ukDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        arsDatabase.update("INSERT INTO reports(date,numbers) VALUES('" + ukDateFormat.format(new Date(System.currentTimeMillis())).toUpperCase() + "', " + i + ")");
    }

    /**
     * A method to get a sorted map of all reports in the database
     *
     * @return A sorted map of all reports in the database
     */
    public TreeMap<String, Integer> getTrackedReports() {
        ResultSet rs = arsDatabase.getResult("SELECT * FROM reports ORDER BY reports . `date` ASC");
        TreeMap<String, Integer> p = new TreeMap<>();
        while (true) {
            try {
                if (!rs.next()) break;
                p.put(rs.getString("date"), rs.getInt("numbers"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        arsDatabase.closeConnection();
        return p;
    }

    private HashMap<Vessel, ArrayList<Users>> getUsersByVessels() throws SQLException {
        ArrayList<Vessel> vessels = ARSReloaded.vesselsCache;
        HashMap<Vessel, ArrayList<Users>> usersByVessels = new HashMap<>();
        for (Vessel v : vessels) {
            ArrayList<Users> tempUsers = new ArrayList<>();
            ResultSet r = arsDatabase.getResult("SELECT * FROM `users` where vesselid = '" + v.getVesselID() + "'");
            while (r.next()) {
                tempUsers.add(new Users(r.getString("name"), r.getString("scc"), r.getString("vesselid"), r.getString("report"), r.getString("uuid")));
            }
            usersByVessels.put(v, tempUsers);
        }
        return usersByVessels;
    }

    private int sendReportOfVessel(Vessel vessel, ArrayList<Users> usersList) {
        ArrayList<String> message = new ArrayList<>();
        int reports = 0;

        message.add("Starting " + ARSReloaded.DATE.format(new Date(System.currentTimeMillis())) + " Reports");
        message.add("--------------------------------------------------------------------");
        message.add(" ");
        Users[] userList = new Users[usersList.size()];
        int index = 0;

        for (Users u : usersList) {
            sendReportOfUser(u, message, vessel);
            userList[index] = u;
            index++;
            reports++;
        }

        ProcessAllReports par = ARSReloaded.processing.get(vessel.getVesselID());
        if (par != null)
            par.process(userList, vessel);

        message.add(" ");
        message.add("End of " + ARSReloaded.DATE.format(new Date(System.currentTimeMillis())) + " Reports");
        ARSReloaded.sendMessage(vessel.getCoID(), StringUtils.join(message, "\n"));
        return reports;
    }

    private void sendReportOfUser(Users u, ArrayList<String> message, Vessel vessel) {
        message.add(u.constructReport(vessel));
        message.add("--------------------------------------------------------------------");
        arsDatabase.update("UPDATE `users` SET report='" + vessel.constructNewReport() + "' WHERE scc='" + u.getScc() + "'");
        arsDatabase.closeConnection();
        ReportProcessing rp = ARSReloaded.processingHashMap.get(vessel.getVesselID());
        if (rp != null)
            rp.process(u, vessel);
    }

    /**
     * Method to send all reports of all user to their respective CO
     *
     * @return Report
     */
    public String sendReports() throws SQLException {
        System.out.println("Reports");
        HashMap<Vessel, ArrayList<Users>> usersByVessels = getUsersByVessels();

        MutableInt reports = new MutableInt(0);

        usersByVessels.keySet().forEach(vessel -> reports.add(sendReportOfVessel(vessel, usersByVessels.get(vessel))));

        setLast();
        keepTrackReports(reports.intValue());
        arsDatabase.closeConnection();
        return "Report";
    }


    /**
     * Method to get the timestamp of the last sending
     *
     * @return The timestamp.
     */
    public Long getLast() {
        Long l = (Long) arsDatabase.read("SELECT * FROM `properties` WHERE id=0", "last");
        arsDatabase.closeConnection();
        return l;
    }


    /**
     * Method to set the last timestamp of the sending
     */
    public void setLast() {
        arsDatabase.update("UPDATE `properties` SET last=" + System.currentTimeMillis() + " WHERE id=0");
        arsDatabase.closeConnection();
    }


}
