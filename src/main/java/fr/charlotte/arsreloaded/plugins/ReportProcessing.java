package fr.charlotte.arsreloaded.plugins;

import fr.charlotte.arsreloaded.utils.Users;
import fr.charlotte.arsreloaded.utils.Vessel;
import org.pf4j.ExtensionPoint;

public interface ReportProcessing extends ExtensionPoint, ReportProcess {

    void process(Users users, Vessel vessel);


}
