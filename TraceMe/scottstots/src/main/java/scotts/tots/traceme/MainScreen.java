package scotts.tots.traceme;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import java.util.ArrayList;
import java.util.List;

import gamescreens.AboutFrag;
import gamescreens.GameActivity;
import gamescreens.HighScoreFragment;
import gamescreens.HomeScreenFragment;
import gamescreens.LevelSelectFragment;
import gamescreens.SettingsFragment;
import helperClasses.Game;
import helperClasses.GameStatus;
import helperClasses.UsefulMethods;

public class MainScreen extends Activity {
    Game game;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] navMenuTitles;
    private ArrayList<String> datalist;
    private android.app.Dialog dlog;
    private android.app.Dialog randDlog;
    private Dialog chooseFriendDlog;
    private Context ctx;
    ProgressDialog loadingDialog;
    Typeface roboto_light;
    Typeface roboto_regular;

    DrawerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This layout contains a navigation drawable and a fragment (defined as "content_frame"), which contains the game lobby etc..
        setContentView(R.layout.activity_main_screen);
        ctx = this;
        final int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        final TextView title = (TextView) getWindow().findViewById(actionBarTitle);
        Typeface face = Typeface.createFromAsset(getAssets(), "GrandHotel-Regular.otf");
        roboto_light = Typeface.createFromAsset(getAssets(),"Roboto/Roboto-Light.ttf");
        roboto_regular = Typeface.createFromAsset(getAssets(),"Roboto/Roboto-Regular.ttf");

        title.setTypeface(face);
        title.setTextSize(30);


        game = ((TraceMeApplication) this.getApplicationContext()).getGame();
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);

        //mTitle is the action bar's title when drawer is closed.
        // mDrawer title is the title that appears when the drawer opens.
        mTitle = getResources().getString(R.string.title_activity_main_screen);
        mDrawerTitle = "Game Menu";
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, navMenuTitles));

        datalist = new ArrayList<String>();

        // Add the current name of the user
        datalist.add(UsefulMethods.getParseUsername(ParseUser.getCurrentUser()));
        datalist.add("About");
        datalist.add("Rankings");
        datalist.add("Settings");
        datalist.add("Logout");
        adapter = new DrawerListAdapter(this, R.layout.drawer_list_item, datalist);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        adapter.notifyDataSetChanged();
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    public void showDialog(View v) {
        Log.d("Group Click", "New Button Pressed");
        dlog = new android.app.Dialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
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

                    FragmentTransaction nFrag = getFragmentManager().beginTransaction();

                    nFrag.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                    nFrag.replace(R.id.content_frame, frag);
                    nFrag.addToBackStack(nTag);
                    nFrag.commit();
                    break;
                case R.id.randomOpponentButton:
                    Log.d("Mainscreen.java", "RandomOpponent Button Clicked.");
                    dlog.dismiss();
                    game.setMultiplayer(true);
                    findRandomOpponent();
                    break;
                case R.id.challengeButton:
                    Log.d("Mainscreen.java", "Challenge Button Clicked.");
                    dlog.dismiss();
                    game.setMultiplayer(true);
                    findFriendOpponent();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This is for the actual items on the action bar *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0: // User
                    // If we're not trying to logout, we might try to change our content_view fragment based
                    // on the item we clicked, so we do the following steps:

                    // update the main content by replacing fragments
                    Fragment fragment = new HomeScreenFragment();
                    Bundle args = new Bundle();

                    // We send an int containing which item on the list was pressed.
                    // The "planet_number" stuff is from the tutorial.
                    args.putInt(HomeScreenFragment.ARG_PLANET_NUMBER, position);
                    fragment.setArguments(args);

                    // We replace the fragment
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    break;
                case 1: // About
                    Fragment frag1 = new AboutFrag();
                    Bundle args1 = new Bundle();
                    args1.putInt("Foo", 0);
                    frag1.setArguments(args1);
                    FragmentManager fragManager1 = getFragmentManager();
                    fragManager1.beginTransaction().replace(R.id.content_frame, frag1).commit();
                    break;
                case 2: //Rankings
                    Fragment fragment2 = new HighScoreFragment();
                    Bundle args2 = new Bundle();

                    args2.putInt("Foo", 0);
                    fragment2.setArguments(args2);

                    FragmentManager fragmentManager2 = getFragmentManager();
                    fragmentManager2.beginTransaction().replace(R.id.content_frame, fragment2).commit();
                    break;
                case 3: // Settings
                    Fragment fragment3 = new SettingsFragment();
                    Bundle args3 = new Bundle();

                    args3.putInt("Foo", 0);
                    fragment3.setArguments(args3);

                    FragmentManager fragmentManager3 = getFragmentManager();
                    fragmentManager3.beginTransaction().replace(R.id.content_frame, fragment3).commit();
                    break;
                case 4: //Logout
                    ParseUser.logOut();
                    Intent intent = new Intent(view.getContext(), LoginScreen.class);
                    startActivity(intent);
                    finish();
                    break;
            }
//            setTitle(navMenuTitles[position]);
            mDrawerList.setItemChecked(position, true);     // update selected item and title so it doesn't
            // show up in the menu if we reopen it, then close the drawer
            mDrawerLayout.closeDrawer(mDrawerList);         // now the actionbar will have the same title as the item name.
        }
    }

    /**
     * This handles the items that we click on the left menu (the opened drawer) *
     */
    private void selectItem(int position) {
        String choiceStr = getResources().getStringArray(R.array.nav_drawer_array)[position];


        // If selected the Logout option, simply log them out
        if (choiceStr.equals("Logout")) {
            ParseUser.logOut();
            Intent intent = new Intent(this, LoginScreen.class);
            startActivity(intent);
            finish();
        }

        if (position == 0) {
            // If we're not trying to logout, we might try to change our content_view fragment based
            // on the item we clicked, so we do the following steps:

            // update the main content by replacing fragments
            Fragment fragment = new HomeScreenFragment();
            Bundle args = new Bundle();

            // We send an int containing which item on the list was pressed.
            // The "planet_number" stuff is from the tutorial.
            args.putInt(HomeScreenFragment.ARG_PLANET_NUMBER, position);
            fragment.setArguments(args);

            // We replace the fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 1) {            // Highscore
            Fragment fragment = new HighScoreFragment();
            Bundle args = new Bundle();

            args.putInt("Foo", 0);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (position == 2) {     // About
            Fragment fragment = new AboutFrag();
            Bundle args = new Bundle();
            args.putInt("Foo", 0);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
        setTitle(navMenuTitles[position]);
        mDrawerList.setItemChecked(position, true);     // update selected item and title so it doesn't
        // show up in the menu if we reopen it, then close the drawer
        mDrawerLayout.closeDrawer(mDrawerList);         // now the actionbar will have the same title as the item name.
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void findRandomOpponent() {
        randDlog = new android.app.Dialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
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


                ((TraceMeApplication) this.getApplicationContext()).setGame((Game)game);
                Toast.makeText(this, "Found a game!", Toast.LENGTH_SHORT).show();
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
        chooseFriendDlog = new android.app.Dialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
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
                    Toast.makeText(MainScreen.this, "Player Two's username required to play!",
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
                Toast.makeText(MainScreen.this, "You cannot challenge yourself :(",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Verify the given player exists
            if (user == null) {
                Toast.makeText(MainScreen.this, "Username " + "\"" + playerTwoName + "\"does not exist.",
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
            FragmentTransaction nFrag = getFragmentManager().beginTransaction();
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


    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    class DrawerListAdapter extends ArrayAdapter<String> {
        Context mContext;
        int textViewResourceId;
        ArrayList<String> data;

        public DrawerListAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            this.mContext = context;
            this.textViewResourceId = textViewResourceId;
            this.data = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(textViewResourceId, parent, false);
            }
            switch(position){
                case 0: // User
                    break;
                case 1: // About
                    ImageView b = (ImageView) convertView.findViewById(R.id.drawer_image);
                    b.setImageResource(R.drawable.icon_rankings);
                    break;
                case 2: // Rankings
                    ImageView c = (ImageView) convertView.findViewById(R.id.drawer_image);
                    c.setImageResource(R.drawable.icon_friends);
                    break;
                case 3: //Rankings
                    ImageView d = (ImageView) convertView.findViewById(R.id.drawer_image);
                    d.setImageResource(R.drawable.icon_settings);
                    break;
                case 4: //Logout
                    ImageView e = (ImageView) convertView.findViewById(R.id.drawer_image);
                    e.setVisibility(View.GONE);
                    break;
            }
            Log.d("LevelSelectAdapter", "Updating View");
            TextView levelView = (TextView) convertView.findViewById(R.id.drawer_text);
            levelView.setText(data.get(position));
            levelView.setTypeface(roboto_regular);
            return convertView;
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        // Subscribe to notifications when app is gone from view.
        if(ParseUser.getCurrentUser() != null)
            PushService.subscribe(this, ParseUser.getCurrentUser().getUsername(), DispatchActivity.class);
        //ParseInstallation.getCurrentInstallation().saveEventually();

    }

    @Override
    public void onResume() {
        super.onResume();
        game = ((TraceMeApplication) this.getApplicationContext()).getNewGame();
        // No notifications received while user is using the app. Uncomment to see notifications at all times.
        PushService.unsubscribe(this, ParseUser.getCurrentUser().getUsername());
    }

}
