package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Aaron on 3/23/2014.
 */

/**
 * The Game class will switch and load levels, save the user's path data
 * manage player turns, and communicate with the network.
 */
public class Game {
    public enum  Status{

    }
    public int levelNum = 1;

    public String gameStatus = "running"; // game ends when this is "end"

    public Level level;
    // For singlePlayer
    public Game() {

    }

    public void setLevel(int levelCount, Context ctx) {
        // level, timeLeft, totalTraces, currentTrace are set.
        level = new Level(levelCount, ctx);
    }

    // Called before starting the game. It basically loads the level,
    // and should be called inside an async task.
    public void loadGame(Context ctx) {
        level.loadSinglePlayerLevel(ctx);
    }
}
