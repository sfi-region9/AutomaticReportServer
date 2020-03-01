package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.ARSReloaded;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;

import java.sql.SQLException;

public class CheckVessel {

    private String vesselID;
    private String coID;
    private String template;

    public CheckVessel(String vesselID, String coID, String template) {
        this.vesselID = vesselID;
        this.coID = coID;
        this.template = template;
    }

    public boolean update() {
        DatabaseWrapper databaseWrapper = ARSReloaded.getWrapper();
        try {
            if (!databaseWrapper.isCo(vesselID, coID)) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        template = template.replace("\n", "\\n");
        databaseWrapper.changeVesselTemplate(vesselID, template);
        return false;
    }
}
