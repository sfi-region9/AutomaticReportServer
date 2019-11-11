package fr.charlotte.arsreloaded.databases;

import fr.charlotte.arsreloaded.ARSReloaded;
import fr.charlotte.arsreloaded.plugins.ProcessAllReports;
import fr.charlotte.arsreloaded.utils.Users;
import fr.charlotte.arsreloaded.utils.Vessel;
import fr.charlotte.arsreloaded.utils.VesselNotFoundException;
import fr.charlotte.arsreloaded.plugins.ReportProcessing;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class to wrap the database into easy-to-use methods
 */

public class DatabaseWrapper {

    Database db = ARSReloaded.getDb();

    public DatabaseWrapper() {

    }

    /**
     * Method to get all pending vessels in order to accept/deny them
     *
     * @return An ArrayList with all the pending vessel ( name,co and id )
     */
    public ArrayList<String> getPendingWaiting() throws SQLException {
        ArrayList<String> a = new ArrayList<>();
        ResultSet rs = db.getResult("SELECT * FROM `waiting`");
        a.add("");
        while (rs.next()) {
            a.add("● " + rs.getString("vesselname") + " CO : " + rs.getString("COID") + " ID : " + rs.getString("vesselid"));
        }
        if (a.size() == 1) {
            a.clear();
        }
        return a;
    }

    /**
     * Method to check if a co exist (when a co apply to register his chapter)
     *
     * @param senderID The ID of the CO
     * @return if the co exist or not in the database
     **/
    private boolean coExist(String senderID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `vessels` WHERE coid = '" + senderID + "'");
        if (rs.next())
            return true;
        else {
            ResultSet s = db.getResult("SELECT * FROM `waiting` WHERE coid='" + senderID + "'");
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
        db.update(String.format("INSERT INTO `waiting`(coid,vesselid,coscc, vesselname, date, region) VALUES('%s','%s','%s', '%s', '%s', %s)", senderID, vesselNameToID(vessel), scc, vessel, System.currentTimeMillis(), region));
        return true;
    }

    /**
     * Get the CO ID of a pending vessel registry
     *
     * @param vesselID The ID of the CO Vessel
     * @return The CO ID
     */
    public String getPendingCoId(String vesselID) throws SQLException, VesselNotFoundException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next())
            throw new VesselNotFoundException();
        return rs.getString("coid");
    }

    /**
     * Method to turn a pending vessel in a full vessel
     *
     * @param vesselID The ID of the Vessel
     * @return if the switch is successful
     */
    public boolean switchPending(String vesselID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        String coID = rs.getString("coid");
        String vesselid = rs.getString("vesselid");
        String coscc = rs.getString("coscc");
        String vesselName = rs.getString("vesselname");
        String date = rs.getString("date");
        int region = rs.getInt("region");

        if (ARSReloaded.vesselByIdCache == null)
            getVesselByRegions();
        int i = ARSReloaded.vesselByIdCache.get(region);
        i++;
        ARSReloaded.vesselByIdCache.remove(region);
        ARSReloaded.vesselByIdCache.put(region, i);

        db.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        db.update(String.format("INSERT INTO `vessels`(name,vesselid,coid,template,default_text,date,region) VALUES('%s','%s','%s','%s','%s', '%s', %s)", vesselName, vesselid, coID, "Name : %name%\\nDate : %date%\\nSCC : %scc%\\n", "#Nothing to report", date, region));
        return true;
    }

    /**
     * Method to deny a pending vessel and delete it from database
     *
     * @param vesselID The ID of the Vessel
     * @return if the deleting is successful
     */
    public boolean deletePending(String vesselID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        db.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        return true;
    }

    public boolean isCo(String coID) {
        ResultSet rs = db.getResult("SELECT * FROM vessels WHERE coid='" + coID + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Vessel vCo(String coID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM vessels WHERE coid='" + coID + "'");
        rs.next();
        return new Vessel(rs.getString("name").replace("_", " "), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text"));
    }

    public boolean isCo(String vesselid, String coid) throws SQLException {
        return db.getResult("SELECT * FROM vessels WHERE coid='" + coid + "' AND vesselid='" + vesselid + "'").next();
    }

    public void changeVesselTemplate(Vessel vessel, String template) {
        db.update("UPDATE vessels SET template='" + template + "' WHERE vesselid='" + vessel.getVesselid() + "'");
    }

    public void changeVesselTemplate(String vesselid, String template) {
        db.update("UPDATE vessels SET template='" + template + "' WHERE vesselid='" + vesselid + "'");
    }


    public void changeVesselDefaultReport(Vessel vessel, String defaul) {
        db.update("UPDATE vessels SET default_text='" + defaul + "' WHERE vesselid='" + vessel.getVesselid() + "'");
    }

    public void changeVesselDefaultReport(String vesselid, String defaul) {
        db.update("UPDATE vessels SET default_text='" + defaul + "' WHERE vesselid='" + vesselid + "'");
    }

    public ArrayList<Vessel> allVessels() throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM vessels");
        ArrayList<Vessel> v = new ArrayList<>();

        while (rs.next()) {
            v.add(new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text")));
        }

        return v;
    }

    public Vessel getVesselById(String vesselid) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM vessels WHERE vesselid='" + vesselid + "'");
        Vessel v = null;
        while (rs.next()) {
            v = new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text"));
        }
        return v;
    }

    /**
     * A method to convert a vesselname in a one-word ID
     *
     * @param vesselName The Name to convert
     * @return The ID from the name
     */
    private String vesselNameToID(String vesselName) {
        vesselName = vesselName.replace("_", "");
        vesselName = vesselName.toLowerCase();
        return vesselName;
    }

    /**
     * Check if a user exist in the database by his SCC
     *
     * @param user The User object
     * @return if the user exist or not
     */
    private boolean exist(Users user) {
        ResultSet rs = db.getResult("SELECT * FROM `users` WHERE scc='" + user.getScc() + "'");
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
            v = fromId(user.getVesselid());
        } catch (SQLException e) {
            return;
        }
        if (user.getUuid().equalsIgnoreCase("defaultuuid"))
            return;

        db.update(String.format("INSERT INTO `users`(name,scc,vesselid,report,uuid) VALUES('%s','%s','%s','%s','%s')", user.getName(), user.getScc(), user.getVesselid(), v.constructNewReport(), user.getUuid()));
    }

    public boolean switchVessel(Users u, String vesselid) {
        if (!exist(u))
            return false;
        if (!verifyToken(u.getScc(), u.getUuid())) {
            return false;
        }
        db.update("UPDATE users SET vesselid='" + vesselid + "' WHERE scc='" + u.getScc() + "'");
        return true;
    }


    public String destroyUser(Users u) {
        if (!verifyToken(u.getScc(), u.getUuid())) {
            return "ID Invalid";
        }

        db.update("DELETE FROM users WHERE scc='" + u.getScc() + "'");
        return "User removed";
    }

    public Vessel fromId(String vesselID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM vessels WHERE vesselid='" + vesselID + "'");
        rs.next();
        return new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text"));
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
        db.update(String.format("UPDATE `users` SET report='" + users.getReport().replace("\n", "\\n") + "' WHERE scc='" + users.getScc() + "'"));
        return "Save";
    }


    public boolean verifyToken(String scc, String token) {
        String storedToken = "";
        ResultSet rs = db.getResult("SELECT * FROM users WHERE scc='" + scc + "'");
        try {
            if (!rs.next())
                return false;
            storedToken = rs.getString("uuid");
            return storedToken.equals(token);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get report of the given user ( for the syncronize HTTP Call )
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
        return ((String) db.read("SELECT * FROM `users` WHERE scc='" + users.getScc() + "'", "report")).replace("\n", "\\n");
    }

    public Users syncronizeUser(Users users) {
        if (!exist(users)) {
            register(users);
            return users;
        }
        if (!verifyToken(users.getScc(), users.getUuid())) {
            return new Users("invalidID", "", "", "");
        }
        ResultSet rs = db.getResult("SELECT * FROM users WHERE scc='" + users.getScc() + "'");
        try {
            rs.next();
            String r = rs.getString("report");
            Vessel v = getVesselById(users.getVesselid());
            if (r.equalsIgnoreCase(v.constructNewReport())) {
                r = v.constructFullReport();
            }
            return new Users(rs.getString("name"), rs.getString("scc"), rs.getString("vesselid"), r, rs.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Users("invalidID", "", "", "");
    }

    public HashMap<Integer, Integer> getVesselByRegions() {
        HashMap<Integer, Integer> sd = new HashMap<>();
        for (int i = 0; i < 21; i++) {
            int s = 0;
            ResultSet rs = db.getResult("SELECT * FROM vessels WHERE region=" + i);
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

        if (ARSReloaded.vesselByIdCache == null)
            ARSReloaded.vesselByIdCache = sd;
        return sd;
    }

    static SimpleDateFormat d = new SimpleDateFormat("YYYY-MM-dd", Locale.UK);


    public void keepTrackReports(int i) {
        d.setNumberFormat(NumberFormat.getNumberInstance(Locale.US));
        d.setTimeZone(TimeZone.getTimeZone("GMT"));
        db.update("INSERT INTO reports(date,numbers) VALUES('" + d.format(new Date(System.currentTimeMillis())).toUpperCase() + "', " + i + ")");
    }

    public TreeMap<String, Integer> getTrackedReports() {
        ResultSet rs = db.getResult("SELECT * FROM reports ORDER BY reports . `date` ASC");
        TreeMap<String, Integer> p = new TreeMap<>();
        while (true) {
            try {
                if (!rs.next()) break;
                p.put(rs.getString("date"), rs.getInt("numbers"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    /**
     * Method to send all reports of all user to their respective CO
     *
     * @return Report
     */
    public String sendReports() throws SQLException {
        System.out.println("Reports");
        ResultSet rs = db.getResult("SELECT * FROM `vessels`");
        ArrayList<Vessel> vessels = new ArrayList<>();
        HashMap<Vessel, ArrayList<Users>> users = new HashMap<>();
        int reports = 0;
        while (rs.next()) {
            vessels.add(new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"), rs.getString("template"), rs.getString("default_text")));
        }

        for (Vessel v : vessels) {
            ArrayList<Users> tempUsers = new ArrayList<>();
            ResultSet r = db.getResult("SELECT * FROM `users` where vesselid = '" + v.getVesselid() + "'");
            while (r.next()) {
                tempUsers.add(new Users(r.getString("name"), r.getString("scc"), r.getString("vesselid"), r.getString("report"), r.getString("uuid")));
            }
            users.put(v, tempUsers);
        }

        for (Vessel v : users.keySet()) {
            ArrayList<String> message = new ArrayList<>();
            message.add("Starting " + ARSReloaded.DATE.format(new Date(System.currentTimeMillis())) + " Reports");
            message.add("--------------------------------------------------------------------");
            message.add(" ");
            Users[] s = new Users[users.get(v).size()];
            int index = 0;
            for (Users u : users.get(v)) {
                System.out.println(u.getVesselid() + " " + u.getScc() + " " + v.getCoid() + " " + (ARSReloaded.messenger == null));
                message.add(u.constructReport(v));
                message.add("--------------------------------------------------------------------");
                db.update("UPDATE `users` SET report='" + v.constructNewReport() + "' WHERE scc='" + u.getScc() + "'");
                ReportProcessing rp = ARSReloaded.processingHashMap.get(v.getVesselid());

                if (rp != null)
                    rp.process(u, v);
                s[index] = u;
                index++;
                reports++;
            }
            ProcessAllReports par = ARSReloaded.processings.get(v.getVesselid());
            if (par != null)
                par.process(s, v);

            message.add(" ");
            message.add("End of " + ARSReloaded.DATE.format(new Date(System.currentTimeMillis())) + " Reports");
            ARSReloaded.sendMessage(v.getCoid(), StringUtils.join(message, "\n"));
        }
        setLast();
        keepTrackReports(reports);
        return "Report";
    }

    /**
     * Method to get the timestamp of the last sending
     *
     * @return The timestamp.
     */
    public Long getLast() {
        return (Long) db.read("SELECT * FROM `properties` WHERE id=0", "last");
    }


    /**
     * Method to set the last timestamp of the sending
     */
    public void setLast() {
        db.update("UPDATE `properties` SET last=" + System.currentTimeMillis() + " WHERE id=0");
    }


}
