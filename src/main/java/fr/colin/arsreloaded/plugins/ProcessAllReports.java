package fr.colin.arsreloaded.plugins;

import fr.colin.arsreloaded.utils.Users;
import fr.colin.arsreloaded.utils.Vessel;
import org.pf4j.ExtensionPoint;

public interface ProcessAllReports extends ExtensionPoint {

    void process(Users[] users, Vessel vessel);
    String getID();
    String getVesselID();

}
