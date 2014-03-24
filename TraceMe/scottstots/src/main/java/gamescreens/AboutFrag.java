package gamescreens;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import scotts.tots.traceme.R;

/**
 * The about the game fragment.=
 */
public class AboutFrag extends Fragment {

    TextView aboutTextView;

    public AboutFrag() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        aboutTextView = (TextView) view.findViewById(R.id.about_text);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());

    }

}
