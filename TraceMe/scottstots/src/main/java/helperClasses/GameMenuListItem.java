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

    private String username;
    private Date lastUpdatedTime;

    public GameMenuListItem(String username, Date lastUpdatedTime) {
        this.username = username;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getUsername() {
        return username;
    }

    public String getLastUpdatedString() {
        PrettyTime p = new PrettyTime();
        return p.format(lastUpdatedTime);
    }
}
