package helperClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
            return getCroppedBitmap(BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length));
        } else {
            Bitmap img = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.traceme_logo);
            return getCroppedBitmap(Bitmap.createScaledBitmap(img, 150, 150, false));
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
