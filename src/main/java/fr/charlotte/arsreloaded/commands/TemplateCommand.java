package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.Vessel;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.charlotte.arsreloaded.AutomaticReportServer.*;

@Extension
public class TemplateCommand extends Command {

    public TemplateCommand() {
        super("template");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length == 0) {
            ArrayList<String> message = new ArrayList<>();
            message.add("This command allow you to alter the default template (NAME,DATE,SCC) with a variety of placeholders ( %date% show the date, %name% the name, %scc% the SCC, %month% for the month, %year% for the year and %vesselname% for the name of the vessel).");
            message.add("If you want something advanced ( msr-like template for example ) or more user-friendly please use the web version ( require a linked account ) at https://reports.nwa2coco.fr/pages/template.php");
            message.add(" ");
            message.add("To use it, just send the !template command with your wanted template, the \\n word must be used to symbolise a new line.");
            message.add("Example the default template is this : Name : %name%\\nDate : %date%\\nSCC : %scc%");
            message.add("So the complete command for the default template is : !template Name : %name%\\nDate : %date%\\nSCC : %scc%");
            sendMultiMessage(senderID, "Template Help", message);
            return;
        }
        String localText = text;
        localText = localText.substring(10);
        DatabaseWrapper databaseWrapper = getWrapper();
        if (!databaseWrapper.isCo(senderID)) {
            sendMessage(senderID, "You are not a CO of a vessel.");
            return;
        }
        try {
            Vessel vessel = databaseWrapper.getVesselWithCo(senderID);
            databaseWrapper.changeVesselTemplate(vessel, localText);
            sendMessage(senderID, "You changed your chapter report template to \n" + localText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String usage() {
        return "allow you to alter the template of your chapter reports.";
    }

    @Override
    public String args() {
        return " [template]";
    }
}
