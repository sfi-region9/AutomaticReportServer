package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.Vessel;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.charlotte.arsreloaded.AutomaticReportServer.sendMessage;
import static fr.charlotte.arsreloaded.AutomaticReportServer.sendMultiMessage;

@Extension
public class DefaultCommand extends Command {

    public DefaultCommand() {
        super("default");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length == 0) {
            ArrayList<String> message = new ArrayList<>();
            message.add("This command allow you to alter the default report witch is 'nothing to report'");
            message.add("To use it, just send the !template command with your wanted template, the \\n word must be used to symbolise a new line.");
            message.add("So the complete command for the default template is : !default nothing to report");
            sendMultiMessage(senderID, "Default Report Help", message);
            return;
        }
        String localText = text;
        localText = localText.substring(9);
        DatabaseWrapper dw = AutomaticReportServer.getWrapper();
        if (!dw.isCo(senderID)) {
            sendMessage(senderID, "You are not a CO of a vessel.");
            return;
        }
        try {
            Vessel vessel = dw.getVesselWithCo(senderID);
            dw.changeVesselDefaultReport(vessel, localText);
            sendMessage(senderID, "You changed your chapter report default report to \n" + localText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String usage() {
        return "allows you to alter the default report of your chapter reports.";
    }

    @Override
    public String args() {
        return " [report]";
    }
}
