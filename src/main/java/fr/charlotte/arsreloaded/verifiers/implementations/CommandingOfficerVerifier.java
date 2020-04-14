package fr.charlotte.arsreloaded.verifiers.implementations;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.verifiers.Verifier;

public class CommandingOfficerVerifier extends Verifier {

    public CommandingOfficerVerifier(String vesselID, String coID, DatabaseWrapper wrapper) {
        super(vesselID, coID, wrapper);
    }

    @Override
    protected boolean process() {
        return true;
    }


}
