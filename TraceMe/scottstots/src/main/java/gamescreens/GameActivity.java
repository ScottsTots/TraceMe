package gamescreens;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import helperClasses.PointManager;
import scotts.tots.traceme.R;

/**
 * Created by Aaron on 3/9/14.
 */

/**
 * Manages the DrawingBoard and ViewingBoard, which are connected to this via xml.
 * The pathsArray is an object full of arrays of points. This is the buffer we use
 * to save our point data and redraw it. We could possibly transmit this data to a score manager to score
 * traces
 * See the helperClasses package
 * and DrawingBoard.java for more info.
 */
public class GameActivity extends Activity {

    public static ArrayList<PointManager> pathsArray;
    DrawingBoard drawingBoard;
    ViewingBoard viewingBoard;
    Button playButton;
    ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        pathsArray = new ArrayList<PointManager>();
        drawingBoard = (DrawingBoard) findViewById(R.id.draw);
        viewingBoard = (ViewingBoard) findViewById(R.id.view);

        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        // Switch into the viewingBoard using the viewFlipper.
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.setDisplayedChild(1); //drawingBoard is 0, viewingBoard is 1
                playButton.setVisibility(View.INVISIBLE);
                viewingBoard.startDrawing(); // this updates our viewingBoard to the current data.
            }
        });
    }

}
