package scotts.tots.levelcreator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends Activity {

    static final String TAG = "SignUpActivity";

    Button signUpButton;
    EditText usernameField;
    EditText passwordField;
    EditText passwordAgainField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Connect the UI Elements
        signUpButton = (Button) findViewById(R.id.signUpButton);
        usernameField = (EditText) findViewById(R.id.signUpUsernameField);
        passwordField = (EditText) findViewById(R.id.signUpPasswordField);
        passwordAgainField = (EditText) findViewById(R.id.signUpPasswordAgainField);

        // Hook up the Click Listener(s)
        signUpButton.setOnClickListener(buttonListener);

    }

    View.OnClickListener buttonListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.signUpButton:
                    signUpUser();
                    break;
                default:
                    Log.d(TAG, "Strange button pressed. Don't know which.");
            }
        }
    };

    public void signUpUser() {
        Log.d(TAG, "Sign up sequence initiated.");

        // Validate the SignUp data
        boolean validationErr = false;
        StringBuilder validationErrMsg = new StringBuilder(getResources().getString(R.string.error_intro));

        if (isEmpty(usernameField)) {
            validationErr = true;
            validationErrMsg.append(getResources().getString(R.string.error_blank_username));
        }

        if (isEmpty(passwordField)) {
            if (validationErr) {
                validationErrMsg.append(getResources().getString(R.string.error_join));
            }
            validationErr = true;
            validationErrMsg.append(getResources().getString(R.string.error_blank_password));
        }

        if (isEmpty(passwordAgainField)) {
            validationErr = true;
            validationErrMsg.append(". Second password field can't be blank. Dummy.");
        }

        if (!isMatching(passwordField, passwordAgainField)) {
            if (validationErr) {
                validationErrMsg.append(getResources().getString(R.string.error_join));
            }

            validationErr = true;
            validationErrMsg.append(getResources().getString(R.string.error_mismatched_passwords));
        }

        validationErrMsg.append(getResources().getString(R.string.error_end));

        // If an error validation msg, display it and return
        if (validationErr) {
            Log.d(TAG, "Error attempting to sign up");

            Toast.makeText(SignUpActivity.this,
                    validationErrMsg.toString(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Fields are valid, fire up progress dialog
        final ProgressDialog dlg = new ProgressDialog(SignUpActivity.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Attempting to create a new user.");
        dlg.show();

        // Create the new parse user
        ParseUser user = new ParseUser();
        user.setUsername(usernameField.getText().toString());
        user.setPassword(passwordField.getText().toString());

        // Try to sign up the user
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dlg.dismiss();

                if (e != null) {        // Error signing up, display the error msg
                    Toast.makeText(SignUpActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    // Login successful, go to the MainScreenActivity
                    Toast.makeText(SignUpActivity.this,
                            "Sign up successful. Presenting the good stuff.",
                            Toast.LENGTH_LONG).show();

                    Intent i = new Intent(SignUpActivity.this, MainScreen.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
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


    // Copied over from LoginNewUser. Helper methods for EditText fields.
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
