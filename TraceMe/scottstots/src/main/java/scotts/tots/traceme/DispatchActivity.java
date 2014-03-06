package scotts.tots.traceme;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class DispatchActivity extends Activity {
    // Application can only manipulate one Game object at time.
   // public static Game game;


    public DispatchActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // track app usage
        ParseAnalytics.trackAppOpened(getIntent());


        // Check if there is current user info
        if (ParseUser.getCurrentUser() != null) {

            // Start an intent for the logged in activity
            Log.d("parseNetwork", "user" + ParseUser.getCurrentUser().getUsername());
           // startActivity(new Intent(this, MainMenuActivity.class));
            finish();

        } else {
            // Start and intent for the logged out activity
            startActivity(new Intent(this, SignUpOrLogInActivity.class));
        }
    }

}
