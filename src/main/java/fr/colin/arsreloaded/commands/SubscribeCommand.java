package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.plugins.Command;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.Arrays;

import static fr.colin.arsreloaded.ARSReloaded.*;

@Extension
public class SubscribeCommand extends Command {
    public SubscribeCommand() {
        super("subscribe", "sub", "register", "r");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (args.length < 2) {
            sendMessage(senderID, "Usage : !subscribe" + this.args());
            return;
        }
        String scc;
        String name;
        if (args.length == 2) {
            scc = args[0];
            name = args[1];
        } else {
            scc = args[0];
            String[] names = Arrays.copyOfRange(args, 1, args.length);
            name = StringUtils.join(names, "_");
        }
        try {
            boolean result = getWrapper().addWaiting(senderID, name, scc);
            if (result) {
                sendMessage(senderID, "Your vessel is successfully registered in the database !");
                sendMessage(ADMIN_ID, "You have a new pending registry from " + scc + " for the vessel " + name);
            } else {
                sendMessage(senderID, "Your vessel already exist in the database !");
            }
        } catch (SQLException e) {
            sendMessage(senderID, "An error occured please try again or contact the administrator");
        }

    }

    @Override
    public String usage() {
        return "Register your vessel";
    }

    @Override
    public String args() {
        return " [scc] [vesselname]";
    }
}
