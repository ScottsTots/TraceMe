package scotts.tots.traceme;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.parse.ParseUser;
import com.parse.PushService;

import java.util.ArrayList;

import gamescreens.AboutFrag;
import gamescreens.HighScoreFragment;
import gamescreens.HomeScreenFragment;
import gamescreens.SettingsFragment;
import helperClasses.Game;
import helperClasses.UsefulMethods;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

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
    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This layout contains a navigation drawable and a fragment
        // (defined as "content_frame"), which contains the game lobby etc..
        setContentView(R.layout.activity_main_screen);
        ctx = this;

        final int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        final TextView title = (TextView) getWindow().findViewById(actionBarTitle);
        Typeface face = Typeface.createFromAsset(getAssets(), "GrandHotel-Regular.otf");
        roboto_light = Typeface.createFromAsset(getAssets(),"Roboto/Roboto-Light.ttf");
        roboto_regular = Typeface.createFromAsset(getAssets(),"Roboto/Roboto-Regular.ttf");

        title.setTypeface(face);
        title.setTextSize(30);

        // mTitle is the action bar's title when drawer is closed.
        // mDrawer title is the title that appears when the drawer opens.
        mTitle = getResources().getString(R.string.title_activity_main_screen);
        mDrawerTitle = "Game Menu";
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Set up drawers items
        datalist = new ArrayList<String>();
        datalist.add(UsefulMethods.getParseUsername(ParseUser.getCurrentUser()));
        datalist.add("About");
        datalist.add("Rankings");
        datalist.add("Settings");
        datalist.add("Logout");
        adapter = new DrawerListAdapter(this, R.layout.drawer_list_item, datalist);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        adapter.notifyDataSetChanged();

        // Enable ActionBar app icon to behave as action to toggle nav drawer
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
            Fragment fragment = new HomeScreenFragment();
            String mTag = fragment.getTag(); // instance method of a to get a tag

            FragmentTransaction mFragmentTransaction = getFragmentManager().beginTransaction();

            mFragmentTransaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
            mFragmentTransaction.replace(R.id.content_frame, fragment);
            mFragmentTransaction.commit();
        }
    }

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

    // TODO: Clean this function up. Some of this is needed, but a lot of ugly comments
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0: // User
                    Fragment fragment = new HomeScreenFragment();
                    String mTag = fragment.getTag(); // instance method of a to get a tag

                    FragmentTransaction mFragmentTransaction = getFragmentManager().beginTransaction();

//                    mFragmentTransaction.setCustomAnimations( R.anim.slide_in_right, R.anim.slide_out_left,R.anim.slide_in_left,R.anim.slide_out_right);
                    mFragmentTransaction.add(R.id.content_frame, fragment);
//                    mFragmentTransaction.addToBackStack(mTag);
                    mFragmentTransaction.commit();
                    break;
                case 1: // About
                    Fragment frag1 = new AboutFrag();
                    String nTag = frag1.getTag(); // instance method of a to get a tag

                    FragmentTransaction nFragmentTransaction = getFragmentManager().beginTransaction();

//                    nFragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                    nFragmentTransaction.add(R.id.content_frame, frag1);
//                    nFragmentTransaction.addToBackStack(nTag);
                    nFragmentTransaction.commit();
                    break;
                case 2: //Rankings
                    Fragment frag2 = new HighScoreFragment();
                    String pTag = frag2.getTag(); // instance method of a to get a tag

                    FragmentTransaction pFragmentTransaction = getFragmentManager().beginTransaction();

//                    pFragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                    pFragmentTransaction.add(R.id.content_frame, frag2);
//                    pFragmentTransaction.addToBackStack(pTag);
                    pFragmentTransaction.commit();
                    break;
                case 3: // Settings
                    Fragment frag3 = new SettingsFragment();
                    String qTag = frag3.getTag(); // instance method of a to get a tag

                    FragmentTransaction qFragmentTransaction = getFragmentManager().beginTransaction();

//                    qFragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                    qFragmentTransaction.add(R.id.content_frame, frag3);
//                    qFragmentTransaction.addToBackStack(qTag);
                    qFragmentTransaction.commit();
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


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
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
