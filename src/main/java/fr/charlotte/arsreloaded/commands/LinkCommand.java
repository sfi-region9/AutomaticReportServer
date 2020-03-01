package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import org.pf4j.Extension;

import static fr.charlotte.arsreloaded.ARSReloaded.*;

@Extension
public class LinkCommand extends Command {
    public LinkCommand() {
        super("link", "linking");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length < 2) {
            sendMessage(senderID, "Usage : !link " + this.args());
            return;
        }
        String username = args[0];
        String uuid = args[1];
        DatabaseUserWrapper duw = getWrapperD();
        boolean r = duw.updateId(username, uuid, senderID);
        if(!r){
            sendMessage(senderID, "The uuid or username you type is invalid.");
            return;
        }
        sendMessage(senderID, "Your messenger account was successfully linked to your ARW account !");
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
