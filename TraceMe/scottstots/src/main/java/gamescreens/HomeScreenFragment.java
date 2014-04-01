package gamescreens;

/**
 * Created by Aaron on 3/9/14.
 */

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;

import scotts.tots.traceme.R;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class HomeScreenFragment extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    private Button logOutButton;
    private Button newGameButton;
    private android.app.Dialog dlog;
    private ListView challengesListView;
    private ArrayList<String> challengesList;
    private CustomMultiplayerListAdapter challengesAdapter;
    private ListView currentGamesListView;
    private ArrayList<String> currentGamesList;
    private CustomMultiplayerListAdapter currentGamesAdapter;
    private ListView pastGamesListView;
    private ArrayList<String> pastGamesList;
    private CustomMultiplayerListAdapter pastGamesAdapter;

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
        newGameButton = (Button) view.findViewById(R.id.newGameButton);

        challengesListView = (ListView) view.findViewById(R.id.challengesListView);
        currentGamesListView = (ListView) view.findViewById(R.id.currentGamesListView);
        pastGamesListView = (ListView) view.findViewById(R.id.pastGamesListView);
        setupListViews();

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

    private void setupListViews(){
        challengesList = new ArrayList<String>();
        challengesList.add("Example 1");
        challengesList.add("Example 2");
        challengesList.add("Example 3");
        challengesAdapter = new CustomMultiplayerListAdapter(getActivity().getApplicationContext(),
                R.layout.list_multiplayer,challengesList);
        challengesListView.setAdapter(challengesAdapter);
        challengesAdapter.notifyDataSetChanged();

        currentGamesList = new ArrayList<String>();
        currentGamesList.add("Example 1");
        currentGamesList.add("Example 2");
        currentGamesAdapter = new CustomMultiplayerListAdapter(getActivity().getApplicationContext(),
                R.layout.list_multiplayer,currentGamesList);
        currentGamesListView.setAdapter(currentGamesAdapter);
        currentGamesAdapter.notifyDataSetChanged();

        pastGamesList = new ArrayList<String>();
        pastGamesList.add("Example 1");
        pastGamesAdapter = new CustomMultiplayerListAdapter(getActivity().getApplicationContext(),
                R.layout.list_multiplayer,pastGamesList);
        pastGamesListView.setAdapter(pastGamesAdapter);
        pastGamesAdapter.notifyDataSetChanged();
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
                    Intent intent = new Intent(getActivity(), LoginScreen.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }*/
}

class CustomMultiplayerListAdapter extends ArrayAdapter<String> {
    Context mContext;
    int textViewResourceId;
    ArrayList<String> data;

    public CustomMultiplayerListAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
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

        TextView item = (TextView) convertView.findViewById(R.id.list_multiplayer);
        item.setText(data.get(position));

//        // Gets linearlayout
//        LinearLayout layout = (LinearLayout)item.getParent();
//        // Gets the layout params that will allow you to resize the layout
//        ViewGroup.LayoutParams params = layout.getLayoutParams();
//        // Changes the height and width to the specified *pixels*
//        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, mContext.getResources().getDisplayMetrics());
//        params.height = height*data.size();

        return convertView;
    }

}