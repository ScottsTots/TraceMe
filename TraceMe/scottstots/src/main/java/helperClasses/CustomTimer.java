package helperClasses;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Aaron on 3/31/2014.
 */
public class CustomTimer {

    int seconds;
    Timer timer;
    long start = 0;
    public CustomTimer() {
        timer = new Timer();
        seconds = 100;
    }

    public void start() {

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seconds++;
            }
        }, 0, 1000); //every second it runs.
    }

    public void resetTime() {
        start = System.currentTimeMillis();
        seconds = 0;
    }

    public int getTime() {
        return seconds;
    }

    // Returns time in seconds
    public long getTime2() {
        return (System.currentTimeMillis() - start);
    }
}
