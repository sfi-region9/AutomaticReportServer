package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.ARSReloaded;

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
            return ARSReloaded.getWrapper().isCo(vesselID, coID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
