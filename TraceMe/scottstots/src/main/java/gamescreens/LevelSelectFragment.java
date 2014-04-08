package gamescreens;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import helperClasses.Game;
import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;


public class LevelSelectFragment extends Fragment {// implements View.OnClickListener {
    private TextView level1;
    private final int NUM_LEVELS = 11;

    private Game game;
    private GridView gridview;
    private LevelAdapter adapter;

    public LevelSelectFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_level_select, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        level1 = (TextView) view.findViewById(R.id.level1);
        game = ((TraceMeApplication)view.getContext().getApplicationContext()).getGame();
//        level1.setOnClickListener(levelListener);

//        scoreListView = (ListView) view.findViewById(R.id.highscoreListView);
//        scoreList = new ArrayList<Score>();
//        scoreList.add(new Score("Loading information..", 0));
//
//        adapter = new CustomHighScoreListAdapter(getActivity().getApplicationContext(),
//                R.layout.list_score,
//                scoreList);
//        scoreListView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();

        gridview = (GridView) view.findViewById(R.id.gridview);

        adapter = new LevelAdapter(getActivity().getApplicationContext(),R.layout.fragment_level_select_item);
        adapter.notifyDataSetChanged();
    }

    View.OnClickListener levelListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.level1:
//                    game.setLevel(1);
                    startActivity(new Intent(getActivity(), GameActivity.class));
                    break;
            }
        }
    };

    class LevelAdapter extends ArrayAdapter<String> {
        Context mContext;
        int textViewResourceId;
        ArrayList<String> data;

        public LevelAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mContext = context;
            this.textViewResourceId = textViewResourceId;

            data = new ArrayList<String>();

            for(int i = 1; i <= NUM_LEVELS;++i ){
                data.add(""+1);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(textViewResourceId, parent, false);
            }

            TextView usernameTextView = (TextView) convertView.findViewById(R.id.level_number);
            usernameTextView.setText(data.get(position));

            return convertView;
        }

    }

}