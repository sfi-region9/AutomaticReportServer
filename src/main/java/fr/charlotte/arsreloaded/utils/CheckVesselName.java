package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.ARSReloaded;

import java.sql.SQLException;

public class CheckVesselName {

    private String vesselID;
    private String coID;
    private String text;

    public CheckVesselName(String vesselID, String coID, String text) {
        this.vesselID = vesselID;
        this.coID = coID;
        this.text = text;
    }

    public boolean update() {
        DatabaseWrapper w = ARSReloaded.getWrapper();
        try {
            if (!w.isCo(vesselID, coID)) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        text = text.replace("\n", "\\n");
        w.changeVesselDefaultReport(vesselID, text);
        return false;
    }
}
