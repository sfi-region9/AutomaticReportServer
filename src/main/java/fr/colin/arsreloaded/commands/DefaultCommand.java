package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.Command;
import fr.colin.arsreloaded.objects.Vessel;
import fr.colin.arsreloaded.utils.DatabaseWrapper;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.colin.arsreloaded.ARSReloaded.sendMessage;
import static fr.colin.arsreloaded.ARSReloaded.sendMultiMessage;

public class DefaultCommand extends Command {

    public DefaultCommand() {
        super("default");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length == 0) {
            ArrayList<String> mesa = new ArrayList<>();
            mesa.add("This command allow you to alter the default report witch is 'nothing to report'");
            mesa.add("To use it, just send the !template command with your wanted template, the \\n word must be used to symbolise a new line.");
            mesa.add("So the complete command for the default template is : !default nothing to report");
            sendMultiMessage(senderID, "Default Report Help", mesa);
            return;
        }
        text = text.substring(9);
        DatabaseWrapper dw = ARSReloaded.getWrapper();
        if (!dw.isCo(senderID)) {
            sendMessage(senderID, "You are not a CO of a vessel.");
            return;
        }
        try {
            Vessel vessel = dw.vCo(senderID);
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
