package fr.colin.arsreloaded.utils;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.objects.Users;
import fr.colin.arsreloaded.objects.Vessel;
import fr.colin.arsreloaded.objects.VesselNotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseWrapper {

    Database db = ARSReloaded.getDb();

    public DatabaseWrapper() {

    }

    public Vessel getVesselWithID(String vesselID) throws VesselNotFoundException, SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `vessels` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            throw new VesselNotFoundException();
        }
        return new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid"));
    }

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


    public boolean coExist(String senderID) throws SQLException {
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

    public boolean addWaiting(String senderID, String vessel, String scc) throws SQLException {
        if (coExist(senderID)) {
            return false;
        }
        db.update(String.format("INSERT INTO `waiting`(coid,vesselid,coscc, vesselname) VALUES('%s','%s','%s', '%s')", senderID, vesselNameToID(vessel), scc, vessel));
        return true;
    }

    public String getPendingCoId(String vesselID) throws SQLException, VesselNotFoundException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next())
            throw new VesselNotFoundException();
        return rs.getString("coid");
    }

    public boolean switchPending(String vesselID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        String coID = rs.getString("coid");
        String vesselid = rs.getString("vesselid");
        String coscc = rs.getString("coscc");
        String vesselName = rs.getString("vesselname");
        db.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        db.update(String.format("INSERT INTO `vessels`(name,vesselid,coid) VALUES('%s','%s','%s')", vesselName, vesselid, coID));
        return true;
    }


    public boolean deletePending(String vesselID) throws SQLException {
        ResultSet rs = db.getResult("SELECT * FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        if (!rs.next()) {
            return false;
        }
        db.update("DELETE FROM `waiting` WHERE vesselid = '" + vesselID + "'");
        return true;
    }

    public String vesselNameToID(String vesselName) {
        vesselName = vesselName.replace("_", "");
        vesselName = vesselName.toLowerCase();
        return vesselName;
    }

    public boolean exist(Users user) {
        ResultSet rs = db.getResult("SELECT * FROM `users` WHERE scc='" + user.getScc() + "'");
        try {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void register(Users user) {
        if (exist(user))
            return;
        db.update(String.format("INSERT INTO `users`(name,scc,vesselid,report) VALUES('%s','%s','%s','Nothing to report')", user.getName(), user.getScc(), user.getVesselid()));
    }

    public void saveReport(Users users) {
        if (!exist(users)) {
            register(users);
        }
        db.update(String.format("UPDATE `users` SET report='" + users.getReport().replace("\n", "\\n") + "' WHERE scc='" + users.getScc() + "'"));
    }

    public String getReport(Users users) {
        if (!exist(users)) {
            register(users);
            return "Nothing to report";
        }
        return ((String) db.read("SELECT * FROM `users` WHERE scc='" + users.getScc() + "'", "report")).replace("\n", "\\n");
    }

    public String sendReports() throws SQLException {
        System.out.println("Reports");
        ResultSet rs = db.getResult("SELECT * FROM `vessels`");
        ArrayList<Vessel> vessels = new ArrayList<>();
        HashMap<Vessel, ArrayList<Users>> users = new HashMap<>();

        while (rs.next()) {
            vessels.add(new Vessel(rs.getString("name"), rs.getString("vesselid"), rs.getString("coid")));
        }

        for (Vessel v : vessels) {
            ArrayList<Users> tempUsers = new ArrayList<>();
            ResultSet r = db.getResult("SELECT * FROM `users` where vesselid = '" + v.getVesselid() + "'");
            while (r.next()) {
                tempUsers.add(new Users(r.getString("name"), r.getString("scc"), r.getString("vesselid"), r.getString("report")));
            }
            users.put(v, tempUsers);
        }
        System.out.println(users.size());

        for (Vessel v : users.keySet()) {
            System.out.println("Debug");
            for (Users u : users.get(v)) {
                System.out.println(u.getVesselid() + " " + u.getScc() + " " + v.getCoid() + " " + (ARSReloaded.messenger==null));
                ARSReloaded.sendMessage(v.getCoid(), u.constructReport());
                db.update("UPDATE `users` SET report='Nothing to report' WHERE scc='" + u.getScc() + "'");
            }
        }

        return "Report";
    }

    public Long getLast() {
        return (Long) db.read("SELECT * FROM `properties` WHERE id=0", "last");
    }

    public void setLast() {
        db.update("UPDATE `properties` SET last=" + System.currentTimeMillis() + " WHERE id=0");
    }


}
