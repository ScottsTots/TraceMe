package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gamescreens.GameLoop;

/**
 * Created by Aaron on 3/23/2014.
 */

/**
 * The Game class will determine which level is loaded, save the user's path data
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

    public static ParseQuery<Game> getQuery() {
        return ParseQuery.getQuery(Game.class);
    }


    public void saveUserDrawings(ArrayList<CustomPath> userPaths) {
        // We will save userPath data in a JSONArray (array of paths)
        // full of JSON arrays (path) full of JSON objects (individual point)
        JSONArray customPathArray = new JSONArray();

        // Iterate through all paths
        for(int i = 0; i < userPaths.size(); i++) {
            JSONArray jsonPath = new JSONArray();
            CustomPath path = userPaths.get(i);
            // for each path
            for(int j = 0; j < path.size(); j++ ) {
                // get each point save it into JSON Object
                DataPoint point = path.get(j);
                JSONObject jsonPoint = new JSONObject();
                try {
                    jsonPoint.put("x", (int)(point.x));
                    jsonPoint.put("y", (int)(point.y));
                    jsonPoint.put("time", point.time); // time is a long
                    jsonPoint.put("score", point.score); //score is a double
                    // Add it to json path array
                    jsonPath.put(j, point);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Add this path to our list of paths
            try {
                jsonPath.put(i, jsonPath);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Add all this json data into the correct player_one_data or player_two_data slot
        if(playerOne.equals(ParseUser.getCurrentUser()))
            put("player_one_data", customPathArray);
        else {
            put("player_two_data", customPathArray);
        }
    }

    /**
     * Gets a player's JSON data from parse, and converts it back to an array of CustomPaths
     * that we can draw.
     * @return
     */
    public ArrayList<CustomPath> getCustomPaths(ParseUser user) {
        ArrayList<CustomPath> pathsArray = new ArrayList<CustomPath>();
        JSONArray jsonPathsArray = new JSONArray();

        // Grab the right json data
        if(user.equals(playerOne)) {
            jsonPathsArray = getJSONArray("player_one_data");
        }
        else if(user.equals(playerTwo)) {
            jsonPathsArray = getJSONArray("player_two_data");
        }

        // decode it into an array of custom paths.
        for(int i = 0; i < jsonPathsArray.length(); i++) {
            // First get an individual path
            CustomPath path = new CustomPath();
            JSONArray jsonPath = new JSONArray();
            try {
                jsonPath = jsonPathsArray.getJSONArray(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Add all points to the CustomPath
            for(int j = 0; j < jsonPath.length(); j++) {
                try {
                    JSONObject jsonPoint = jsonPath.getJSONObject(j);
                    int x = jsonPoint.getInt("x");
                    int y = jsonPoint.getInt("y");
                    long time = jsonPoint.getLong("time");
                    int score = jsonPoint.getInt("score");
                    path.addUserPoint(x, y, time, score);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pathsArray.add(path);
        }
        return pathsArray;
    }

}
