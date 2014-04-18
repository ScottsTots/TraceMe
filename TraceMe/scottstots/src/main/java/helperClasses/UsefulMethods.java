package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import scotts.tots.traceme.R;

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

    // Send a notification with the given String to a user,
    // Double checks to make sure the user is has notifications on
    public static void sendPushNotification(ParseUser user, String notificationStr) {
        // TODO: Send push notification to the user if push notifications is on
    }

    // Given a parse user, return the appropriate Bitmap to be displayed
    public static Bitmap getParseUserPicture(ParseUser user, Context context) {
        byte[] imgBytes = user.getBytes("profile_picture");
        if (imgBytes != null) {
            return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
        } else {
            // TODO: If no picture check, Facebook and Twitter for images
            return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.traceme_logo);
        }
    }
}
