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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Activity which displays a registration screen to the user.
 * Based on the Parse "Anywall App" tutorial
 */
public class SignUpOrLogInActivity extends Activity {

    // UI references.
    private EditText usernameView;
    private EditText passwordView;
    private EditText passwordAgainView;
    private ImageView validFormDrawable;

    private boolean usernameValid;
    Button logInButton;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_or_log_in);
        usernameValid = false;

        // Set up the signup form
        signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpButton.setEnabled(false);

        // Set up the login button
        logInButton = (Button) findViewById(R.id.logInButton);

        validFormDrawable = (ImageView) findViewById(R.id.validFormDrawable);
        usernameView = (EditText) findViewById(R.id.usernameField);
        passwordView = (EditText) findViewById(R.id.passwordField);
        // passwordAgainView = (EditText) findViewById(R.id.passwordAgain);

        usernameView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                final String text = s.toString();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", s.toString());
                Log.d("parseNetwork", "verifying username availability");
                query.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if (e != null && text.length() > 0) { // username available, so set green symbol
                            validFormDrawable.setImageDrawable(getResources().getDrawable(
                                    R.drawable.valid_form_green));
                            usernameValid = true;
                            signUpButton.setEnabled(true);
                            logInButton.setEnabled(false);

                        } else {
                            usernameValid = false;
                            signUpButton.setEnabled(false);
                            logInButton.setEnabled(true);
                            validFormDrawable.setImageDrawable(getResources().getDrawable(
                                    R.drawable.valid_form_red));
                        }
                    }
                });
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Set up the sign in button click handler
        logInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Validate the log in data
                boolean validationError = false;
                StringBuilder validationErrorMessage =
                        new StringBuilder("Error logging in.");

                // Check the username textview for data
                if (isEmpty(usernameView)) {
                    validationError = true;
                    validationErrorMessage.append("Username field was blank. Idiot.");
                }

                // Check the password textview for data
                if (isEmpty(passwordView)) {
                    if (validationError)
                        validationErrorMessage.append("Password field blank too. Dummy.");
                    else
                        validationError = true;
                    validationErrorMessage.append("Password field left blank. Idiot");
                }

                // If there is an error at this point display it and return
                if (validationError) {
                    Toast.makeText(SignUpOrLogInActivity.this,
                            validationErrorMessage.toString(),
                            Toast.LENGTH_LONG).show();
                    return;
                }


                // Logging in waiting dialog
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
                                    // Fire up an intent for the next activity
                                    Intent i = new Intent(SignUpOrLogInActivity.this, MainScreen.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
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
                    return;
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
                        if (e != null) {
                            // Show the error message
                            Toast.makeText(SignUpOrLogInActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            // Start an intent for the dispatch activity
                            Intent intent = new Intent(SignUpOrLogInActivity.this, MainScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isMatching(EditText etText1, EditText etText2) {
        if (etText1.getText().toString().equals(etText2.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }
}
