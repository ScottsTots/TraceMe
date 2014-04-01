package helperClasses;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;

import gamescreens.GameActivity;
import gamescreens.GameLoop;
import scotts.tots.traceme.R;


/**
 * Created by Aaron on 3/23/2014.
 */
public class Level {
    Bitmap framebuffer;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSED = 0;

    ArrayList<String> drawings;
    ArrayList<TraceFile> traceArray;
    // Same thing as the traceArray, except it just has the traceFile bitmaps (used for optimization)
    ArrayList<Bitmap> traceBitmaps;
    ScoreManager scoreManager;

    Bitmap traceBitmap;

    public int totalTraces = 6;
    public int currentTrace = 0;
    public int timeLeft = 15;

    Context ctx;
    Paint paint;
    GameLoop view;
    boolean countDown = true;
    CustomTimer timer = new CustomTimer(); // Our 3 second countdown timer.
    /**
     *
     * @param levelNum the level
     * @param ctx what activity the level is in, used for loading files
     * @param v the surfaceView/GameView connected to this level. Used for detecting touch input
     *          in this view.
     */
    public Level(int levelNum, Context ctx, GameLoop v) {
        String levelFile = "level" + levelNum;
        // Reads all level data from this filename.
        // File would look like:
        // trace1.txt 5 seconds    normal
        // trace2.txt 10 seconds   disappearing
        // trace3.txt 6 seconds    blinking
        view = v;
        this.ctx = ctx;
        setUpDrawing();
        traceArray = new ArrayList<TraceFile>();
        traceBitmaps = new ArrayList<Bitmap>();
        timer.start();
    }

    public void getNextTrace() {
        currentTrace++;
        // Update the scoremanager with the new set of datapoints to score from.
        scoreManager.traceData = traceArray.get(currentTrace).points;
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
    boolean isTouchUp = false;
    /**************************************** UPDATING ************************************/
    // First we update game logic...
    public void update(float deltaTime) {
        if(!isTouchUp) {   //if is touched and timer < 2.

        }




    }


    // Then we paint stuff on the framebuffer
    public void paint() {
        mCanvas.drawColor(Color.WHITE);


        if(timer.getTime() < 3)
        {
            mCanvas.drawText("GREAAAT!", 20, 200, textPaint);
        }

        traceBitmap = traceBitmaps.get(currentTrace);
        // Draw the current trace image
        if(traceBitmap != null) {
            mCanvas.drawBitmap(traceBitmap, 0, 0, mPaint);
        }

        //TODO try drawing pixels but set alpha to false
        //TODO create traces using rgb 565 format or compress them
        // draw previous paths
        mCanvas.drawBitmap(pathsBitmap, 0, 0, mPaint);
        // draw current Path
        mCanvas.drawPath(mPath, mPaint);
        mCanvas.drawText("Score: " + Integer.toString(getScore()), 20, 120, textPaint);
        mCanvas.drawText("yay", 300, 700, paint);
    }

    /***************************** LOADING *****************************************/
    int numTracesLoaded = 1; //starts at 1
    // Loads level from internal storage
    public void loadSinglePlayerLevel() {
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
        GameActivity.pathsArray.add(new CustomPath(x, y));
        Log.d("view",  "size" + GameActivity.pathsArray.size());
    }

    private void touch_move(float x, float y) {
        // Insert the next point in our current CustomPath, which should be at the end of the stack.
        // This is an array of CustomPaths, which contains points USED FOR DRAWING ANIMATION
        GameActivity.pathsArray.get(GameActivity.pathsArray.size() - 1).addPoint(x, y);
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }


    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to a separate framebuffer that is in mCanvas2
        mCanvas2.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();


        timer.resetTime();
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
}
