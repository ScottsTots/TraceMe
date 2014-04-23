package gamescreens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private String TAG = "GameActivity";
    private Game game;
    // Contains all points for the trace separated by the path they were at.
    // This array is used to do the drawing animation in ViewingBoard
    public static ArrayList<CustomPath> pathsArray;

    private ViewingBoard viewingBoard;
    private MultiViewingBoard multiViewingBoard;

    private GameLoop gameLoop;
    private Button playButton;
    private Button endTurnButton;
    private ViewFlipper flipper;
    private final int COUNTDOWN_TIME = 2;
    private int time_left;
    private TextView countdownTimerView;
    private TextView feedback_text;
    private TextView round_text;


    private Timer myTimer;
    private long mStartTime;

    private Dialog warningDialog;
    private ProgressDialog loadingDialog;
    private Dialog endGameDlog;
    private TextView scoreText;

    private Level level;
    private Context ctx;

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


        game = ((TraceMeApplication)this.getApplicationContext()).getGame();
        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);
//      loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//      loadingDialog.setProgress(0);
//      loadingDialog.setMax(100);


        // Warning dialog to be played onBackPressed()
        warningDialog = new android.app.Dialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        warningDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        warningDialog.setContentView(R.layout.dialog_warning);


        // UI views
        countdownTimerView = (TextView) findViewById(R.id.countdown_timer);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);

//      if(game.isMultiplayer())
        new LoadTask().execute("loadOnline");
    }



    private class LoadTask extends AsyncTask<String, Integer, Void> {
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

    // When user goes onPause, we should set any games we held to not blocked, and dismiss the
    // game... They will have to resume it from main menu again.
    @Override
    public void onPause() {
        super.onPause();
        if(game.isMultiplayer()) {
            game.updateState();
            game.saveInBackground();
        }
        gameLoop.stopThread();
        finish();
    }

    // Warn user game will be lost
    @Override
    public void onBackPressed() {
        if (game.isMultiplayer() && !game.isComplete()) {
            Button dismissDialog = (Button) warningDialog.findViewById(R.id.dlogWarningDismissButton);
            dismissDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningDialog.dismiss();
                    finish();
                }
            });
            warningDialog.show();
        }
        else {
            super.onBackPressed();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("HandleMessage", "Messaged Handled!");
            Animation fade_slide_in = AnimationUtils.loadAnimation(GameActivity.this, R.anim.fade_slide_in);
            assert fade_slide_in != null;
            fade_slide_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //Nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    feedback_text.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //Nothing
                }
            });
            if(msg.what >= 0 && msg.what <= 2) {
                if (msg.what == 0) {
                    feedback_text.setText("PERFECT!");
                } else if (msg.what == 1) {
                    feedback_text.setText("GREAT!");
                } else if (msg.what == 2) {
                    feedback_text.setText("NICE!");
                }
                feedback_text.setVisibility(View.VISIBLE);
                assert fade_slide_in != null;
                feedback_text.startAnimation(fade_slide_in);
            }

            // Handle end game message
            if(msg.what == 5000) {
                endGame();
            }
        }
    };

    /**
     * When we finish a multiplayer game, save stuff online, move on to the endGame Activity, which handles all drawing
     * and score showing. Called when there are no more traces
     */
    public void endGame() {
        gameLoop.running = false;
        // Saves the game to parse asynchronously and displays the MultiViewingboard afterwards.
        if(game.isMultiplayer()) {
            new endGameTask().execute(game);
        }
        else { // If singleplayer, just display regular viewing board
            flipper.setDisplayedChild(2); //gameloop is 0, viewingBoard is 2
            viewingBoard.startDrawing(); // this starts the animation
        }
    }


    private class endGameTask extends AsyncTask<Game, Integer, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }
        @Override
        protected Void doInBackground(Game... params) {
            Game game = params[0];
            game.saveUserDrawings(pathsArray); //TODO save this pathsArray somewhere else..
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

