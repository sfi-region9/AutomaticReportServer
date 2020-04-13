package fr.charlotte.arsreloaded.verifiers;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;

import java.sql.SQLException;

public abstract class Verifier implements IVerifier {

    protected final String coID;
    protected final String vesselID;

    protected DatabaseWrapper databaseWrapper = AutomaticReportServer.getWrapper();

    public Verifier(String vesselID, String coID) {
        this.coID = coID;
        this.vesselID = vesselID;
    }

    protected abstract boolean process();

    @Override
    public boolean update() {

        try {
            if (!databaseWrapper.isCo(vesselID, coID)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return process();
    }
}
