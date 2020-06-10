package fr.charlotte.arsreloaded.verifiers;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;

import java.sql.SQLException;

public abstract class Verifier implements IVerifier {

    protected final String coID;
    protected final String vesselID;

    public Verifier(String vesselID, String coID) {
        this.coID = coID;
        this.vesselID = vesselID;
    }

    protected abstract boolean process(DatabaseWrapper wrapper);


    @Override
    public boolean update(DatabaseWrapper wrapper) {
        try {
            if (!wrapper.isCo(vesselID, coID)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return process(wrapper);
    }
}
