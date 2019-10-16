package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.plugins.Command;
import fr.colin.arsreloaded.utils.VesselNotFoundException;
import org.pf4j.Extension;

import java.sql.SQLException;
import java.util.ArrayList;

import static fr.colin.arsreloaded.ARSReloaded.*;

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
            case "show":
                showList(senderID);
                break;
            case "accept":
                accept(senderID, args);
                break;
            case "deny":
                deny(senderID, args);
        }
    }

    private void deny(String senderID, String[] args) {
        if (args.length < 2)
            return;
        String vesselID = args[1];
        String coID;
        try {
            coID = getWrapper().getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            sendMessage(senderID, "An error occured");
            return;
        }
        boolean success = false;
        try {
            success = getWrapper().deletePending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (success) {
            sendMessage(senderID, "You denied the subscribe of " + coID + " CO of the " + vesselID);
            sendMessage(coID, "Your pending subscribe was denied by the administrator :(");
        }
    }

    private void accept(String senderID, String[] args) {
        if (args.length < 2)
            return;
        //CONSTRUCTION !wait accept <vesselid>
        String vesselID = args[1];
        String coID;
        try {
            coID = getWrapper().getPendingCoId(vesselID);
        } catch (SQLException | VesselNotFoundException e) {
            sendMessage(senderID, "An error occured");
            return;
        }
        boolean success = false;
        try {
            success = getWrapper().switchPending(vesselID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (success) {
            sendMessage(senderID, "You accepted the subscribe of " + coID + " CO of the " + vesselID);
            sendMessage(coID, "Your pending subscribe was accepted by the administrator !");
        }
    }

    private void showList(String senderID) {
        ArrayList<String> m = new ArrayList<>();
        try {
            ArrayList<String> pW = getWrapper().getPendingWaiting();
            m.addAll(pW);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (m.isEmpty()) {
            sendMessage(senderID, "Empty ! Good job sir !");
            return;
        }
        sendMultiMessage(senderID, "Pending List", m);
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
