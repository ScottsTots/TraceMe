package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.R;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

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

    private PullToRefreshLayout mPullToRefreshLayout;

    public HomeScreenFragment() {
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
        listAdapter.notifyDataSetChanged();

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener()
        {
            @Override
            public boolean onGroupClick(ExpandableListView parent,
                                        View v, int groupPosition, long id)
            {
                return parent.isGroupExpanded(groupPosition);
            }
        });


        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(refreshListener).setup(mPullToRefreshLayout);
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            Log.d("Refresh", "Refresh initiated.");

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        // TODO: Instead of sleeping actually load the data
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    mPullToRefreshLayout.setRefreshComplete();
                    Toast.makeText(getActivity(),
                            "Done refreshing.",
                            Toast.LENGTH_LONG).show();
                }
            }.execute();
        }
    };

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

        ParseQuery<Game> currentGameQuery1 = ParseQuery.getQuery("Game");
        currentGameQuery1.whereEqualTo("player_one", ParseUser.getCurrentUser());

        ParseQuery<Game> currentGameQuery2 = ParseQuery.getQuery("Game");
        currentGameQuery2.whereEqualTo("player_two", ParseUser.getCurrentUser());

        List<ParseQuery<Game>> queries = new ArrayList<ParseQuery<Game>>();
        queries.add(currentGameQuery1);
        queries.add(currentGameQuery2);

        ParseQuery<Game> combinedQuery = ParseQuery.or(queries);
        combinedQuery.orderByDescending("updatedAt");
        combinedQuery.whereNotEqualTo("game_status", GameStatus.INVALID.id);
        combinedQuery.include("player_one");
        combinedQuery.include("player_two");

        combinedQuery.findInBackground(new FindCallback<Game>() {
            @Override
            public void done(List<Game> parseObjects, ParseException e) {
                if (e == null) {    // Successful query
                    for (Game game : parseObjects) {
                        // TODO: Make this a switch statement instead. Tried, but got error so come back.

                        if (game.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id ||
                                game.getInt("game_status") == GameStatus.CHALLENGED.id) {         // Waiting on opponent
                            challenges.add(new GameMenuListItem(game));
                        } else if (game.getInt("game_status") == GameStatus.IN_PROGRESS.id) {
                            currentgames.add(new GameMenuListItem(game));
                        } else if (game.getInt("game_status") == GameStatus.GAME_OVER.id) {
                            pastgames.add(new GameMenuListItem(game));
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


    @Override
    public void onResume() {
        super.onResume();
        prepareListData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
