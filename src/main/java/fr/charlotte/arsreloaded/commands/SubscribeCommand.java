package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.Arrays;


@Extension
public class SubscribeCommand extends Command {
    public SubscribeCommand() {
        super("subscribe", "sub", "register", "r");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper) {
        if (args.length < 4) {
            utils.sendMessage(senderID, "Usage : !subscribe" + this.args());
            return;
        }
        String scc;
        String name;
        String region;
        String mail;
        if (args.length == 4) {
            scc = args[0];
            name = args[3];
            region = args[1];
            mail = args[2];
        } else {
            scc = args[0];
            region = args[1];
            mail = args[2];
            String[] names = Arrays.copyOfRange(args, 3, args.length);
            name = StringUtils.join(names, "_");
        }
        int re;
        try {
            re = Integer.parseInt(region);
        } catch (NumberFormatException e) {
            utils.sendMessage(senderID, "Error, please enter a number for the region");
            return;
        }
        if (re > 99 || re < 0) {
            utils.sendMessage(senderID, "Error, the region must be between 0 and 99");
            return;
        }
        try {
            boolean result = wrapper.addWaiting(senderID, name, scc, re, mail);
            if (result) {
                utils.sendMessage(senderID, "Your vessel is successfully registered in the database !");
                utils.sendCompletedMail("New Pending Vessel in ARS", "You have a new pending vessel in ARS!", "Administrator", utils.getAdministratorMail());
            } else {
                utils.sendMessage(senderID, "Your vessel already exist in the database !");
            }
        } catch (SQLException e) {
            utils.sendMessage(senderID, "An error occurred please try again or contact the administrator");
        }

    }

    @Override
    public String usage() {
        return "Register your vessel";
    }

    @Override
    public String args() {
        return " [scc] [region] [mail] [vesselname]";
    }
}
