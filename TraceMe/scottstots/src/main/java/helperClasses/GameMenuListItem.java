package helperClasses;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by matthewebeweber on 4/5/14.
 */

public class GameMenuListItem {
    private ParseObject game;
    public GameMenuListItem(ParseObject game) {
        this.game = game;
    }

    public String getStatusString() {
        if (game.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
            return "Waiting for opponent..";
        } else if (game.getInt("game_status") == GameStatus.CHALLENGED.id) {
            // This user is the one being challenged
            if (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return "Challenged by " + game.getParseUser("player_two").getUsername();
            else
                return "Waiting for " + game.getParseUser("player_one").getUsername();
        } else if (game.getInt("game_status") == GameStatus.IN_PROGRESS.id) {
            ParseUser opponent = (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername())) ? game.getParseUser("player_two") : game.getParseUser("player_one");
            return opponent.getUsername();
        } else if (game.getInt("game_status") == GameStatus.GAME_OVER.id) {
            // TODO: Add some Game Over magic
            return "Game Over";
        } else {
            return "";
        }
    }

    public String getLastUpdatedString() {
        PrettyTime p = new PrettyTime();
        return p.format(game.getUpdatedAt());
    }
}
