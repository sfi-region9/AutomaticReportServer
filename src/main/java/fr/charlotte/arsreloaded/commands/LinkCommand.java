package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import org.pf4j.Extension;


@Extension
public class LinkCommand extends Command {
    public LinkCommand() {
        super("link", "linking");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper) {
        if (args.length < 2) {
            utils.sendMessage(senderID, "Usage : !link " + this.args());
            return;
        }
        String username = args[0];
        String uuid = args[1];
        boolean r = userWrapper.updateId(username, uuid, senderID);
        if(!r){
            utils.sendMessage(senderID, "The uuid or username you type is invalid.");
            return;
        }
        utils.sendMessage(senderID, "Your messenger account was successfully linked to your ARW account !");
    }


    @Override
    public String usage() {
        return "link your website account with your messenger account";
    }

    @Override
    public String args() {
        return " [username] [uuid]";
    }
}
