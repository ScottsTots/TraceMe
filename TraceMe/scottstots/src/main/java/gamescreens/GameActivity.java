package gamescreens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;
import helperClasses.ScoreManager;
import helperClasses.TraceFile;
import scotts.tots.traceme.R;

/**
 * Created by Aaron on 3/9/14.
 */

/**
 * Manages the DrawingBoard and ViewingBoard, which are connected to this via xml.
 * The pathsArray is an object full of arrays of points that are probably not equidistant.
 * This is the buffer we use to save our point data and redraw it in ViewingBoard
 *
 * The pointsArray is used for saving a trace's points.
 * It is different from a regular DataPoint array in that all points we compute are equidistant
 * See DrawingBoard.convertToPoints for the details.
 */
public class GameActivity extends Activity {
    // Contains all points for the trace separated by the path they were at.
    // This array is used to do the drawing animation in ViewingBoard
    public static ArrayList<CustomPath> pathsArray;

    // When we load, we put our points here. The scoring manager uses this
    // to calculate score. When we save, this array should already be full with
    // the trace data.
    public static ArrayList<DataPoint> pointsArray;
    DrawingBoard drawingBoard;
    ViewingBoard viewingBoard;
    Button playButton;
    ViewFlipper flipper;
    TextView scoreText;

    Button saveTraceButton;
    TraceFile trace;

    ProgressDialog loadingDialog;
    SharedPreferences mPrefs;

    public static ScoreManager score;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        pathsArray = new ArrayList<CustomPath>();
        pointsArray = new ArrayList<DataPoint>();

        drawingBoard = (DrawingBoard) findViewById(R.id.draw);
        viewingBoard = (ViewingBoard) findViewById(R.id.view);
        scoreText = (TextView) findViewById(R.id.scoreText);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");


        // Load level, if any
        trace = new TraceFile(null, new ArrayList<DataPoint>());
        mPrefs = getSharedPreferences("gameprefs", MODE_PRIVATE);
        new LoadOrSaveTask().execute("load");



        // Switch into the viewingBoard using the viewFlipper if we press "play"
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.setDisplayedChild(1); //drawingBoard is 0, viewingBoard is 1
                playButton.setVisibility(View.INVISIBLE);
                viewingBoard.startDrawing(); // this updates our viewingBoard to the current data.
            }
        });


        // Save a trace to be used as the initial trace in the next game.
        saveTraceButton = (Button) findViewById(R.id.saveTraceButton);
        saveTraceButton.setOnClickListener(new View.OnClickListener() {
            // When we click save, all the points in pointsArray, AND the bitmap we drew will get saved
            // into a traceFile object.
            @Override
            public void onClick(View view) {
                new LoadOrSaveTask().execute("save");
                drawingBoard.setPaintColor(Color.BLACK);
            }
        });




    }

    private class LoadOrSaveTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params[0].equals("load"))
                loadTrace();
            else if(params[0].equals("save"))
                saveTrace();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {

           // Log.d("score", "siiiize" + trace.getPointArray().size());
            score = new ScoreManager(trace, handler);
            loadingDialog.dismiss();
        }
    }


    public void loadTrace() {
        // If there is a game level/trace present in our shared prefs
        Gson gson = new Gson();
        String json = mPrefs.getString("Trace1", "");
        Log.d("loading", "got json");
        if (json != null) {
            trace = gson.fromJson(json, TraceFile.class);
            Log.d("loading", "got trace");
            // draw it on the canvas.
            if (trace != null) {
                Log.d("loading", "drawing trace...");
                drawingBoard.drawTrace(trace.getBitmap());
                // drawingBoard.drawTrace(trace.points);
            }
            else {
                trace = new TraceFile(null, new ArrayList<DataPoint>());

                drawingBoard.setPaintColor(Color.BLUE);
            }

        }
    }

    public void saveTrace() {
        Bitmap bmp = drawingBoard.getCanvasBitmap();
        trace = new TraceFile(bmp, pointsArray);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trace);
        prefsEditor.putString("Trace1", json);
        prefsEditor.commit();
       // Toast.makeText(GameActivity.this, "Trace saved, start a new game for it to load", Toast.LENGTH_LONG).show();
    }


}
