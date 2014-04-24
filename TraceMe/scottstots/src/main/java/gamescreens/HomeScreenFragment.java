package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.R;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
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
    private PullToRefreshAttacher mPullToRefreshAttacher;

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
        ViewGroup viewGroup = (ViewGroup) view;
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.expandGroup(1);
        expListView.expandGroup(2);
        expListView.expandGroup(3);
        listAdapter.notifyDataSetChanged();

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent,
                                        View v, int groupPosition, long id) {
                return parent.isGroupExpanded(groupPosition);
            }
        });

        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(expListView, expListView.getEmptyView())
                .listener(refreshListener).setup(mPullToRefreshLayout);
        mPullToRefreshLayout.setRefreshing(true);


        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v, int groupPosition, int childPosition, long id) {
                Log.d("SHIT CLICKED", "YO SHIT WAS CLICKED");
                Log.d("SHIT CLICKED", Integer.toString(groupPosition));
                if (groupPosition == 1)
                    promptUserToCancel(groupPosition, childPosition);


                return true;
            }
        });
    }

    private void promptUserToCancel(final int groupPosition, final int childPosition) {
        Log.d("getChallengerListener", "Challenger Listener Pressed");

        final ParseObject obj = listDataChild.get(listDataHeader.get(groupPosition))
                .get(childPosition).getGameParseObject();
        final Dialog dlog = new Dialog(getActivity(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dlog.setContentView(R.layout.generic_dialog);

        View yesDlogButton = dlog.findViewById(R.id.yes);
        View noDlogButton = dlog.findViewById(R.id.no);

        ((TextView) dlog.findViewById(R.id.prompt)).setText("Would you like to cancel your challenge?");

        yesDlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                obj.put("game_status", GameStatus.INVALID.id);
                obj.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("REMOVING..", "Attempting to remove the object from the list.");
                            listAdapter.delete(groupPosition, childPosition);

                            Log.d("getWaitingOpponentListener", "game cancelled successfully");
                            Toast.makeText(getActivity(),
                                    "Challenge Cancelled Successfully",
                                    Toast.LENGTH_LONG).show();

                            // TODO: Send the user a push notification for cancelled game.
                            // From Aaron: Instead of sending a notification to player B that the game was cancelled,
                            // We can instead verify the game is still valid when we get it from the listview,
                            // If it is NOT valid, then player A cancelled the game, so we locally notify player B
                            // that A cancelled the game with a "toast" or a simple message instead of notification from A, and refresh the listview?
                        } else
                            e.printStackTrace();
                    }
                });
                dlog.dismiss();
            }
        });


        noDlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlog.dismiss();
            }
        });
        dlog.show();
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
        listDataHeader.add("CHALLENGES");
        listDataHeader.add("CURRENT GAMES");
        listDataHeader.add("PAST GAMES");

        // Adding child data
        final List<GameMenuListItem> challenges = new ArrayList<GameMenuListItem>();
        final List<GameMenuListItem> currentgames = new ArrayList<GameMenuListItem>();
        final List<GameMenuListItem> pastgames = new ArrayList<GameMenuListItem>();

        final GameMenuListItem loading = new GameMenuListItem();

        challenges.add(loading);
        currentgames.add(loading);
        pastgames.add(loading);

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
                    challenges.remove(loading);
                    currentgames.remove(loading);
                    pastgames.remove(loading);

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
                    mPullToRefreshLayout.setRefreshing(false);
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
