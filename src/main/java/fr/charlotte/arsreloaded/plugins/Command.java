package fr.charlotte.arsreloaded.plugins;

import fr.charlotte.arsreloaded.databases.DatabaseUserWrapper;
import fr.charlotte.arsreloaded.databases.DatabaseWrapper;
import fr.charlotte.arsreloaded.utils.MessengerUtils;
import org.pf4j.ExtensionPoint;

import java.util.HashMap;

/**
 * Abstract class to handle command request from the messenger bot
 */

public abstract class Command implements ExtensionPoint {

    private String name;
    private String[] aliasies;
    protected boolean hidden = false;
    protected boolean permissionNeeded = false;

    public static HashMap<String, Command> commands = new HashMap<>();
    public static HashMap<String, Command> alias = new HashMap<>();

    public Command(String name, String... alias) {
        this.name = name;
        this.aliasies = alias;
    }

    public void register() {
        commands.put(name, this);
        for (String s : aliasies) {
            Command.alias.put(s, this);
        }
    }

    public void execute(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper, String permission) {
        if (!senderID.equalsIgnoreCase(permission)) {
            utils.sendMessage(senderID, "You have not the permission to perform this command");
            return;
        }
        onCommand(senderID, text, args, wrapper, utils, userWrapper);
    }

    public abstract void onCommand(String senderID, String text, String[] args, DatabaseWrapper wrapper, MessengerUtils utils, DatabaseUserWrapper userWrapper);


    public abstract String usage();

    public abstract String args();


    public boolean isHidden() {
        return hidden;
    }

    public String getName() {
        return name;
    }
}
