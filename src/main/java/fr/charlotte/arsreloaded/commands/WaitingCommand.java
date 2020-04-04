package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.plugins.Command;
import fr.charlotte.arsreloaded.utils.VesselNotFoundException;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.charlotte.arsreloaded.AutomaticReportServer.*;

@Extension
public class WaitingCommand extends Command {
    public WaitingCommand() {
        super("wait", "wa");
        hidden = true;
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        if (!senderID.equals(ADMIN_ID)) {
            sendMessage(senderID, "You do not have the permission to perform this command.");
            return;
        }
        System.out.println("Wait request received by " + senderID);
        if (args.length < 1) {
            return;
        }
        switch (args[0]) {
            case "accept":
                accept(senderID, args);
                break;
            case "deny":
                deny(senderID, args);
                break;
            default:
                showList(senderID);
                break;
        }
    }

    private void deny(String senderID, String[] args) {
        String vesselID = args[1];
        String coID;
        try {
            coID = getWrapper().getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            sendMessage(senderID, "An error occured");
            return;
        }
        String success = "false";
        try {
            success = getWrapper().deletePending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!(success.equalsIgnoreCase("false"))) {
            sendMessage(senderID, "You denied the subscribe of " + coID + " CO of the " + vesselID);
            sendMessage(coID, "Your pending subscribe was denied by the administrator :(");
            sendCompletedMail("ARS", "We're sorry your pending subscribe has been denied by the administrator, contact them via email", "CO", success);
        }
    }

    private void accept(String senderID, String[] args) {
        String vesselID = args[1];
        String coID;
        try {
            coID = getWrapper().getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            e.printStackTrace();
            sendMessage(senderID, "An error occured");
            return;
        }
        String success = "";
        try {
            success = getWrapper().switchPending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!(success.equalsIgnoreCase("false"))) {
            sendMessage(senderID, "You accepted the subscribe of " + coID + " CO of the " + vesselID);
            sendMessage(coID, "Your pending subscribe was accepted by the administrator !");
            sendCompletedMail("ARS", "Your pending subscribe was accepted by the administrator !", "CO", success);
        }
    }

    private void showList(String senderID) {
        ArrayList<String> message = new ArrayList<>();
        try {
            ArrayList<String> pendingWaiting = getWrapper().getPendingWaiting();
            message.addAll(pendingWaiting);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (message.isEmpty()) {
            sendMessage(senderID, "Empty ! Good job !");
            return;
        }
        sendMultiMessage(senderID, "Pending List", message);
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
