package fr.charlotte.arsreloaded.verifiers.implementations;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.verifiers.Verifier;

public class VesselTemplateVerifier extends Verifier {

    private String template;

    public VesselTemplateVerifier(String vesselID, String coID, String template) {
        super(vesselID, coID);
        this.template = template;
    }

    @Override
    protected boolean process(DatabaseWrapper wrapper) {
        template = template.replace("\n", "\\n");
        wrapper.changeVesselTemplate(vesselID, template);
        return true;
    }
}
