package scotts.tots.traceme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


import java.util.Arrays;
import java.util.List;


/**
 * SignUpOrLoginActivity --
 * Displays the login screen for the application. General flow involves username and
 * password field, FB sign(in|up), and Twitter sign(in|up). Also allows user registration.
 *
 * TL;DR: This activity presents the MainScreenActivity after user authentication.
 *
 */

public class SignUpOrLogInActivity extends Activity {

    static final String TAG = "SignUpOrLogInActivity";

    // UI References
    private EditText usernameView;
    private EditText passwordView;
    private EditText passwordAgainView;
    private ImageView validFormDrawable;

    private boolean usernameValid;  // Tests username for validity

    // Buttons on the screen
    Button logInButton;
    Button signUpButton;
    Button facebookButton;
    Button twitterButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sign_up_or_log_in);

        usernameValid = false;      // Beginning username (empty) isn't valid

        // Connect SignUp Button
        signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpButton.setEnabled(false);

        // Connect LogIn Button
        logInButton = (Button) findViewById(R.id.logInButton);
        logInButton.setEnabled(false);

        // Connect Twitter/Facebook Buttons
        facebookButton = (Button) findViewById(R.id.facebookButton);
        twitterButton  = (Button) findViewById(R.id.twitterButton);

        // Connect EditText Views and Validation Img
        validFormDrawable = (ImageView) findViewById(R.id.validFormDrawable);
        usernameView = (EditText) findViewById(R.id.usernameField);
        passwordView = (EditText) findViewById(R.id.passwordField);
        // passwordAgainView = (EditText) findViewById(R.id.passwordAgain);


        // Username Text Change Listener
        usernameView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                final String text = s.toString();

                // Query for a username with that name
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", s.toString());
                Log.d("parseNetwork", "verifying username availability");
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if (e != null && text.length() > 0) {       // Username Available
                            validFormDrawable.setImageDrawable(getResources().getDrawable(
                                    R.drawable.valid_form_green));
                            usernameValid = true;
                            signUpButton.setEnabled(true);
                            logInButton.setEnabled(false);

                        } else {                                    // Username Taken
                            validFormDrawable.setImageDrawable(getResources().getDrawable(
                                    R.drawable.valid_form_red));
                            usernameValid = false;
                            signUpButton.setEnabled(false);
                            logInButton.setEnabled(true);
                        }
                    }
                });
            }

            // Must implement these methods
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Login Button ClickHandler
        logInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean validationError = false;

                // Initialize error msg
                StringBuilder validationErrorMessage =
                        new StringBuilder("Error logging in.");

                // Check username text input
                if (isEmpty(usernameView)) {
                    validationError = true;
                    validationErrorMessage.append("Username field was blank. Idiot.");
                }

                // Check password for input
                if (isEmpty(passwordView)) {
                    if (validationError) {
                        validationErrorMessage.append("Password field blank too. Dummy.");
                    } else {
                        validationError = true;
                        validationErrorMessage.append("Password field left blank. Idiot");
                    }
                }

                // Display error and terminate here if validationError
                if (validationError) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            validationErrorMessage.toString(),
                            Toast.LENGTH_LONG).show();
                    return; // No reason to attempt to login
                }


                // LogIn Process Dialog
                final ProgressDialog dlg = new ProgressDialog(SignUpOrLogInActivity.this);
                dlg.setTitle("Please wait..");
                dlg.setMessage("Logging in..");
                dlg.show();

                // Attempt to log in through Parse
                ParseUser.logInInBackground(usernameView.getText().toString(),
                        passwordView.getText().toString(),
                        new LogInCallback() {
                            @Override
                            public void done(ParseUser parseUser, ParseException e) {
                                dlg.dismiss();      // Get rid of the loading dlg
                                if (e != null) {    // Uh oh! Login messed up.
                                    Toast.makeText(SignUpOrLogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    showMainScreenActivity();
                                }
                            }
                        });

            }
        }
        );


        // Set up the submit button click handler
        signUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("parseNetwork", "user: " + usernameView.toString());

                // Validate the sign up data
                boolean validationError = false;

                StringBuilder validationErrorMessage = new StringBuilder(getResources().getString(
                        R.string.error_intro));
                if (isEmpty(usernameView)) {
                    validationError = true;
                    validationErrorMessage.append(getResources().getString(
                            R.string.error_blank_username));
                }

                // Error if username was not valid
                if (!usernameValid) {
                    validationError = true;
                    validationErrorMessage = new StringBuilder(getResources().getString(
                            R.string.error_username_taken));
                }

                if (isEmpty(passwordView)) {
                    if (validationError) {
                        validationErrorMessage
                                .append(getResources().getString(R.string.error_join));
                    }
                    validationError = true;
                    validationErrorMessage.append(getResources().getString(
                            R.string.error_blank_password));
                }
             /*   if (!isMatching(passwordView, passwordAgainView)) {
                    if (validationError) {
                        validationErrorMessage
                                .append(getResources().getString(R.string.error_join));
                    }
                    validationError = true;
                    validationErrorMessage.append(getResources().getString(
                            R.string.error_mismatched_passwords));
                }*/
                validationErrorMessage.append(getResources().getString(R.string.error_end));

                // If there is a validation error, display the error
                if (validationError) {
                    Log.d("parseNetwork",   "Validation Error!");
                    Toast.makeText(SignUpOrLogInActivity.this, validationErrorMessage.toString(),
                            Toast.LENGTH_LONG).show();
                    return;     // Return early -> error
                }

                // Set up a progress dialog
                final ProgressDialog dlg = new ProgressDialog(SignUpOrLogInActivity.this);
                dlg.setTitle("Please wait.");
                dlg.setMessage("Signing up.  Please wait.");
                dlg.show();

                // Set up a new Parse user
                ParseUser user = new ParseUser();
                user.setUsername(usernameView.getText().toString());
                user.setPassword(passwordView.getText().toString());

                // Call the Parse signup method
                user.signUpInBackground(new SignUpCallback() {

                    @Override
                    public void done(ParseException e) {
                        dlg.dismiss();
                        if (e != null) { // Show the error message
                            Toast.makeText(SignUpOrLogInActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            showMainScreenActivity();
                        }
                    }
                });
            }
        });

        // Set up the facebook button click event
        facebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                loginWithFacebook();
            }
        });

        // Set up the twitter button click event
        twitterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                loginWithTwitter();
            }
        });
    }

    public void loginWithFacebook() {
        final ProgressDialog dlg = new ProgressDialog(SignUpOrLogInActivity.this);
        dlg.setTitle("Please wait..");
        dlg.setMessage("Logging in..");
        dlg.show();

        List<String> permissions = Arrays.asList("basic_info", "user_about_me",
                "user_relationships", "user_birthday", "user_location");

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                dlg.dismiss();

                if (user == null) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "Unable to log in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (user.isNew()) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "New user created through Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                } else {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "User logged in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                }
            }
        });
    }

    public void loginWithTwitter() {
        final ProgressDialog dlg = new ProgressDialog(SignUpOrLogInActivity.this);
        dlg.setTitle("Please wait..");
        dlg.setMessage("Logging in..");
        dlg.show();


        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                dlg.dismiss();

                if (parseUser == null) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "Uh oh. The user cancelled the Twitter login.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (parseUser.isNew()) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "User signed up and logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                } else {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            "User logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    showMainScreenActivity();
                }
            }
        });
    }

    // Helper method that presents MainScreen
    private void showMainScreenActivity() {
        Intent i = new Intent(this, MainScreen.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    // Helper method that checks for empty EditText field
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    // Helper method that tests 2 EditText fields for equality
    private boolean isMatching(EditText etText1, EditText etText2) {
        if (etText1.getText().toString().equals(etText2.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
}
