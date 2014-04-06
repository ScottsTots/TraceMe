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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
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
    private HashMap<String, List<GameMenuListItem>> listDataChild;

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
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<GameMenuListItem>>();

        // Adding child data
        listDataHeader.add("New Game");
        listDataHeader.add("Challenges");
        listDataHeader.add("Current Games");
        listDataHeader.add("Past Games");

        // Adding child data
        final List<GameMenuListItem> challenges = new ArrayList<GameMenuListItem>();

        // Query for this user's current games w/out an opponent
        ParseQuery<ParseObject> awaitingQuery = ParseQuery.getQuery("Game");
        awaitingQuery.orderByDescending("updatedAt");
        awaitingQuery.whereEqualTo("player_one", ParseUser.getCurrentUser());
        awaitingQuery.whereEqualTo("game_status", GameStatus.WAITING_FOR_OPPONENT.id);
        awaitingQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameList, ParseException e) {
                if (e == null) {
                    for (ParseObject game : gameList) {
                        challenges.add(new GameMenuListItem("Waiting for opponent..", game.getUpdatedAt()));
                    }
                    listAdapter.notifyDataSetChanged();
                } else {
                    Log.d("prepareListData", "Error: " + e.getMessage());
                }
            }
        });

        // TODO: Find challenges involving this current user.
        // TODO: Awaiting opponent should have click and hold to cancel


        // TODO: Collapse these two into one query
        final List<GameMenuListItem> currentgames = new ArrayList<GameMenuListItem>();
        // For player one
        ParseQuery<ParseObject> currentGameQuery1 = ParseQuery.getQuery("Game");
        currentGameQuery1.whereEqualTo("player_one", ParseUser.getCurrentUser());
        currentGameQuery1.whereEqualTo("game_status", GameStatus.IN_PROGRESS.id);
        currentGameQuery1.orderByDescending("updatedAt");
        currentGameQuery1.include("_User");
        currentGameQuery1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameList, ParseException e) {
                if (e == null) {
                    for (ParseObject game :  gameList) {
                        ParseUser user = game.getParseUser("player_two");
                        try {
                            user.fetchIfNeeded();
                            currentgames.add(new GameMenuListItem(user.getUsername(), game.getUpdatedAt()));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                } else {
                    Log.d("prepareListData", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery<ParseObject> currentGameQuery2 = ParseQuery.getQuery("Game");
        currentGameQuery2.whereEqualTo("player_two", ParseUser.getCurrentUser());
        currentGameQuery2.whereEqualTo("game_status", GameStatus.IN_PROGRESS.id);
        currentGameQuery2.orderByDescending("updatedAt");
        currentGameQuery2.include("_User");
        currentGameQuery2.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> gameList, ParseException e) {
                if (e == null) {
                    for (ParseObject game :  gameList) {
                        ParseUser user = game.getParseUser("player_one");
                        try {
                            user.fetchIfNeeded();
                            currentgames.add(new GameMenuListItem(user.getUsername(), game.getUpdatedAt()));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                } else {
                    Log.d("prepareListData", "Error: " + e.getMessage());
                }
            }
        });

        List<GameMenuListItem> pastgames = new ArrayList<GameMenuListItem>();

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
