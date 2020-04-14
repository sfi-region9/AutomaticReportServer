package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import org.pf4j.Extension;

@Extension
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper) {
        System.out.println("Help Command received from " + senderID);
        utils.sendHelp(senderID);
    }

    @Override
    public String usage() {
        return "shows help topic";
    }

    @Override
    public String args() {
        return "";
    }
}
