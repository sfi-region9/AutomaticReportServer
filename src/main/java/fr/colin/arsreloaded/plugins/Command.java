package fr.colin.arsreloaded.plugins;

import org.pf4j.ExtensionPoint;

import java.util.HashMap;

/**
 * Abstract class to handle command request from the messenger bot
 */

public abstract class Command implements ExtensionPoint {

    private String name;
    private String[] aliasies;
    protected boolean hidden = false;


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

    public abstract void onCommand(String senderID, String text, String[] args);

    public abstract String usage();

    public abstract String args();

    public boolean isHidden() {
        return hidden;
    }

    public String getName() {
        return name;
    }
}
