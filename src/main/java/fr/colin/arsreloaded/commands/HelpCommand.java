package fr.colin.arsreloaded.commands;

import fr.colin.arsreloaded.ARSReloaded;
import fr.colin.arsreloaded.Command;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        System.out.println("Help Command received from " + senderID);
        ARSReloaded.sendHelp(senderID);
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
