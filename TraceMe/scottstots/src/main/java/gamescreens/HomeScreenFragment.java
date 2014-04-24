package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;
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
    private android.app.Dialog dlog;
    private android.app.Dialog randDlog;
    private Dialog chooseFriendDlog;
    private Context ctx;
    ProgressDialog loadingDialog;
    Typeface roboto_light;
    Typeface roboto_regular;

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


        game = ((TraceMeApplication) getActivity().getApplicationContext()).getGame();
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent,
                                        View v, int groupPosition, long id) {
                switch(groupPosition){
                    case 0:
                        showDialog();
                        break;
                }

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
                Log.d("EVENT", "List Item Pressed");

                GameMenuListItem listItem = listDataChild
                        .get(listDataHeader.get(groupPosition)).get(childPosition);
                Game gameObj = listItem.getGameParseObject();


                // Listener for a Game that is awaiting an opponent.
                if (gameObj.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
                    promptUserToCancel(groupPosition, childPosition);
                } else if (gameObj.getInt("game_status") == GameStatus.CHALLENGED.id) {
                    // TODO: Aaron is the Challenged gameflow are games ever going to have
                    // a status of challenge? From what I have observed they do not so this
                    // may not matter.
                } else if (gameObj.getInt("game_status") == GameStatus.IN_PROGRESS.id) {
                    Log.d("EVENT", "Hit In Progress Game");
                    Toast.makeText(getActivity(),
                            "Starting game",
                            Toast.LENGTH_SHORT).show();
                    ((TraceMeApplication) getActivity().getApplicationContext()).setGame(gameObj);
                    startActivity(new Intent(getActivity(), GameActivity.class));

                }

                return true;
            }
        });
    }


    public void showDialog() {
        Log.d("Group Click", "New Button Pressed");
        dlog = new android.app.Dialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dlog.setContentView(R.layout.home_fragment_new_game_dialog);

        // Hook up single player button
        View singlePlayerButton = dlog.findViewById(R.id.singlePlayer);
        singlePlayerButton.setOnClickListener(viewListener);

        View randomOpponentButton = dlog.findViewById(R.id.randomOpponentButton);
        randomOpponentButton.setOnClickListener(viewListener);

        View challengeFriendButton = dlog.findViewById(R.id.challengeButton);
        challengeFriendButton.setOnClickListener(viewListener);

        dlog.show();
    }

    View.OnClickListener viewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.singlePlayer:
                    Log.d("Mainscreen.java", "SinglePlayer Button Clicked.");
                    dlog.dismiss();
                    game.setMultiplayer(false);

                    Fragment frag = new LevelSelectFragment();
                    String nTag = frag.getTag(); // instance method of a to get a tag

                    FragmentTransaction nFrag = getActivity().getFragmentManager().beginTransaction();

                    nFrag.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                    nFrag.add(R.id.content_frame, frag);
                    nFrag.addToBackStack(nTag);
                    nFrag.commit();
                    break;
                case R.id.randomOpponentButton:
                    Log.d("Mainscreen.java", "RandomOpponent Button Clicked.");
                    dlog.dismiss();
                    game.setMultiplayer(true);
//                    findRandomOpponent();
                    break;
                case R.id.challengeButton:
                    Log.d("Mainscreen.java", "Challenge Button Clicked.");
                    dlog.dismiss();
                    game.setMultiplayer(true);
//                    findFriendOpponent();
                    break;
            }
        }
    };

    public void findRandomOpponent() {
        randDlog = new android.app.Dialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        randDlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        randDlog.setContentView(R.layout.message);

        TextView dlogText = (TextView) randDlog.findViewById(R.id.dlogText);
        Button dismissButton = (Button) randDlog.findViewById(R.id.dlogDismissButton);

        // Search for games that need another player if possible
        // Criteria:
        //      Game is Searching for an opponent
        //      player_one of the game is not the current user
        ParseQuery<Game> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("game_status", GameStatus.WAITING_FOR_OPPONENT.id);
        query.whereNotEqualTo("player_one", ParseUser.getCurrentUser());
        query.whereNotEqualTo("blocked", true);
        query.include("player_one");

        try { //TODO loading dialog here?
            List<Game> gameList = query.find();
            if (gameList.size() == 0) {         // No games, create a new one awaiting an opponent
                ParseObject game = ParseObject.create("Game");
                game.put("player_one", ParseUser.getCurrentUser());
                game.put("game_status", GameStatus.WAITING_FOR_OPPONENT.id);
                game.put("multiplayer", true);
                game.put("blocked", false);
                game.saveInBackground();
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        randDlog.dismiss();
                    }
                });


                dlogText.setText("Unable to match you with opponent. You will be notified when an opponent is found.");
                randDlog.show();
            } else {                                    // Retrived games, pair with one of the games
                Game game = gameList.get(0);     // Just grab the first item
                //game.put("game_status", GameStatus.IN_PROGRESS.id); ---- not putting in progress just yet..
                game.put("blocked", true); // when this player grabs the game, we lock it to keep it from pairing with other people looking for games.
                game.saveInBackground(); // TODO is this really atomic? what if two people successfully block/play 1 game?
                game.put("player_two", ParseUser.getCurrentUser());
                // From Aaron: Will instead send a notification after the player is done playing this game.
                // And just say "player accepted your challenge", with the results already there.


                ((TraceMeApplication) getActivity().getApplicationContext()).setGame((Game)game);
                Toast.makeText(getActivity(), "Found a game!", Toast.LENGTH_SHORT).show();
                // We start the game now because if we have a dialog we need to clear out the
                // "blocked" var back to false if they exit the dialog instead of playing and it's kind of chaotic...
                startActivity(new Intent(ctx, GameActivity.class));
//                dlogText.setText("Found an opponent!"); //TODO maybe insert stats about opponent and profile pic here.
//                dismissButton.setText("Start Game!");
//                dismissButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        startActivity(new Intent(ctx, GameActivity.class));
//                        randDlog.dismiss();
//                    }
//                });

            }
        } catch (ParseException e) {
            e.printStackTrace();

            dlogText.setText("Error. Please try again.");
        }
    }

    // Saves the name from the editText the user inputs
    String playerTwoName;

    public void findFriendOpponent() {

        // Set up the dialog
        chooseFriendDlog = new android.app.Dialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        chooseFriendDlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        chooseFriendDlog.setContentView(R.layout.dialog_choose_friend);


        // Set up the edit text
        final EditText editText = (EditText) chooseFriendDlog.findViewById(R.id.friendName);

        Button startGameButton = (Button) chooseFriendDlog.findViewById(R.id.startButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerTwoName = editText.getText().toString().trim();

                // PlayerTwo validation
                if (playerTwoName != null) {
                    new startGameTask().execute(playerTwoName); // initiates game if name is valid
                } else { // empty field
                    Toast.makeText(getActivity(), "Player Two's username required to play!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        chooseFriendDlog.show();

    }

    /** Checks if a user name exists and starts the game if it is valid **/
    public class startGameTask extends AsyncTask<String, Integer, ParseUser> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected ParseUser doInBackground(String... params) {
            return getPlayer(params[0]);
        }

        @Override
        protected void onPostExecute(ParseUser user) {
            loadingDialog.dismiss();
            // You cannot challenge yourself
            if (playerTwoName.equals(ParseUser.getCurrentUser().getUsername())) {
                Toast.makeText(getActivity(), "You cannot challenge yourself :(",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Verify the given player exists
            if (user == null) {
                Toast.makeText(getActivity(), "Username " + "\"" + playerTwoName + "\"does not exist.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // At this point you're good to go.. Create the new Challenge object.
            game.setPlayerOne(ParseUser.getCurrentUser());
            game.setPlayerTwo(user);
            game.setGameStatus(GameStatus.CHALLENGED);
            //  game.saveInBackground(); // game isn't saved to parse until we end the game.

            // Initiate level select
            Fragment frag = new LevelSelectFragment();
            String nTag = frag.getTag(); // instance method of a to get a tag
            FragmentTransaction nFrag = getActivity().getFragmentManager().beginTransaction();
            nFrag.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
            nFrag.replace(R.id.content_frame, frag);
            // nFrag.addToBackStack(nTag);
            nFrag.commit();
            chooseFriendDlog.dismiss();
        }
    }

    private ParseUser getPlayer(String s) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", s);
        Log.d("parseNetwork", "verifying username availability");
        try {
            return query.getFirst();
        } catch (ParseException e1) { // if no results, player doesn't exist
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
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
                try {
                    obj.delete();

                    // For consistency delete from both
                    listDataChild.get(listDataHeader.get(groupPosition)).remove(childPosition);
                    listAdapter.delete(groupPosition, childPosition);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
                    // Whoops
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
        listDataHeader.add("New Game!");
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
        listDataChild.put(listDataHeader.get(0), new ArrayList<GameMenuListItem>());
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
