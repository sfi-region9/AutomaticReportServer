package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.plugins.Command;
import org.pf4j.Extension;

@Extension
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        System.out.println("Help Command received from " + senderID);
        AutomaticReportServer.sendHelp(senderID);
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
