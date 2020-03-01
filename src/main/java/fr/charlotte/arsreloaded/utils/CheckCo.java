package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.AutomaticReportServer;

import java.sql.SQLException;

public class CheckCo {

    private String vesselID;
    private String coID;

    public CheckCo(String vesselID, String coID) {
        this.vesselID = vesselID;
        this.coID = coID;
    }

    public boolean process() {
        try {
            return AutomaticReportServer.getWrapper().isCo(vesselID, coID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
