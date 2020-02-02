package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.ARSReloaded;

import java.sql.SQLException;

public class CheckVesselName {

    private String vesselID;
    private String coid;
    private String text;

    public CheckVesselName(String vesselID, String coid, String text) {
        this.vesselID = vesselID;
        this.coid = coid;
        this.text = text;
    }

    public boolean update() {
        DatabaseWrapper w = ARSReloaded.getWrapper();
        try {
            if (!w.isCo(vesselID, coid)) {
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
