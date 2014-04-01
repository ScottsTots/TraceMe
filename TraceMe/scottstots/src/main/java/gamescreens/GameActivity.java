package gamescreens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import helperClasses.CustomPath;
import helperClasses.DataPoint;
import helperClasses.Game;
import helperClasses.Level;
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

    static String TAG = "GameActivity";

    // Contains all points for the trace separated by the path they were at.
    // This array is used to do the drawing animation in ViewingBoard
    public static ArrayList<CustomPath> pathsArray;

    ViewingBoard viewingBoard;

    public static GameLoop gameLoop;
    Button playButton;
    ViewFlipper flipper;
    private final int COUNTDOWN_TIME = 2;
    private int time_left;
    private TextView countdownTimerView;

    private Timer myTimer;
    private long mStartTime;

    ProgressDialog loadingDialog;

    public static Level level;
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        ctx = this;
        pathsArray = new ArrayList<CustomPath>(); //used to play animation

        // Load the game
        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loadingDialog.setProgress(0);
        loadingDialog.setMax(100);
        new LoadTask().execute("load");


        // UI
        countdownTimerView = (TextView) findViewById(R.id.countdown_timer);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        // Switch into the viewingBoard using the viewFlipper if we press "play"
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewingBoard = (ViewingBoard) findViewById(R.id.view);
                flipper.setDisplayedChild(2); //gameloop is 0, viewingBoard is 1
                playButton.setVisibility(View.INVISIBLE);
                viewingBoard.startDrawing(); // this updates our viewingBoard to the current data.
                saveHighScore();
            }
        });
    }


    public class LoadTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals("load")) {
                gameLoop = (GameLoop) findViewById(R.id.surfaceView);
                level = new Level(1, ctx, gameLoop);

                // Loads each trace
                for(int i = 1; i <= 3; i++) {
                    level.loadSinglePlayerLevel();
                    publishProgress((int) ((i / 3.0) * 100));
                }
                gameLoop.setLevel(level);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {


            loadingDialog.dismiss();
            startCountDownTimer();
//            gameLoop.startLoop();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //
            loadingDialog.setProgress(progress[0]);
        }

        private void startCountDownTimer(){
            mStartTime = System.currentTimeMillis();


            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }

            }, 0, 1000);
        }
    }

    public void saveHighScore() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (ParseFacebookUtils.isLinked(currentUser)) {         // Currently not working
            Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                    new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                String username = user.getName();
                                int newScore = level.getScore();
                                saveHighScoreToParse(username, newScore);
                            } else if (response.getError() != null) {
                                if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                        || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                    Log.d(TAG,
                                            "The facebook session was invalidated.");
                                } else {
                                    Log.d(TAG,
                                            "Some other error: "
                                                    + response.getError()
                                                    .getErrorMessage()
                                    );
                                }
                            }
                        }
                    });
            request.executeAsync();
        } else if (ParseTwitterUtils.isLinked(currentUser)) {
            String username = ParseTwitterUtils.getTwitter().getScreenName();
            int newScore    = level.getScore();

            saveHighScoreToParse(username, newScore);
        } else {                // Regularly signed in user
            String username = currentUser.getUsername();
            int newScore    = level.getScore();

            saveHighScoreToParse(username, newScore);
        }
    }

    public void saveHighScoreToParse(String username, int newScore) {
        Toast.makeText(getApplicationContext(),
                "Saving Score - " + username + ": " + newScore,
                Toast.LENGTH_LONG).show();

        ParseObject newHighScore = new ParseObject("Highscore");
        newHighScore.put("username", username);
        newHighScore.put("score", newScore);
        newHighScore.saveInBackground();
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.
        long millis = System.currentTimeMillis() - mStartTime;
        int seconds = (int) (millis / 1000);

        time_left = COUNTDOWN_TIME - seconds;
        if (time_left < 0){
            gameLoop.startLoop();
        }

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);

    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.
            //Do something to the UI thread here
            if (time_left < 0){
                flipper.setDisplayedChild(1);
                myTimer.cancel();
            }
            else if (time_left == 0){
                countdownTimerView.setText("GO!");
            }
            else{
                countdownTimerView.setText("" + time_left);
            }
        }
    };
}