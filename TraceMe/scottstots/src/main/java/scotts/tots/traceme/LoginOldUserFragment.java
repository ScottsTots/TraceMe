package scotts.tots.traceme;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import scotts.tots.traceme.R;



public class LoginOldUserFragment extends Fragment {// implements View.OnClickListener {
    static final String TAG = "LoginOldUserFragment";

    private EditText usernameView;
    private EditText passwordView;

    private EditText passwordAgainView;
    private boolean usernameValid;
    private Button logInButton;

    public LoginOldUserFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_old_user, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        usernameValid = false;

        // Set up the login button
        logInButton = (Button) view.findViewById(R.id.logInButton);

        usernameView = (EditText) view.findViewById(R.id.usernameField);
        passwordView = (EditText) view.findViewById(R.id.passwordField);

        // Set up the sign in button click handler
        logInButton.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.logInButton:
                    loginUser();
                    break;
                default:
                    Log.d(TAG, "Strange button pressed. Don't know which.");
            }
        }
    };


    public void loginUser(){
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
            Toast.makeText(getActivity(),
                    validationErrorMessage.toString(),
                    Toast.LENGTH_LONG).show();
            return;
        }


        // Logging in waiting dialog
        final ProgressDialog dlg = new ProgressDialog(getActivity());
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
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            // Fire up an intent for the next activity
                            Intent i = new Intent(getActivity(), MainScreen.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
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

}