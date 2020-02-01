package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.plugins.Command;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.Arrays;

import static fr.charlotte.arsreloaded.ARSReloaded.*;

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
        String region;
        if (args.length == 3) {
            scc = args[0];
            name = args[2];
            region = args[1];
        } else {
            scc = args[0];
            region = args[1];
            String[] names = Arrays.copyOfRange(args, 2, args.length);
            name = StringUtils.join(names, "_");
        }
        int re;
        try {
            re = Integer.parseInt(region);
        } catch (NumberFormatException e) {
            sendMessage(senderID, "Error, please enter a number for the region");
            return;
        }
        if (re > 99 || re < 0) {
            sendMessage(senderID, "Error, the region must be between 0 and 99");
            return;
        }
        try {
            boolean result = getWrapper().addWaiting(senderID, name, scc, re);
            if (result) {
                sendMessage(senderID, "Your vessel is successfully registered in the database !");
                sendMessage(ADMIN_ID, "You have a new pending registry from " + scc + " for the vessel " + name);
            } else {
                sendMessage(senderID, "Your vessel already exist in the database !");
            }
        } catch (SQLException e) {
            sendMessage(senderID, "An error occurred please try again or contact the administrator");
        }

    }

    @Override
    public String usage() {
        return "Register your vessel";
    }

    @Override
    public String args() {
        return " [scc] [region] [vesselname]";
    }
}
