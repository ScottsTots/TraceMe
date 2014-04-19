package gamescreens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import helperClasses.CustomPath;
import helperClasses.Game;
import helperClasses.Level;
import scotts.tots.traceme.MainScreen;
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

    static ViewingBoard viewingBoard;
    static MultiViewingBoard multiViewingBoard;

    public static GameLoop gameLoop;
    static Button playButton;
    static Button endTurnButton;
    static ViewFlipper flipper;
    private final int COUNTDOWN_TIME = 2;
    private int time_left;
    private TextView countdownTimerView;
    private TextView feedback_text;
    private TextView round_text;

    private Timer myTimer;
    private long mStartTime;

    static ProgressDialog loadingDialog;
    static Dialog endGameDlog;
    static TextView scoreText;

    public static Level level;
    static Context ctx;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        ctx = this;
        pathsArray = new ArrayList<CustomPath>(); //used to play animation


        multiViewingBoard = (MultiViewingBoard) findViewById(R.id.view2);
        viewingBoard = (ViewingBoard) findViewById(R.id.view);
        feedback_text = (TextView)findViewById(R.id.feedback_text);
        round_text = (TextView) findViewById(R.id.round_text);

        endGameDlog = new android.app.Dialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        endGameDlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        endGameDlog.setContentView(R.layout.dialog_level_end);
        endGameDlog.setCanceledOnTouchOutside(false);

        scoreText = (TextView) endGameDlog.findViewById(R.id.scoreTextView);
        endTurnButton = (Button) endGameDlog.findViewById(R.id.endTurnButton);

        // Load the game
        game = ((TraceMeApplication)this.getApplicationContext()).getGame();
        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);
//      loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//      loadingDialog.setProgress(0);
//      loadingDialog.setMax(100);

//      if(game.isMultiplayer())
        new LoadTask().execute("loadOnline");



        // UI
        countdownTimerView = (TextView) findViewById(R.id.countdown_timer);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        // Switch into the viewingBoard using the viewFlipper if we press "play"
//        playButton = (Button) findViewById(R.id.playButton);
//        playButton.setVisibility(View.INVISIBLE);
   /*     playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewingBoard = (ViewingBoard) findViewById(R.id.view);
                if(game.isMultiplayer()) {
                    flipper.setDisplayedChild(3); //gameloop is 0, viewingBoard is 1
                    playButton.setVisibility(View.INVISIBLE);

                    multiViewingBoard.setGameData(game);
                    multiViewingBoard.startDrawing();
                }
                else {

                    flipper.setDisplayedChild(2); //gameloop is 0, viewingBoard is 1
                    playButton.setVisibility(View.INVISIBLE);
                    viewingBoard.setGameData(game); // passes player's drawing data they just did. TODO reorganize this method and one below into 1
                    viewingBoard.startDrawing(); // this updates our viewingBoard to the current data.
                }
            }
        });*/

       handler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("HandleMessage", "Messaged Handled!");
                if (msg.what == 0) {
                    feedback_text.setText("PERFECT!");
                    feedback_text.setVisibility(View.VISIBLE);
                } else if (msg.what == 1) {
                    feedback_text.setText("GREAT!");
                    feedback_text.setVisibility(View.VISIBLE);
                } else if (msg.what == 2) {
                    feedback_text.setText("NICE!");
                    feedback_text.setVisibility(View.VISIBLE);
                } else if (msg.what == 3) {
                    feedback_text.setText("GOOD TRY");
                    feedback_text.setVisibility(View.VISIBLE);
                } else if (msg.what == 4) {
                    feedback_text.setText("Well...");
                    feedback_text.setVisibility(View.VISIBLE);
                }
            }
        };

    }

    // Called by the level object when there's no more traces.
    /**
     * When we finish a multiplayer game, save stuff online, move on to the endGame Activity, which handles all drawing
     * and score showing.
     */
    public static void endGame() {
        gameLoop.running = false;
        // Saves the game, then checks game status to see what to do next.
        if(game.isMultiplayer()) {
            Log.d("parseNetwork", "game is multiplayer. saving data");
            new endGameTask().execute(game);
        }
        else {
            // TODO show the viewingBoard for single player, repeat animation once.
            // TODO after animation is done, show dialog that shows medals/score/level/repeat animation button.
            flipper.setDisplayedChild(2); //gameloop is 0, viewingBoard is 1
//            playButton.setVisibility(View.INVISIBLE);
            viewingBoard.startDrawing(); // this updates our viewingBoard to the current
            //scoreText.setText(Integer.toString(level.getScore()));
            //endGameDlog.show();
        }
    }


    public static class endGameTask extends AsyncTask<Game, Integer, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }
        @Override
        protected Void doInBackground(Game... params) {
            Game game = params[0];
            game.saveUserDrawings(pathsArray);

            game.updateState();
            try {
                game.save();
                // send push notification to opponent about game status:
                game.notifyOpponent();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
            loadingDialog.dismiss();
            // Both players done, show final end game stuff.
            if(game.isComplete()) {
                flipper.setDisplayedChild(3); //gameloop is 0, viewingBoard is 1
                multiViewingBoard.setGameData(game);
                multiViewingBoard.startDrawing();
                //((Activity)ctx).finish();
            }
            // Only player A has moved. Show him/her a dialog that says "We will notify you when B finishes!"
            // And his/her current score maybe?
            else {
                scoreText.setText(Integer.toString(level.getScore()));
                endTurnButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Activity)ctx).startActivity(new Intent((Activity)ctx, MainScreen.class));
                        endGameDlog.dismiss();
                        ((Activity)ctx).finish();
                    }
                });
                endGameDlog.show();
            }
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
                // Will send a message when all images are uploaded
                level = new Level(1, ctx, gameLoop, handler);
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
                level = new Level(1, ctx, gameLoop, handler);
                level.loadLevelFromParse();
                gameLoop.setLevel(level);
                try {
                    game.fetch();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                game.loadUserData(); // load arrays of user drawings, playerOneData and playerTwoData
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            loadingDialog.dismiss();

            // If we already have everything we need, start the whole animation with the 2 players.
            if(game.isMultiplayer() && game.isComplete()) {
                flipper.setDisplayedChild(3); //gameloop is 0, viewingBoard is 1

                multiViewingBoard.setGameData(game);
                multiViewingBoard.startDrawing();
            }
            else {
                startCountDownTimer();
            }
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