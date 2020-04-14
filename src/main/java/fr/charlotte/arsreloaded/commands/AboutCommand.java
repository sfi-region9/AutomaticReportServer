package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import org.pf4j.Extension;

import java.util.ArrayList;

@Extension
public class AboutCommand extends Command {

    public AboutCommand() {
        super("about");
    }


    @Override
    public void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper) {
        ArrayList<String> message = new ArrayList<>();
        message.add(" ");
        message.add("Automatize all your report in your chapter !");
        message.add("Send a message to Charlotte THOMAS, a mail to contact@nwa2coco.fr for any infos !");
        message.add("Developed with ❤️ by Charlotte THOMAS");
        message.add("Maintained and Hosted by USS Versailles, R9");
        message.add(" ");
        utils.sendMultiMessage(senderID, "Automatic Report Server", message);
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
