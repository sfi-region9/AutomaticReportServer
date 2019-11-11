package fr.charlotte.arsreloaded.utils;

import fr.charlotte.arsreloaded.ARSReloaded;

import java.sql.SQLException;

public class CheckCo {

    private String vesselid;
    private String coid;

    public CheckCo(String vesselid, String coid) {
        this.vesselid = vesselid;
        this.coid = coid;
    }

    public boolean process() {
        try {
            return ARSReloaded.getWrapper().isCo(vesselid, coid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
