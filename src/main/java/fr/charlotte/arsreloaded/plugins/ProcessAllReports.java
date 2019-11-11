package fr.charlotte.arsreloaded.plugins;

import fr.charlotte.arsreloaded.utils.Users;
import fr.charlotte.arsreloaded.utils.Vessel;
import org.pf4j.ExtensionPoint;

public interface ProcessAllReports extends ExtensionPoint {

    void process(Users[] users, Vessel vessel);
    String getID();
    String getVesselID();

}
