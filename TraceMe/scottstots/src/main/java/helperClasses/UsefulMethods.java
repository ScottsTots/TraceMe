package helperClasses;

import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matthewebeweber on 4/16/14.
 */
public class UsefulMethods {

    // Takes a user and returns there username. Depending on how the user is registered,
    // this will return the username, facebook name, or twitter handle
    public static String getParseUsername(ParseUser user) {
        if (ParseFacebookUtils.isLinked(user)) {
            final JSONObject userProfile = new JSONObject();

            Session session = ParseFacebookUtils.getSession();
            if (session != null && session.isOpened()) {
                Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                        new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(GraphUser user, Response response) {
                                if (user != null) {
                                    try {
                                        userProfile.put("name", user.getName());
                                    } catch (JSONException e) {
                                        Log.d("getUsername",
                                                "Error parsing returned user data.");
                                    }
                                } else if (response.getError() != null) {
                                    Log.d("getUsername", "Error trying to grab facebook information.");
                                }
                            }
                        });
                request.executeAndWait();
            }

            try {
                return userProfile.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return "Your Games";
            }
        } else if (ParseTwitterUtils.isLinked(user)) {
            return "@" + ParseTwitterUtils.getTwitter().getScreenName();
        } else {
            return user.getUsername();
        }
    }
}
