package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parse.ParseUser;

import scotts.tots.traceme.R;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class HomeScreenFrag extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    Button logOutButton;
    Button newGameButton;
    android.app.Dialog dlog;
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
        newGameButton = (Button) view.findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dlog = new android.app.Dialog(getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
                dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dlog.setContentView(R.layout.home_fragment_new_game_dialog);


                //SinglePlayer button
                View singlePlayerButton = dlog.findViewById(R.id.singlePlayer);
                singlePlayerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startSinglePlayer();
                    }
                });
/*
                // Random opponent button
                Button randomPlayerButton = (Button) dlog.findViewById(R.id.);
                randomPlayerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Retrieve a randomOpponent (do this in the game object)  E.g. game.get/setRandomOpponent();
                        ParseUser opponent = getRandomOpponent();
                        startMultiPlayer(opponent);
                    }
                });


                // Challenge a friend button
                Button multiPlayerButton = (Button) dlog.findViewById(R.id.singlePlayer);
                multiPlayerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showFriendPicker();
                        // the startMultiplayer() method gets called inside friendPicker. OR we could set up a handler here
                        // that gets called when we're done choosing a friend.
                    }
                });
*/

                dlog.show();
            }
        });
    }

    // When we start the game, it must be the case that all game components are set to how the user wants them. (time limit, etc)
    // If we want to add new game modes, game content later on, those game modes must be set before reaching this.
    public void startSinglePlayer() {
        dlog.dismiss();
        startActivity(new Intent(getActivity(), GameActivity.class));
    }

    public void startMultiPlayer(ParseUser opponent) {
        dlog.dismiss();
        //startActivity(new Intent(getActivity(), DrawingActivityMultiplayer.class);
    }


    public ParseUser getRandomOpponent() {
        return null;
    }


    public void showFriendPicker() {

    }

    // If we wanted to just make a switch case scenario for all buttons, then we'd implement the onclick listener and do this:
       /* @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.logOutButton:
                    ParseUser.logOut();
                    Intent intent = new Intent(getActivity(), LoginOldUserActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }*/
}