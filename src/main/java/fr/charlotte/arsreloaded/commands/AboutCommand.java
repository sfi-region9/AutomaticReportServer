package fr.charlotte.arsreloaded.commands;

import fr.charlotte.arsreloaded.plugins.Command;
import org.pf4j.Extension;

import java.io.IOException;
import java.util.ArrayList;

import static fr.charlotte.arsreloaded.ARSReloaded.sendMultiMessage;

@Extension
public class AboutCommand extends Command {

    public AboutCommand() {
        super("about");
    }


    public static void main(String[] args) throws IOException {
        System.getenv().keySet().stream().filter(s -> s.contains("EPSILON_VERSION")).forEach(System.out::println);

        for(String s : System.getenv().keySet()){
            if(s.contains("EPSILON_VERSION")){
                System.out.println(s);
            }
        }

    }



    @Override
    public void onCommand(String senderID, String text, String[] args) {
        ArrayList<String> message = new ArrayList<>();
        message.add(" ");
        message.add("Automatize all your report in your chapter !");
        message.add("Send a message to Charlotte THOMAS, a mail to contact@nwa2coco.fr for any infos !");
        message.add("Developed with ❤️ by Charlotte THOMAS");
        message.add("Maintained and Hosted by USS Versailles, R9");
        message.add(" ");
        sendMultiMessage(senderID, "Automatic Report Server", message);
    }

    @Override
    public String usage() {
        return "About";
    }

    @Override
    public String args() {
        return "";
    }
}
