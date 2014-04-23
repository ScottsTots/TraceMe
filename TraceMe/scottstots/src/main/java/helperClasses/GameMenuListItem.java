package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseException;
import com.parse.ParseUser;

import org.ocpsoft.prettytime.PrettyTime;

import scotts.tots.traceme.R;

/**
 * Created by matthewebeweber on 4/5/14.
 */

public class GameMenuListItem {
    private Game game;
    public boolean isDisabled;

    public GameMenuListItem(){
        //Empty Constructor
        this.isDisabled = true;
    }
    public GameMenuListItem(Game game) {
        this.game = game;
        this.isDisabled = false;
    }

    public String getStatusString() {
        if(game == null){
            return "Preparing your data";
        }
        if (game.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
            return "Opponent will be found soon...";
        } else if (game.getInt("game_status") == GameStatus.CHALLENGED.id) {
            // This user is the one being challenged
            if (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return "Waiting for challenge response...";
            else
                return "Challenged: Needs response!";
        } else if (game.getInt("game_status") == GameStatus.IN_PROGRESS.id) {
            // If the game is in progress it is either player one's turn or player two's
            try {
                if (game.getParseUser("player_turn").fetchIfNeeded().getUsername()
                        .equals(ParseUser.getCurrentUser().getUsername())) {
                    return "Your move!";
                } else {
                    isDisabled = true;
                    return "Waiting for their move...";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (game.getInt("game_status") == GameStatus.GAME_OVER.id) {
            // TODO: Add some Game Over magic
            return "Game Over";
        }

        return "";
    }

    public String getUsernameString() {
        if( game == null){
            return "Loading...";
        }
        if (game.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
            return "Awaiting Opponent";
        } else if (game.getInt("game_status") == GameStatus.CHALLENGED.id ||
                game.getInt("game_status") == GameStatus.IN_PROGRESS.id ||
                game.getInt("game_status") == GameStatus.GAME_OVER.id) {
            // This user is the one being challenged
            if (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return UsefulMethods.getParseUsername(game.getParseUser("player_two"));
            else
                return UsefulMethods.getParseUsername(game.getParseUser("player_one"));
        } else {
            return "";
        }
    }

    public Bitmap getGameImage(Context context) {
        if (game == null){
            Bitmap img = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.logo_2_loading);
            return Bitmap.createScaledBitmap(img, 150, 150, false);
        }
        if (game.getInt("game_status") == GameStatus.CHALLENGED.id ||
                game.getInt("game_status") == GameStatus.IN_PROGRESS.id ||
                game.getInt("game_status") == GameStatus.GAME_OVER.id) {
            // This user is the one being challenged
            if (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return UsefulMethods.getParseUserPicture(game.getParseUser("player_two"), context);
            else
                return UsefulMethods.getParseUserPicture(game.getParseUser("player_one"), context);
        } else {
            Bitmap img = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.traceme_logo);
            return Bitmap.createScaledBitmap(img, 150, 150, false);
        }
    }

    public String getLastUpdatedString() {
        if(game == null){ return ""; }

        PrettyTime p = new PrettyTime();
        return p.format(game.getUpdatedAt());
    }

    public Game getGameParseObject() {
        return game;
    }
}
