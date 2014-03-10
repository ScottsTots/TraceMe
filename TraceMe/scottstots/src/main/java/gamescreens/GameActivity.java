package gamescreens;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import scotts.tots.traceme.R;

/**
 * Created by Aaron on 3/9/14.
 */
public class GameActivity extends Activity {

    DrawingBoard drawingBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        drawingBoard = (DrawingBoard) findViewById(R.id.draw);
    }

}
