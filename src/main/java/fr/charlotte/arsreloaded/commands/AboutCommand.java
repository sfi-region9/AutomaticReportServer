package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.plugins.Command;
import org.pf4j.Extension;

import java.util.ArrayList;

import static fr.charlotte.arsreloaded.ARSReloaded.*;

@Extension
public class AboutCommand extends Command {

    public AboutCommand() {
        super("about");
    }


    @Override
    public void onCommand(String senderID, String text, String[] args) {
        ArrayList<String> all = new ArrayList<>();
        all.add(" ");
        all.add("Automatize all your report in your chapter !");
        all.add("Send a message to Colin THOMAS, a mail to contact@nwa2coco.fr for any infos !");
        all.add("Developed with ❤️ by Colin THOMAS");
        all.add("Maintained and Hosted by USS Versailles, R9");
        all.add(" ");
        sendMultiMessage(senderID, "Automatic Report Server", all);
    }

    @Override
    public String usage() {
        return "About";
    }

    @Override
    public String args() {
        return "";
    }
}
