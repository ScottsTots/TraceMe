package scotts.tots.traceme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;
import java.util.List;

import scotts.tots.traceme.R;


public class LoginNewUserFragment extends Fragment {// implements View.OnClickListener {

    static final String TAG = "SignUpActivity";

    Button signUpButton;
    EditText usernameField;
    EditText passwordField;
    EditText passwordAgainField;

    public LoginNewUserFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_new_user, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Connect the UI Elements
        signUpButton = (Button) view.findViewById(R.id.signUpButton);
        usernameField = (EditText) view.findViewById(R.id.signUpUsernameField);
        passwordField = (EditText) view.findViewById(R.id.signUpPasswordField);
        passwordAgainField = (EditText) view.findViewById(R.id.signUpPasswordAgainField);

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

            Toast.makeText(getActivity(),
                    validationErrMsg.toString(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Fields are valid, fire up progress dialog
        final ProgressDialog dlg = new ProgressDialog(getActivity());
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
                    Toast.makeText(getActivity(),
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    // Login successful, go to the MainScreenActivity
                    Toast.makeText(getActivity(),
                            "Sign up successful. Presenting the good stuff.",
                            Toast.LENGTH_LONG).show();

                    Intent i = new Intent(getActivity(), MainScreen.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        });
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