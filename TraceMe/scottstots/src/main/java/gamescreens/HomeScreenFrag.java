package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parse.ParseUser;

import scotts.tots.traceme.R;
import scotts.tots.traceme.SignUpOrLogInActivity;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class HomeScreenFrag extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    Button logOutButton;
    public HomeScreenFrag() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        logOutButton = (Button) view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(getActivity(), SignUpOrLogInActivity.class);
                startActivity(intent);
            }
        });
    }





    // If we wanted to just make a switch case scenario for all buttons, then we'd implement the onclick listener and do this:
       /* @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.logOutButton:
                    ParseUser.logOut();
                    Intent intent = new Intent(getActivity(), SignUpOrLogInActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }*/
}