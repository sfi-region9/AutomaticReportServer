package fr.charlotte.arsreloaded.verifiers;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;

import java.sql.SQLException;

public abstract class Verifier implements IVerifier {

    protected final String coID;
    protected final String vesselID;
    protected DatabaseWrapper wrapper;

    public Verifier(String vesselID, String coID, DatabaseWrapper wrapper) {
        this.coID = coID;
        this.vesselID = vesselID;
        this.wrapper = wrapper;
    }

    protected abstract boolean process();

    @Override
    public boolean update() {

        try {
            if (!wrapper.isCo(vesselID, coID)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return process();
    }
}
