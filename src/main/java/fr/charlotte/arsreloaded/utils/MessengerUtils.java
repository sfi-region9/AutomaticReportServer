package fr.charlotte.arsreloaded.utils;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TextMessage;
import fr.charlotte.arsreloaded.plugins.Command;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;

import java.util.ArrayList;
import java.util.List;

public class MessengerUtils {

    private final Messenger messenger;
    private final Mailer mailer;
    private final String arsMail;
    private final String administratorMail;

    public MessengerUtils(Messenger messenger, Mailer mailer, String arsMail, String administratorMail) {
        this.messenger = messenger;
        this.mailer = mailer;
        this.arsMail = arsMail;
        this.administratorMail = administratorMail;
    }

    /**
     * Method to send the help ( all commands and their usage ) to a user.
     *
     * @param recipientID ID of the recipient.
     */
    public void sendHelp(String recipientID) {
        ArrayList<String> message = new ArrayList<>();

        if (Command.commands.isEmpty()) {
            sendMessage(recipientID, "No commands are registred, please contact the administrator for any further help.");
            return;
        } else {
            message.add("■ Help for Commands, all commands start with '!' character");
            message.add("■ [] => required argument\n" +
                    "■ {} => optional argument");
            message.add("");
            for (Command c : Command.commands.values()) {
                if (!c.isHidden()) {
                    message.add("● &" + c.getName() + c.args() + " ⇒ " + c.usage());
                }
            }
        }
        sendMultiMessage(recipientID, "ARS Help", message);
    }

    /**
     * Method to send a messenger message to a person
     *
     * @param recipientID ID of the recipient of the message
     * @param message     The message
     */
    public void sendMessage(String recipientID, String message) {
        MessagePayload payload = MessagePayload.create(recipientID, MessagingType.RESPONSE, TextMessage.create(message));
        try {
            messenger.send(payload);
        } catch (MessengerApiException | MessengerIOException e) {
            System.out.println("An error occured with message " + message + " with rid " + recipientID + "\n" + e.getMessage());
        }
    }

    /**
     * Method to send an organized message in multi line
     *
     * @param recipientID ID of the recipient of the message
     * @param header      Header in the upper and lower bars
     * @param messages    The message
     */
    public void sendMultiMessage(String recipientID, String header, List<String> messages) {
        ArrayList<String> finalM = new ArrayList<>();
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        finalM.addAll(messages);
        finalM.add(String.format("-------------------------------- %s --------------------------------", header));
        String message = StringUtils.join(finalM, "\n");
        sendMessage(recipientID, message);
    }


    public void sendCompletedMail(String subject, String message, String recipientName, String recipientAdress) {
        mailer.sendMail(EmailBuilder.startingBlank().from("ARS Mail Sender", arsMail).to(recipientName, recipientAdress).withSubject(subject).withPlainText(message).buildEmail());
    }


    public String getAdministratorMail() {
        return administratorMail;
    }
}
