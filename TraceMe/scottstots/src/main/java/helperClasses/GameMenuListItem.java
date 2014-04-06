package helperClasses;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by matthewebeweber on 4/5/14.
 */

public class GameMenuListItem {

    ParseObject gameItem;
    public String username;

    public GameMenuListItem(ParseObject gameItem, String username) {
        this.gameItem = gameItem;
        this.username = username;
    }

    public String getLastUpdatedString() {
        PrettyTime p = new PrettyTime();
        try {
            gameItem.fetchIfNeeded();
            Date d = gameItem.getUpdatedAt();
            Log.d("TIME", d.toString());
            return p.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "No info";
        }
    }
}
