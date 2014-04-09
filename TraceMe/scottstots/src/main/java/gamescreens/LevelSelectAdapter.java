package gamescreens;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import scotts.tots.traceme.R;

/**
 * Created by nlaz on 4/8/14.
 */
//public class LevelSelectAdapter extends ArrayAdapter<String> {
//    Context mContext;
//    int textViewResourceId;
//    ArrayList<String> data;
//
//    public LevelSelectAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
//        super(context, textViewResourceId, objects);
//        this.mContext = context;
//        this.textViewResourceId = textViewResourceId;
//        this.data = objects;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) mContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(textViewResourceId, parent, false);
//        }
//
//        Log.d("LevelSelectAdapter", "Updating View");
//        TextView usernameTextView = (TextView) convertView.findViewById(R.id.level_number);
//        usernameTextView.setText(data.get(position));
//
//        return convertView;
//    }
//
//}