package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.Command;
import fr.colin.arsreloaded.objects.Vessel;
import fr.colin.arsreloaded.utils.DatabaseWrapper;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.colin.arsreloaded.ARSReloaded.*;

public class TemplateCommand extends Command {

    public TemplateCommand() {
        super("template");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length == 0) {
            ArrayList<String> mesa = new ArrayList<>();
            mesa.add("This command allow you to alter the default template (NAME,DATE,SCC) with a variety of placeholders ( %date% show the date, %name% the name, %scc% the SCC ).");
            mesa.add("If you want something advanced ( msr-like template for example ) or more user-friendly please use the web version ( require a linked account ) at https://reports.nwa2coco.fr/pages/template.php");
            mesa.add(" ");
            mesa.add("To use it, just send the !template command with your wanted template, the \\n word must be used to symbolise a new line.");
            mesa.add("Example the default template is this : Name : %name%\\nDate : %date%\\nSCC : %scc%");
            mesa.add("So the complete command for the default template is : !template Name : %name%\\nDate : %date%\\nSCC : %scc%");
            sendMultiMessage(senderID, "Template Help", mesa);
            return;
        }
        text = text.substring(10);
        DatabaseWrapper dw = ARSReloaded.getWrapper();
        if (!dw.isCo(senderID)) {
            sendMessage(senderID, "You are not a CO of a vessel.");
            return;
        }
        try {
            Vessel vessel = dw.vCo(senderID);
            dw.changeVesselTemplate(vessel, text);
            sendMessage(senderID, "You changed your chapter report template to \n" + text);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //TODO : TEST AND UPDATE
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
