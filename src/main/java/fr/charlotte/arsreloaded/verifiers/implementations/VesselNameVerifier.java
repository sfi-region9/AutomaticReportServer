package fr.charlotte.arsreloaded.verifiers.implementations;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.verifiers.Verifier;

public class VesselNameVerifier extends Verifier {

    private String text;

    public VesselNameVerifier(String vesselID, String coID, String text) {
        super(vesselID, coID);
        this.text = text;
    }

    @Override
    protected boolean process() {
        text = text.replace("\n", "\\n");
        databaseWrapper.changeVesselDefaultReport(vesselID, text);
        return true;
    }
}
