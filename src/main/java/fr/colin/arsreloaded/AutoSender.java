package fr.colin.arsreloaded;

import fr.colin.arsreloaded.utils.DatabaseWrapper;

import java.sql.SQLException;
import java.util.Calendar;

import static java.lang.Thread.sleep;

/**
 * Simple runnable class, this class send reports every 30th of each month - except february.
 */

public class AutoSender implements Runnable {

    public DatabaseWrapper db = new DatabaseWrapper();

    public DatabaseWrapper getWrapper() {
        return db;
    }

    public boolean isTimeToSent() {

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        long last = getWrapper().getLast();
        int month = cal.get(Calendar.MONTH);

        int detectDay = 0;

        if (month != 2) {
            detectDay = 28;
        } else {
            detectDay = 30;
        }

        if (detectDay == day && (last + 1000 * 60 * 60 * 24 * 10) < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          //  ARSReloaded.sendMessage(ARSReloaded.ADMIN_ID, "I'm still here");
            System.out.println("Launching Automatic Launching Verification");
            if (isTimeToSent()) {
                System.out.println("Time is now :D !");
                try {
                    getWrapper().sendReports();
                    getWrapper().setLast();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("Reports successfully sends");
                try {
                    sleep(1000 * 3600 * 24 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            //function
            try {
                sleep(1000 * 60 * 60 * 6);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
