package scotts.tots.traceme;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.PushService;

/**
 * Created by matthewebeweber on 3/5/14.
 */
public class TraceMeApplication extends Application {

    private static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("AJDLKSJADLK", "ASLKDJSALKDJSAKLDJLKASJLKASJ");
        Parse.initialize(this, "gTBa2AMXgWvTNIBO1liUk1hisZzUs8CVcn7q6dT4", "4DywYi8KYdM1COhfOt3hNNOssWV4Pq9DeXsO9Xme");

//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("matt", "matt");
//        testObject.saveInBackground();

        // Enable to receive push
        PushService.setDefaultPushCallback(this, DispatchActivity.class);

        // Defines permissions
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this line. (testing stuff here..)
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        preferences = getSharedPreferences("scotts.tots.traceme", Context.MODE_PRIVATE);

    }
}
