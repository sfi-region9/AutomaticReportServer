import at.favre.lib.crypto.bcrypt.BCrypt;
import fr.colin.arsext.ARA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("127.0.0.1", 12345);
        BufferedReader sd = ARA.readerFromInput(s.getInputStream());
        PrintStream out = new PrintStream(s.getOutputStream());

        out.println("LOG}_}7of9}_}TEST");
        String lig;
        while ((lig = sd.readLine()) != null) {
            System.out.println(lig);
        }
        s.close();


    }


    public static String readAllBytes(InputStream s) throws IOException {
        byte[] b = new byte[1000];
        int bitRecus = s.read(b);
        if (bitRecus > 0) {
            return new String(b, 0, bitRecus);
        }
        return "";
    }

}
