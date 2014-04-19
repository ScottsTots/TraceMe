package helperClasses;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import gamescreens.GameActivity;
import gamescreens.GameLoop;


/**
 * Created by Aaron on 3/23/2014.
 */

public class Level{

    // Level variables:
    // Same thing as the traceArray, except it just has the traceFile bitmaps (used for optimization)
    ArrayList<Bitmap> traceBitmaps;
    ArrayList<TraceFile> traceArray;


    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSED = 0;
    public static boolean GAME_OVER = false;


    long startTime;

    static final String TAG = "LEVEL";
    Bitmap framebuffer;
    ArrayList<String> drawings;
    ScoreManager scoreManager;
    Bitmap traceBitmap;
    public static int TOTAL_TRACES = 8;
    private int currentTrace = 0;
    private float pathLength;
    private float maxPathLength;
    public int timeLeft = 15;

    Context ctx;
    Paint paint;
    GameLoop view;
    boolean countDown = true;
    double ink = 0;
    CustomTimer timer = new CustomTimer(); // Our 3 second countdown timer.
    private Handler handler;
    /**
     *
     * @param levelNum the level
     * @param ctx what activity the level is in, used for loading files
     * @param v the surfaceView/GameView connected to this level. Used for detecting touch input
     *          in this view.
     */
    public Level(int levelNum, Context ctx, GameLoop v, Handler handler) {
        String levelFile = "level" + levelNum;
        // Reads all level data from this filename.
        // File would look like:
        // trace1.txt 5 seconds    normal
        // trace2.txt 10 seconds   disappearing
        // trace3.txt 6 seconds    blinking


        this.view = v;
        this.ctx = ctx;
        this.handler = handler;
        setUpDrawing();
        traceArray = new ArrayList<TraceFile>();
        traceBitmaps = new ArrayList<Bitmap>();
    }

    String message;
    double lastScore;
    public void updateMessage(int possibleMaxPoints) {


        // Actual points gotten.
        lastScore = scoreManager.getScore() - lastScore;

        double correct = (lastScore / possibleMaxPoints) * 100;
        Log.d("gameloop", "scoring:  max points " + possibleMaxPoints + "current " + lastScore + " correct: " + correct);
        // 0 sucks, 100 perfect
//        handler.sendEmptyMessage(0);
        if (correct > 100){
            handler.sendEmptyMessage(0);
        } else if(correct > 90) {
            handler.sendEmptyMessage(1);
//            message = "GREAT";
        } else if (correct> 70) {
            handler.sendEmptyMessage(2);
//            message = "NICE JOB!";
        } else if (correct > 50) {
            handler.sendEmptyMessage(3);
//            message = "Booooo";
        } else {
            handler.sendEmptyMessage(4);
//            message = "wow..";
        }
    }

    public void getNextTrace() {

        updateMessage(scoreManager.traceData.size());
        if(currentTrace + 1 < TOTAL_TRACES) {
            currentTrace++;
            // Update the scoremanager with the new set of datapoints to score from.
            scoreManager.traceData = traceArray.get(currentTrace).points;

            // Removed saved paths.
            pathsBitmap.eraseColor(Color.TRANSPARENT);
        }
        // Game over
        else {
            saveHighScore();
            GameActivity.endGame();
        }
    }

    public void updateScore(DataPoint p) {
        scoreManager.update(p);
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getCombo() {
        return scoreManager.getCombo();
    }

    boolean isTouched = false;
    boolean startTimer = true;
    /**************************************** UPDATING ************************************/
    public void update(float deltaTime) {
        // Start the game timer if it's the first update.
        if(startTimer) {
            startTime = System.currentTimeMillis();
            timer.start();
            startTimer = false;
        }
    }

    // Then we paint stuff on the framebuffer
    public void paint() {
        mCanvas.drawColor(Color.WHITE);


        // If the user "touched up", draw congratulatory text for 2 seconds
        if(timer.getTime() < 2) {
//            mCanvas.drawText(message, 20, 200, textPaint);
        }

        traceBitmap = traceBitmaps.get(currentTrace);
        // Draw the current trace image
        if(traceBitmap != null) {
            mCanvas.drawBitmap(traceBitmap, 0, 0, mPaint);
            //drawTrace(scoreManager.traceData);
        }
        else {
            Log.d("gameloop", "trace is null!");
        }

        //TODO try drawing pixels but set alpha to false
        //TODO create traces using rgb 565 format or compress them
        // draw previous paths
        mCanvas.drawBitmap(pathsBitmap, 0, 0, mPaint);
        // draw current Path
        mCanvas.drawPath(mPath, mPaint);
        mCanvas.drawText("Score: " + Integer.toString(getScore()), 20, 120, textPaint);

        PathMeasure pm = new PathMeasure(mPath, false);
        pathLength = pm.getLength();
        maxPathLength = traceArray.get(currentTrace).getLength() * .75f;
        if(pathLength > maxPathLength)
            pathLength = maxPathLength;
        int x = 20;
        int y = frameBufferHeight - 50;
        int w = 420 - (int)((pathLength / maxPathLength) * 420); //400 is width in pixels of the ink bar on the screen
        int h = 15;
        mCanvas.drawText("Ink Level", frameBufferWidth/2 - 55, y - 20, textPaint);
        mCanvas.drawRect(x, y, x + w, y + h, mPaint); // left top right bottom
        //Log.d("gameloop", "path length " + pm.getLength());
    }

    public void drawGameOver() {
    }

    /***************************** LOADING *****************************************/
    int numTracesLoaded = 1; //starts at 1
    // Loads level from internal storage
    public void loadTrace() {
        Log.d("gameloop", "loading files");
            TraceFile trace = getTraceFile(ctx, "trace" + numTracesLoaded + ".txt");
            if (trace != null) {
                traceArray.add(trace);
                traceBitmaps.add(trace.getBitmap());
                Log.d("gameloop", "loading files " + numTracesLoaded);
            }
            else {
                // UH OH.
            }
        if(numTracesLoaded == 1)
            scoreManager = new ScoreManager(traceArray.get(0));
        numTracesLoaded++;
    }

    Gson gson = new Gson();
    private TraceFile getTraceFile(Context ctx, String filename) {
        TraceFile trace;
        StringBuilder total = new StringBuilder();
        try {
            InputStream inputStream = ctx.getAssets().open("tracedata/" + filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line);
            }
            reader.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        trace = gson.fromJson(total.toString(), TraceFile.class);
        return trace;
    }

    /******************************** INPUT DRAWING  ********************************************/
    // Used for drawing past paths into a buffer/pathsBitmap
    Bitmap pathsBitmap;
    Canvas mCanvas2;
    private Paint mPaint;
    private Canvas mCanvas;
    private Path mPath;
    private Paint textPaint;

    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;

    //TODO for optimization we could change to format RGB_565 for loaded bitmaps
    public void setUpDrawing() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(16);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(40);
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;

        // Scale the canvas for all devices based on the screen dimensions
        boolean isPortrait = ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        frameBufferWidth = isPortrait ? 480 : 800;
        frameBufferHeight = isPortrait ? 800 : 480;
        scaleX = (float) frameBufferWidth / width;
        scaleY = (float) frameBufferHeight / height;

        framebuffer = Bitmap.createBitmap(480, 800, Bitmap.Config.RGB_565);
        // When we finish drawing a path, we "save" it by just drawing it to this bitmap.
        pathsBitmap = Bitmap.createBitmap(480,800, Bitmap.Config.ARGB_8888);
        mCanvas2 = new Canvas(pathsBitmap);
        mCanvas = new Canvas(framebuffer);
        // This is only path object we use to draw. on touch_up, we save it by drawing it in our framebuffer.
        mPath = new Path();
        // The array that we will use to draw our trace. It contains time info + datapoints.
        GameActivity.pathsArray = new ArrayList<CustomPath>();


        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX() * scaleX;
                float y = event.getY() * scaleY;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        updateScore(new DataPoint(x, y));
                        touch_start(x, y);
                        isTouched = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updateScore(new DataPoint(x, y));
                        touch_move(x, y);
                        isTouched = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up();
                        isTouched = false;
                        break;
                }
                return true;
            }
        });
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        // This is an array of CustomPaths, which contains points for drawing animation later..

        GameActivity.pathsArray.add(new CustomPath(x, y, System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        Log.d("view",  "size" + GameActivity.pathsArray.size());
    }

    private void touch_move(float x, float y) {
        if(pathLength < maxPathLength) {
            // Insert the next point in our current CustomPath, which should be at the end of the stack.
            // This is an array of CustomPaths, which contains points USED FOR DRAWING ANIMATION
            GameActivity.pathsArray.get(GameActivity.pathsArray.size() - 1).addUserPoint(x, y, System.currentTimeMillis() - startTime, scoreManager.totalScore);
            startTime = System.currentTimeMillis();
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }
    }


    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to a separate framebuffer that is in mCanvas2
        mCanvas2.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();

        timer.resetTime();
        getNextTrace();
    }

    public Bitmap getFrameBuffer() {
        return framebuffer;
    }


    public Bitmap getCanvasBitmap() {
        // Returns all the stuff that has been drawn so far.
        return framebuffer.copy(Bitmap.Config.ARGB_8888, true);
    }

    // Used to debug. It draws the trace points (the score data). These should all be equidistant points
    public void drawTrace(ArrayList<DataPoint> tracePoints) {
        DataPoint point;
        for(int i = 0; i < tracePoints.size(); i++) {
            point = tracePoints.get(i);
            mCanvas.drawPoint(point.x, point.y, mPaint);
        }
    }

    // Used to draw the trace image and set the Game
    public void drawTrace(Bitmap bitmap) {
        Log.d("loading", "set trace");
        framebuffer = bitmap;
        mCanvas = new Canvas(framebuffer);
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setDashEffect() {
        mPaint.setPathEffect(new DashPathEffect(new float[] {30, 15}, 0));
    }





    /** Saving HighScore @ End of the Game **/
    public void saveHighScore() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (ParseFacebookUtils.isLinked(currentUser)) {         // Currently not working
            Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                    new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                String username = user.getName();
                                int newScore = getScore();
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
            int newScore    = getScore();

            saveHighScoreToParse(username, newScore);
        } else {                // Regularly signed in user
            String username = currentUser.getUsername();
            int newScore    = getScore();
            saveHighScoreToParse(username, newScore);
        }
    }

    public void saveHighScoreToParse(String username, int newScore) {
        ParseObject newHighScore = new ParseObject("Highscore");
        newHighScore.put("username", username);
        newHighScore.put("score", newScore);
        newHighScore.saveInBackground();
    }







    /** Saves this level to parse. To be used only when trying to create new levels **/
    int filesUploaded = 0;
    ParseObject levelObject;
    ArrayList<ParseFile> fileArray;
    public void createLevel() {
        levelObject = new ParseObject("Level");
        // Step 1: build trace data.-----------------------
        JSONArray levelTraces = new JSONArray();
        // Iterate through all the traces
        for(int i = 0; i < traceArray.size(); i++) {
            // Get the scoring points of each trace
            ArrayList<DataPoint> points = traceArray.get(i).points;
            // points are saved into this array.
            JSONArray jsonPoints = new JSONArray();
            for(int j = 0; j < points.size(); j++) {
                JSONObject point = new JSONObject();
                try {
                    point.put("x", (int)(points.get(j).x));
                    point.put("y", (int)(points.get(j).y));
                    point.put("time", 0);
                    jsonPoints.put(j, point);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Add the trace data to the array that holds all traces
            try {
                levelTraces.put(i, jsonPoints);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Step 2, build image data into parseFiles and upload to cloud.-------------------------
        // When this is done, associate the files to our levelObject.
        fileArray = new ArrayList<ParseFile>();

        // Now save bitmaps for this level.
        for (int i = 0; i < traceBitmaps.size(); i++) {

            // Compress into PNG
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean compress = traceBitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 80, stream);
            if (compress)
                Log.d("parseNetwork", "compression success");
            else
                Log.d("parseNetwork", "compression failed");

            // Convert bytes into ParseFile
            final ParseFile file = new ParseFile("img.png", stream.toByteArray());
            // Save each file to the database
            try {
                file.save();
                fileArray.add(file);
            } catch (ParseException e) {
                Log.d("parseNetwork", "couldn't save file!");
                e.printStackTrace();
            }
                /*
                file.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if(e == null) {
                            Log.d("parseNetwork", "file saved");
                            fileArray.add(file);
                            levelObject.put("img" + Integer.toString(filesUploaded), fileArray.get(i));
                        }
                    }
                });*/
        } // end for

        // step 3: associate files to the parse object
        for(int i = 0; i < fileArray.size(); i++) {
            Log.d("parseNetwork", "associating file " + i);
            levelObject.put("trace" + Integer.toString(i), fileArray.get(i));
        }
        // Add other level data
        levelObject.put("trace_array", levelTraces); // this is a JSON array(traces) of JSONArrays(datapoints for each trace) that contain JSON objects with x and y keys(point coords for each datapoint)
        levelObject.put("time_allowed", 20);
        levelObject.put("level_number", 1);
        levelObject.put("number_traces", TOTAL_TRACES);
        levelObject.saveInBackground();
    }

    ParseObject retrievedLevel;
    public void loadLevelFromParse() {
        ParseQuery<ParseObject> levelQuery = ParseQuery.getQuery("Level");
        levelQuery.whereEqualTo("level_number", 1);
        levelQuery.setLimit(1);
        try {
            retrievedLevel = levelQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // STEP 1: Retrieve images ----------------------------
        ArrayList<ParseFile> files = new ArrayList<ParseFile>();
        int totalImages = retrievedLevel.getInt("number_traces");
        for(int i = 0; i < totalImages; i++) {
            files.add(retrievedLevel.getParseFile("trace" + Integer.toString(i)));
        }

        // Retrieve parsefile bytes and turn them into bitmaps.
        traceBitmaps = new ArrayList<Bitmap>();
        Bitmap bmp;
        for (int j = 0; j < totalImages; j++) {
            try {
                byte[] data = files.get(j).getData();
                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                traceBitmaps.add(bmp);
                Log.d("parseNetwork", "Downloaded image " + j);
            } catch (ParseException e) {
                Log.d("parseNetwork", "Download image failure");
                e.printStackTrace();
            }
        } // end all image downloads

        // Step 2: Retrieve trace data -------------------------------
        // this is a JSON array(traces) of JSONArrays(datapoints for each trace) that contain JSON objects with x and y keys(point coords
        JSONArray jsonTraces = retrievedLevel.getJSONArray("trace_array");
        for(int i = 0; i < jsonTraces.length(); i++) {
            JSONArray jsonPointData = null;
            try {
                // Get the array of jsonObjects
                jsonPointData = jsonTraces.getJSONArray(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<DataPoint> tracePoints = new ArrayList<DataPoint>();
            // Iterate through all the points and build a pointArray to add to our traceFile
            for(int j = 0; j < jsonPointData.length(); j++) {
                try {
                    JSONObject jsonPoint = jsonPointData.getJSONObject(j);
                    int x = jsonPoint.getInt("x");
                    int y = jsonPoint.getInt("y");
                    int time = jsonPoint.getInt("time");
                    // Add this datapoint to our pointsarray for this trace
                    tracePoints.add(new DataPoint(x, y, time));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Add it to our buffer of traceFiles.
            TraceFile trace = new TraceFile(traceBitmaps.get(i), tracePoints);
            traceArray.add(trace);
        }
        // Finally initialize the score manager
        scoreManager = new ScoreManager(traceArray.get(0));
    }


}
