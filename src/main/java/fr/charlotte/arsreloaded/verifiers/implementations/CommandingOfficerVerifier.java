package fr.charlotte.arsreloaded.verifiers.implementations;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.verifiers.Verifier;

public class CommandingOfficerVerifier extends Verifier {

    public CommandingOfficerVerifier(String vesselID, String coID) {
        super(vesselID, coID);
    }

    @Override
    protected boolean process(DatabaseWrapper wrapper) {
        return true;
    }


}
