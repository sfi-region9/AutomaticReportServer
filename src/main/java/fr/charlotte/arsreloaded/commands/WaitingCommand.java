package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import fr.charlotte.arsreloaded.utils.VesselNotFoundException;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;


@Extension
public class WaitingCommand extends Command {
    public WaitingCommand() {
        super("wait", "wa");
        hidden = true;
        permissionNeeded = true;
    }

    @Override
    public void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils messengerUtils, DatabaseUserWrapper userWrapper) {
        System.out.println("Wait request received by " + senderID);
        if (args.length < 1) {
            return;
        }
        switch (args[0]) {
            case "accept":
                accept(senderID, args, messengerUtils, wrapper);
                break;
            case "deny":
                deny(senderID, args, wrapper, messengerUtils);
                break;
            default:
                showList(senderID, wrapper, messengerUtils);
                break;
        }
    }

    private void deny(String senderID, String[] args, DatabaseWrapper wrapper, MessengerUtils utils) {
        String vesselID = args[1];
        String coID;
        try {
            coID = wrapper.getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            utils.sendMessage(senderID, "An error occured");
            return;
        }
        String success = "false";
        try {
            success = wrapper.deletePending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!(success.equalsIgnoreCase("false"))) {
            utils.sendMessage(senderID, "You denied the subscribe of " + coID + " CO of the " + vesselID);
            utils.sendMessage(coID, "Your pending subscribe was denied by the administrator :(");
            utils.sendCompletedMail("ARS", "We're sorry your pending subscribe has been denied by the administrator, contact them via email", "CO", success);
        }
    }

    private void accept(String senderID, String[] args, MessengerUtils utils, DatabaseWrapper wrapper) {
        String vesselID = args[1];
        String coID;
        try {
            coID = wrapper.getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            e.printStackTrace();
            utils.sendMessage(senderID, "An error occured");
            return;
        }
        String success = "";
        try {
            success = wrapper.switchPending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!(success.equalsIgnoreCase("false"))) {
            utils.sendMessage(senderID, "You accepted the subscribe of " + coID + " CO of the " + vesselID);
            utils.sendMessage(coID, "Your pending subscribe was accepted by the administrator !");
            utils.sendCompletedMail("ARS", "Your pending subscribe was accepted by the administrator !", "CO", success);
        }
    }

    private void showList(String senderID, DatabaseWrapper wrapper, MessengerUtils utils) {
        ArrayList<String> message = new ArrayList<>();
        try {
            ArrayList<String> pendingWaiting = wrapper.getPendingWaiting();
            message.addAll(pendingWaiting);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (message.isEmpty()) {
            utils.sendMessage(senderID, "Empty ! Good job !");
            return;
        }
        utils.sendMultiMessage(senderID, "Pending List", message);
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public String args() {
        return null;
    }
}
