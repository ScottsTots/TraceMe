package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scotts.tots.traceme.R;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class HomeScreenFragment extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    public HomeScreenFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment_2, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // get the listview
        expListView = (ExpandableListView) view.findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.expandGroup(1);
        expListView.expandGroup(2);
        expListView.expandGroup(3);

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

//                dlog.show();
//            }
//        });
    }



    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("New Game");
        listDataHeader.add("Challenges");
        listDataHeader.add("Current Games");
        listDataHeader.add("Past Games");

        // Adding child data
        List<String> challenges = new ArrayList<String>();
        challenges.add("Aaron Villapando");
        challenges.add("Matt Ebeweber");
        challenges.add("Niko Lazaris");
        challenges.add("Sai Avala");

        List<String> currentgames = new ArrayList<String>();
        currentgames.add("Mike Scott");
        currentgames.add("William Cook");
        currentgames.add("Greg Plaxton");
        currentgames.add("Pradeep Ravikumar");

        List<String> pastgames = new ArrayList<String>();
        pastgames.add("Glen Downing");
        pastgames.add("Risto Mikkulainen");
        pastgames.add("Alison Norman");
        pastgames.add("Adam Klivans");
        pastgames.add("Mike Scott");

        //New Game Button == listDataHeader.get(0)
        listDataChild.put(listDataHeader.get(1), challenges); // Header, Child data
        listDataChild.put(listDataHeader.get(2), currentgames);
        listDataChild.put(listDataHeader.get(3), pastgames);
    }

    // When we start the game, it must be the case that all game components are set to how the user wants them. (time limit, etc)
    // If we want to add new game modes, game content later on, those game modes must be set before reaching this.
//    public void startSinglePlayer() {
//        dlog.dismiss();
//        startActivity(new Intent(getActivity(), GameActivity.class));
//    }

//    public void startMultiPlayer(ParseUser opponent) {
//        dlog.dismiss();
//        //startActivity(new Intent(getActivity(), DrawingActivityMultiplayer.class);
//    }


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
                    Intent intent = new Intent(getActivity(), LoginScreen.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }*/
}
