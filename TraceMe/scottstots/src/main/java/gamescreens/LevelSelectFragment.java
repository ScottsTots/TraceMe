package gamescreens;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import helperClasses.Game;
import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;


public class LevelSelectFragment extends Fragment {// implements View.OnClickListener {
    private TextView level1;
    private final int NUM_LEVELS = 9;

    private Game game;
    private GridView gridview;
//    private ArrayAdapter<String> adapter;
    private LevelSelectAdapter adapter;
    private ArrayList<String> level_list;

    public LevelSelectFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_level_select, container, false);
        gridview = (GridView) rootView.findViewById(R.id.gridview);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        game = ((TraceMeApplication)view.getContext().getApplicationContext()).getGame();

        level_list  = new ArrayList<String>();
        for(int i = 1; i <= NUM_LEVELS;++i ){
            level_list.add(""+i);
        }

        adapter = new LevelSelectAdapter(getActivity().getApplicationContext(),
                R.layout.fragment_level_select_item, level_list);
        gridview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        gridview.setOnItemClickListener(levelListener);


    }

//    View.OnClickListener levelListener= new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            switch(view.getId()) {
//                case R.id.level1:
//                    game.setLevel(1);
//                    startActivity(new Intent(getActivity(), GameActivity.class));
//                    break;
//            }
//        }
//    };

    AdapterView.OnItemClickListener levelListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            switch(pos){
                case 0:
                    game.setLevel(1);
                    startActivity(new Intent(getActivity(), GameActivity.class));
                    break;
            }
        }
    };

    class LevelSelectAdapter extends ArrayAdapter<String> {
        Context mContext;
        int textViewResourceId;
        ArrayList<String> data;

        public LevelSelectAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
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

            Log.d("LevelSelectAdapter", "Updating View");
            TextView levelView = (TextView) convertView.findViewById(R.id.level_number);
            levelView.setText(data.get(position));
            ImageView medal = (ImageView) convertView.findViewById(R.id.level_medal);
            TextView score = (TextView) convertView.findViewById(R.id.level_score);
//            ImageView lock = (ImageView) convertView.findViewById(R.id.level_lock);

            //TODO: Hook up with actual items
            if (position > 0){
                levelView.setBackgroundResource(R.drawable.locked_level);
                levelView.setTextColor(getResources().getColor(R.color.black));
//                lock.setVisibility(View.VISIBLE);
//                medal.setVisibility(View.INVISIBLE);
                score.setVisibility(View.GONE);
                medal.setImageResource(R.drawable.lock);

            }
            return convertView;
        }

    }
    @Override
    public void onResume() {
        super.onResume();

    }

}
