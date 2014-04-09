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
import com.parse.ParseQueryAdapter;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.DispatchActivity;
import scotts.tots.traceme.R;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class HomeScreenFragment extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";
    public static Game game;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<GameMenuListItem>> listDataChild;
    private RefreshThread refreshThread;

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
        refreshThread = new RefreshThread();
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

//        //noinspection ConstantConditions
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                while(true) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("parseNetwork", "refreshing views");
//                    listAdapter.notifyDataSetChanged();
//                }
//
//            }
//        });

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
        final List<GameMenuListItem> currentgames = new ArrayList<GameMenuListItem>();
        final List<GameMenuListItem> pastgames = new ArrayList<GameMenuListItem>();

        // TODO: Find challenges involving this current user.
        // TODO: Awaiting opponent should have click and hold to cancel

        ParseQuery<ParseObject> currentGameQuery1 = ParseQuery.getQuery("Game");
        currentGameQuery1.whereEqualTo("player_one", ParseUser.getCurrentUser());

        ParseQuery<ParseObject> currentGameQuery2 = ParseQuery.getQuery("Game");
        currentGameQuery2.whereEqualTo("player_two", ParseUser.getCurrentUser());

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(currentGameQuery1);
        queries.add(currentGameQuery2);

        ParseQuery<ParseObject> combinedQuery = ParseQuery.or(queries);
        combinedQuery.orderByDescending("updatedAt");
        combinedQuery.include("player_one");
        combinedQuery.include("player_two");

        combinedQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {    // Successful query
                    for (ParseObject game : parseObjects) {
                        // TODO: Make this a switch statement instead. Tried, but got error so come back.

                        if (game.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {         // Waiting on opponent
                            challenges.add(new GameMenuListItem("Awaiting opponent..", game.getUpdatedAt()));
                        } else if (game.getInt("game_status") == GameStatus.IN_PROGRESS.id) {           // Game currently in progress
                            ParseUser opponent = (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername())) ? game.getParseUser("player_two") : game.getParseUser("player_one");
                            currentgames.add(new GameMenuListItem("Game with " + opponent.getUsername(), game.getUpdatedAt()));
                        } else if (game.getInt("game_status") == GameStatus.CHALLENGED.id) {            // Display a game challenge
                            // The current user is the challenger
                            if (game.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                                challenges.add(new GameMenuListItem("Waiting for response from " + game.getParseUser("player_two").getUsername(), game.getUpdatedAt()));
                            } else {        // The user has been challenged by 'player_one'
                                challenges.add(new GameMenuListItem("Challenged by " + game.getParseUser("player_one").getUsername(), game.getUpdatedAt()));
                            }
                        } else if (game.getInt("game_status") == GameStatus.GAME_OVER.id) {
                            // TODO: Display the game over list items
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                } else {
                    Log.d("prepareListData", "Error: " + e.getMessage());
                }
            }
        });

        //New Game Button == listDataHeader.get(0)
        listDataChild.put(listDataHeader.get(1), challenges); // Header, Child data
        listDataChild.put(listDataHeader.get(2), currentgames);
        listDataChild.put(listDataHeader.get(3), pastgames);

    }




    /** Refreshes the UI listviews every 5 seconds. After 16 refreshes it stops to save API accesses
     * Currently is not used because this is a fragment...
     */
    private class RefreshThread extends Thread {
        int count = 0;
        boolean stop = false;
        @Override
        public void run() {
            try {
                while (!stop) {
                    Thread.sleep(5000);
                    // after ~1.3 minutes stop querying so we don't waste API accesses
                    // counter resets at onResume();
                    if (count < 16)
                        listAdapter.notifyDataSetChanged();
                    Log.d("parseNetwork", "querying database");
                    count++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
            stop = true;
            while(true) {
                try {
                    this.join();
                    Log.d("gameloop", " ended thread");
                    break;
                } catch (InterruptedException e) {
                }
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if(!refreshThread.isAlive()) {
       //     refreshThread.start();
        }
    }

    @Override
    public void onPause() {
       // refreshThread.stopThread();
        super.onPause();
    }
}
