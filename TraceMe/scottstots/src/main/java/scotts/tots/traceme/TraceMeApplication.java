package scotts.tots.traceme;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.parse.PushService;
import com.parse.ParseFacebookUtils;

/**
 * Created by matthewebeweber on 3/5/14.
 */
public class TraceMeApplication extends Application {

    private static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();

        // Set up the parse magic
        Parse.initialize(this, "gTBa2AMXgWvTNIBO1liUk1hisZzUs8CVcn7q6dT4", "4DywYi8KYdM1COhfOt3hNNOssWV4Pq9DeXsO9Xme");

        // Enable to receive push
        PushService.setDefaultPushCallback(this, DispatchActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        // Defines permissions
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this line. (testing stuff here..)
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        // Parse Facebook
        ParseFacebookUtils.initialize(getString(R.string.app_id));
        ParseTwitterUtils.initialize("2vmfAXn3w7wEd4EvXwEeow", "RHEerGkxhDBkLVz3JF6eFlAac1JzSIebndD9FOIPhg");

        preferences = getSharedPreferences("scotts.tots.traceme", Context.MODE_PRIVATE);

    }
}
