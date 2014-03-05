package scotts.tots.traceme;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by matthewebeweber on 3/5/14.
 */
public class TraceMeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("AJDLKSJADLK", "ASLKDJSALKDJSAKLDJLKASJLKASJ");
        Parse.initialize(this, "gTBa2AMXgWvTNIBO1liUk1hisZzUs8CVcn7q6dT4", "4DywYi8KYdM1COhfOt3hNNOssWV4Pq9DeXsO9Xme");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("matt", "matt");
        testObject.saveInBackground();

    }
}
