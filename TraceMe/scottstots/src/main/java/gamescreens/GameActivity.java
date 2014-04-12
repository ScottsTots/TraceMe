package gamescreens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.parse.ParseException;
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
import scotts.tots.traceme.TraceMeApplication;

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
    static Game game;
    // Contains all points for the trace separated by the path they were at.
    // This array is used to do the drawing animation in ViewingBoard
    public static ArrayList<CustomPath> pathsArray;

    ViewingBoard viewingBoard;
    MultiViewingBoard multiViewingBoard;

    public static GameLoop gameLoop;
    Button playButton;
    ViewFlipper flipper;
    private final int COUNTDOWN_TIME = 2;
    private int time_left;
    private TextView countdownTimerView;

    private Timer myTimer;
    private long mStartTime;

    static ProgressDialog loadingDialog;
    static Dialog endGameDlog;
    static TextView scoreText;

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

        endGameDlog = new android.app.Dialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        endGameDlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        endGameDlog.setContentView(R.layout.dialog_level_end);
        scoreText = (TextView) endGameDlog.findViewById(R.id.scoreTextView);

        // Load the game
        game = ((TraceMeApplication)this.getApplicationContext()).getGame();
        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loadingDialog.setProgress(0);
        loadingDialog.setMax(100);

        if(game.isMultiplayer())
            new LoadTask().execute("loadOnline");
        else {
            new LoadTask().execute("load");
        }



        // UI
        countdownTimerView = (TextView) findViewById(R.id.countdown_timer);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        // Switch into the viewingBoard using the viewFlipper if we press "play"
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewingBoard = (ViewingBoard) findViewById(R.id.view);
                multiViewingBoard = (MultiViewingBoard) findViewById(R.id.view2);
                flipper.setDisplayedChild(3); //gameloop is 0, viewingBoard is 1
                playButton.setVisibility(View.INVISIBLE);

                multiViewingBoard.setGameData(game);
                multiViewingBoard.startDrawing();

                //viewingBoard.setGameData(game); // passes player's drawing data they just did. TODO reorganize this method and one below into 1
                //viewingBoard.startDrawing(); // this updates our viewingBoard to the current data.
            }
        });
    }

    // Called by the level object when there's no more traces.
    /**
     * When we finish a multiplayer game, we want to save some things online:
     *      1. All our paths drawn (the custom paths array)
     *      3. We also have to check whether the other player already played in GameActivity.
     */
    public static void endGame() {
        gameLoop.running = false;
        if(game.isMultiplayer()) {
            Log.d("parseNetwork", "P1 " + game.getPlayerOne() + " p2 " + game.getPlayerTwo());
            new SaveGame().execute(game);

        }
        // Sets the text on the dialog box that shows the final score.. todo: reorganize or initialize it here
        scoreText.setText(Integer.toString(level.getScore()));
        endGameDlog.show();

        // Notify player2/player1 about game status:

    }


    public static class SaveGame extends AsyncTask<Game, Integer, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }
        @Override
        protected Void doInBackground(Game... params) {
            Game game = params[0];
            game.saveUserDrawings(pathsArray);
            try {
                game.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
            loadingDialog.dismiss();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

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
                for(int i = 0; i < level.TOTAL_TRACES; i++) {
                    level.loadTrace();
                    publishProgress((int) (((i+1) / (double)level.TOTAL_TRACES) * 100));
                }
                gameLoop.setLevel(level);
               // level.createLevel();
            }
            else if(params[0].equals("loadOnline")) {
                gameLoop = (GameLoop) findViewById(R.id.surfaceView);
                level = new Level(1, ctx, gameLoop);
                level.loadLevelFromParse();
                gameLoop.setLevel(level);
                try {
                    game.fetch();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                game.loadUserData();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            loadingDialog.dismiss();

            startCountDownTimer();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
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

    private void TimerMethod() {
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