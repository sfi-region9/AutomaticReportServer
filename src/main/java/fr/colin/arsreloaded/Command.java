package fr.colin.arsreloaded;

import java.util.HashMap;

public abstract class Command {

    private String name;
    protected boolean hidden = false;


    public static HashMap<String, Command> commands = new HashMap<>();
    public static HashMap<String, Command> alias = new HashMap<>();

    public Command(String name, String... alias) {
        this.name = name;
        commands.put(name, this);
        for (String s : alias) {
            Command.alias.put(s, this);
        }
    }

    public abstract void onCommand(String senderID, String text, String[] args);

    public abstract String usage();

    public abstract String args();

    public String getName() {
        return name;
    }
}
