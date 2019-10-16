package fr.colin.arsreloaded.utils;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.databases.DatabaseWrapper;

import java.sql.SQLException;

public class CheckVessel {

    private String vesselid;
    private String coid;
    private String template;

    public CheckVessel(String vesselid, String coid, String template) {
        this.vesselid = vesselid;
        this.coid = coid;
        this.template = template;
    }

    public boolean update() {
        DatabaseWrapper w = ARSReloaded.getWrapper();
        try {
            if (!w.isCo(vesselid, coid)) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        template = template.replace("\n", "\\n");
        w.changeVesselTemplate(vesselid, template);
        return false;
    }
}
