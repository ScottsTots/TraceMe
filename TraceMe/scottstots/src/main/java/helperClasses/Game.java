package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import java.util.ArrayList;

import gamescreens.GameLoop;

/**
 * Created by Aaron on 3/23/2014.
 */

/**
 * The Game class will switch and load levels, save the user's path data
 * manage player turns, and communicate with the network.
 */
public class Game {
    public int levelNum = 1;
    public Level level;
    // For singlePlayer
    public Game() {

    }

    public void setLevel(int levelCount, Context ctx, GameLoop v) {
        // level, timeLeft, totalTraces, currentTrace are set.
        level = new Level(levelCount, ctx, v);
    }

    // Called before starting the game. It basically loads the level,
    // and should be called inside an async task.
    public void loadGame(Context ctx) {
        level.loadSinglePlayerLevel();
    }
}
