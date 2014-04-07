package gamescreens;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import scotts.tots.traceme.R;


public class LevelSelectFragment extends Fragment {// implements View.OnClickListener {
    private TextView level1;

    public LevelSelectFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_old_user, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        level1 = (TextView) view.findViewById(R.id.level1);
        level1.setOnClickListener(levelListener);
    }

    View.OnClickListener levelListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {

            }
        }
    };



}