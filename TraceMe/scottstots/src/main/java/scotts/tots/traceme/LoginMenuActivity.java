package scotts.tots.traceme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;


public class LoginMenuActivity extends Activity {

    static final String TAG = "LoginMenuActivity";

    Button signUpButton;
    Button loginButton;
    Button facebookButton;
    Button twitterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);

        signUpButton    = (Button) findViewById(R.id.sign_up_button);
        loginButton     = (Button) findViewById(R.id.login_in_button);
        facebookButton  = (Button) findViewById(R.id.facebook_button);
        twitterButton   = (Button) findViewById(R.id.twitter_button);


        signUpButton.setOnClickListener(buttonListener);
        loginButton.setOnClickListener(buttonListener);
        facebookButton.setOnClickListener(facebookButtonListener);
        twitterButton.setOnClickListener(twitterButtonListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMainScreenActivity() {
        Intent intent = new Intent(this, MainScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(LoginMenuActivity.this, LoginNewUserActivity.class);
            startActivity(i);
        }
    };

    View.OnClickListener facebookButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loginWithFacebook();
        }
    };

    View.OnClickListener twitterButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loginWithTwitter();
        }
    };

    public void loginWithFacebook() {
        Log.d(TAG, "Attempting to login w/ Facebook.");

        // Start Progress Dialog
        final ProgressDialog dlg = new ProgressDialog(LoginMenuActivity.this);
        dlg.setTitle("Please wait..");
        dlg.setMessage("Attempting to login with Facebook.");
        dlg.show();

        // Attempt to log into FB
        List<String> permissions = Arrays.asList("basic_info", "user_about_me",
                "user_relationships", "user_birthday", "user_location");

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                dlg.dismiss();              // Done with login process

                if (user == null) {         // Error logging in
                    Toast.makeText(LoginMenuActivity.this,
                            "Unable to log in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (user.isNew()) {  // New user
                    Toast.makeText(LoginMenuActivity.this,
                            "New user created through Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                } else {                    // Existing user
                    Toast.makeText(LoginMenuActivity.this,
                            "User logged in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                }
            }
        });
    }

    public void loginWithTwitter() {
        Log.d(TAG, "Attempting to login w/ Twitter.");


        // Start progress dialog
        final ProgressDialog dlg = new ProgressDialog(LoginMenuActivity.this);
        dlg.setTitle("Please wait..");
        dlg.setMessage("Attempting to login with Twitter.");
        dlg.show();


        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                dlg.dismiss();                  // Dismiss dialog

                if (parseUser == null) {        // Error logging in
                    Toast.makeText(LoginMenuActivity.this,
                            "Uh oh. The user cancelled the Twitter login.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (parseUser.isNew()) { // New user
                    Toast.makeText(LoginMenuActivity.this,
                            "New user logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                } else {                        // Existing user
                    Toast.makeText(LoginMenuActivity.this,
                            "User logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
}
