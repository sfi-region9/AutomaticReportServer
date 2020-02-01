package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.ARSReloaded;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.Vessel;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.charlotte.arsreloaded.ARSReloaded.sendMessage;
import static fr.charlotte.arsreloaded.ARSReloaded.sendMultiMessage;

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
        text = text.substring(9);
        DatabaseWrapper dw = ARSReloaded.getWrapper();
        if (!dw.isCo(senderID)) {
            sendMessage(senderID, "You are not a CO of a vessel.");
            return;
        }
        try {
            Vessel vessel = dw.getVesselWithCo(senderID);
            dw.changeVesselDefaultReport(vessel, text);
            sendMessage(senderID, "You changed your chapter report default report to \n" + text);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //TODO : TEST AND UPDATE
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
