package gamescreens;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import scotts.tots.traceme.R;

/**
 * HighScore Fragment
 */
public class HighScoreFragment extends Fragment {

    static String TAG = "HighScoreFragment";

    // Keeps track of all the scores
    ArrayList<Score> scoreList = new ArrayList<Score>();

    ListView scoreListView;
    private CustomHighScoreListAdapter adapter;

    public HighScoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_high_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        scoreListView = (ListView) view.findViewById(R.id.highscoreListView);
        scoreList = new ArrayList<Score>();
        scoreList.add(new Score("Loading information..", 0));

        adapter = new CustomHighScoreListAdapter(getActivity().getApplicationContext(),
                R.layout.list_score,
                scoreList);
        scoreListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        initHighScores();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.highscores_menu, menu);
    }

    // Initialize the high score list
    private void initHighScores() {
        Log.d(TAG, "Querying for the highscores");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Highscore");
        query.orderByDescending("score");
        query.setLimit(10);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {        // Successful
                    Log.d(TAG, "Retrieved " + parseObjects.size() + " scores.");

                    scoreList.remove(0); // Remove the placeholder
                    // Add them to our list
                    for (ParseObject parseScoreObj : parseObjects) {
                        Score newScore = new Score(parseScoreObj.getString("username"), parseScoreObj.getInt("score"));
                        scoreList.add(newScore);
                    }
                    adapter.notifyDataSetChanged();
                } else {                // Error pulling down highscores
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
    }

}

class CustomHighScoreListAdapter extends ArrayAdapter<Score> {
    Context mContext;
    int textViewResourceId;
    ArrayList<Score> data;

    public CustomHighScoreListAdapter(Context context, int textViewResourceId, ArrayList<Score> objects) {
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

        Score scoreObj = data.get(position);

        TextView usernameTextView = (TextView) convertView.findViewById(R.id.list_score_username);
        usernameTextView.setText(scoreObj.getUsername());

        TextView scoreTextView = (TextView) convertView.findViewById(R.id.list_score_highscore);
        scoreTextView.setText(scoreObj.getScore());

        Animation animation = AnimationUtils.loadAnimation(mContext.getApplicationContext(), R.anim.fadein);
        convertView.startAnimation(animation);

        return convertView;
    }

}

class Score {
    private String username;
    private int score;

    public Score(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public String getScore() {
        return String.format("%d", score);
    }

    @Override
    public String toString() {
        return username + "\t\t" + score;
    }
}
