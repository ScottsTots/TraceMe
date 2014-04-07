package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

import gamescreens.GameLoop;

/**
 * Created by Aaron on 3/23/2014.
 */

/**
 * The Game class will switch and load levels, save the user's path data
 * manage player turns, and communicate with the network.
 */

@ParseClassName("Game")
public class Game extends ParseObject {
    private ParseUser playerOne;
    private ParseUser playerTwo;
    private GameStatus gameStatus;
    private boolean isMultiplayer = true;

    private int levelNum = 1;
    public Level level;
    // For singlePlayer
    public Game() {

    }

    // Called before starting the game. It basically loads the level,
    // and should be called inside an async task.
    public void loadGame(Context ctx) {
        level.loadTrace();
    }


    /** Parse methods **/
    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    // getters
    public ParseUser getPlayerOne() {
        return playerOne;
        //return getParseUser("player_one");
    }

    public ParseUser getPlayerTwo() {
        return playerTwo;
        //return getParseUser("player_two");
    }

    public GameStatus getGameStatus() {
        return gameStatus;
        //return getInt("game_status");
    }

    public int getLevelNum() {
        return levelNum;
       // return getInt("level");
    }

    // setters
    public void setGameStatus(GameStatus status) {
        gameStatus = status;
        put("game_status", status.id);
    }

    public void setPlayerTwo(ParseUser user) {
        playerTwo = user;
        put("player_two", user);
    }

    public void setPlayerOne(ParseUser user) {
        playerOne = user;
        put("player_one", user);
    }

    public void setLevel(int num) {
        levelNum = num;
        put("level", num);
    }

    public void setMultiplayer(boolean b) {
        isMultiplayer = b;
        put("multiplayer", b);

    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

}
