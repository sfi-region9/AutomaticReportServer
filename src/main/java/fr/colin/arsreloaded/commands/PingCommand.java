package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.Command;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        System.out.println("Ping Command received from " + senderID);
        ARSReloaded.sendMessage(senderID, "Pong !!, Version 1.0, Developed by Lieutenant Colin THOMAS");
    }

    @Override
    public String usage() {
        return "pong !";
    }

    @Override
    public String args() {
        return "";
    }
}
