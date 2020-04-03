package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.AutomaticReportServer;
import fr.charlotte.arsreloaded.plugins.Command;
import org.pf4j.Extension;

@Extension
public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
    }

    @Override
    public void onCommand(String senderID, String text, String[] args) {
        System.out.println("Ping Command received from " + senderID);
        AutomaticReportServer.sendMessage(senderID, "Pong !!, Version " + AutomaticReportServer.ARS_VERSION + ", Developed by CMDR Charlotte THOMAS");
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
