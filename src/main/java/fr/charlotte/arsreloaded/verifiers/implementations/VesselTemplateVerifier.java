package fr.charlotte.arsreloaded.verifiers.implementations;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.verifiers.Verifier;

public class VesselTemplateVerifier extends Verifier {

    private String template;

    public VesselTemplateVerifier(String vesselID, String coID, String template) {
        super(vesselID, coID);
        this.template = template;
    }

    @Override
    protected boolean process() {
        template = template.replace("\n", "\\n");
        databaseWrapper.changeVesselTemplate(vesselID, template);
        return true;
    }
}
