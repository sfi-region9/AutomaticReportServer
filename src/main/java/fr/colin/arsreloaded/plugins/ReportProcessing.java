package fr.colin.arsreloaded.plugins;

import fr.colin.arsreloaded.objects.Users;
import fr.colin.arsreloaded.objects.Vessel;
import org.pf4j.ExtensionPoint;

public interface ReportProcessing  extends ExtensionPoint {

    void process(Users users, Vessel vessel);
    String getID();
    String getVesselID();



}
