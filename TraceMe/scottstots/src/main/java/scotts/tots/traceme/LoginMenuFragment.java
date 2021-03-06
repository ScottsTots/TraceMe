package scotts.tots.traceme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;



public class LoginMenuFragment extends Fragment {// implements View.OnClickListener {

    static final String TAG = "LoginMenuFragment";

    Button signUpButton;
    Button loginButton;
    Button facebookButton;
    Button twitterButton;

    public LoginMenuFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_menu, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        signUpButton    = (Button) view.findViewById(R.id.sign_up_button);
        loginButton     = (Button) view.findViewById(R.id.login_button);
        facebookButton  = (Button) view.findViewById(R.id.facebook_button);
        twitterButton   = (Button) view.findViewById(R.id.twitter_button);


        signUpButton.setOnClickListener(buttonListener);
        loginButton.setOnClickListener(buttonListener);
        facebookButton.setOnClickListener(buttonListener);
        twitterButton.setOnClickListener(buttonListener);
    }

    private void showMainScreenActivity() {
        Intent intent = new Intent(getActivity(), MainScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.facebook_button:
                    loginWithFacebook();
                    break;
                case R.id.twitter_button:
                    loginWithTwitter();
                    break;
                case R.id.login_button:
                    Fragment fragment_old_user = new LoginOldUserFragment();
                    String nTag = fragment_old_user.getTag(); // instance method of a to get a tag

                    FragmentTransaction nFragmentTransaction = getFragmentManager().beginTransaction();

                    nFragmentTransaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                    nFragmentTransaction.replace(R.id.login_frame, fragment_old_user);
                    nFragmentTransaction.addToBackStack(nTag);
                    nFragmentTransaction.commit();
                    break;
                case R.id.sign_up_button:
                    Fragment fragment_new_user = new LoginNewUserFragment();
                    String mTag = fragment_new_user.getTag(); // instance method of a to get a tag

                    FragmentTransaction mFragmentTransaction = getFragmentManager().beginTransaction();

                    mFragmentTransaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                    mFragmentTransaction.replace(R.id.login_frame, fragment_new_user);
                    mFragmentTransaction.addToBackStack(mTag);
                    mFragmentTransaction.commit();
                    break;
            }
        }
    };

    public void loginWithFacebook() {
        Log.d(TAG, "Attempting to login w/ Facebook.");

        // Start Progress Dialog
        final ProgressDialog dlg = new ProgressDialog(getActivity());
        dlg.setTitle("Please wait..");
        dlg.setMessage("Attempting to login with Facebook.");
        dlg.show();

        // Attempt to log into FB
        List<String> permissions = Arrays.asList("basic_info", "user_about_me",
                "user_relationships", "user_birthday", "user_location");

        ParseFacebookUtils.logIn(permissions, getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                dlg.dismiss();              // Done with login process

                if (user == null) {         // Error logging in
                    Toast.makeText(getActivity(),
                            "Unable to log in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (user.isNew()) {  // New user
                    Toast.makeText(getActivity(),
                            "New user created through Facebook.",
                            Toast.LENGTH_LONG
                    ).show();
                    saveUserInstallationInfo();
                    showMainScreenActivity();
                } else {                    // Existing user
                    Toast.makeText(getActivity(),
                            "User logged in with Facebook.",
                            Toast.LENGTH_LONG
                    ).show();

                    saveUserInstallationInfo();
                    showMainScreenActivity();
                }
            }
        });
    }

    public static void saveUserInstallationInfo() {
        // Associate the device with a user
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user",ParseUser.getCurrentUser());
        installation.saveInBackground();
    }

    public void loginWithTwitter() {
        Log.d(TAG, "Attempting to login w/ Twitter.");


        // Start progress dialog
        final ProgressDialog dlg = new ProgressDialog(getActivity());
        dlg.setTitle("Please wait..");
        dlg.setMessage("Attempting to login with Twitter.");
        dlg.show();


        ParseTwitterUtils.logIn(getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                dlg.dismiss();                  // Dismiss dialog

                if (parseUser == null) {        // Error logging in
                    Toast.makeText(getActivity(),
                            "Uh oh. The user cancelled the Twitter login.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (parseUser.isNew()) { // New user
                    Toast.makeText(getActivity(),
                            "New user logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    saveUserInstallationInfo();
                    showMainScreenActivity();
                } else {                        // Existing user
                    Toast.makeText(getActivity(),
                            "User logged in through Twitter!",
                            Toast.LENGTH_LONG
                    ).show();
                    saveUserInstallationInfo();
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