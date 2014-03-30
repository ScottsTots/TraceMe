package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;
import helperClasses.Game;

/**
 * Created by Aaron on 3/29/2014.
 * based on Lunar Lander sdk sample, as well as Mario Zechner's "Beginning Android Games" game loop framework
 */

// This class will also implement touch
public class GameLoop extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder holder;
    boolean running = false;
    GameThread gameThread;
    Game game;



    private Paint mPaint;


    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint textPaint;
    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;
    public GameLoop(Context context, AttributeSet attrs)  {
        super(context, attrs);
        getHolder().addCallback(this); //TODO dont really need this callback since we already check isValid()
        gameThread = new GameThread(getHolder(), context);
        holder = getHolder();
        setFocusable(true);
       // this.game = game;
        Log.d("gameloop", "constructorrrr");


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(16);


        GameActivity.pathsArray = new ArrayList<CustomPath>();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(!gameThread.isAlive()) {
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gameThread.setRunning(false);
        while(true) {
            try {
                gameThread.join();
                Log.d("gameloop", " ended thread");
                break;
            } catch (InterruptedException e) {
            }
        }
    }

    public GameThread getGameThread() {
        return gameThread;
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        // This is an array of CustomPaths, which contains points. We are currently adding a new CustomPath
        // That starts at x,y
        GameActivity.pathsArray.add(new CustomPath(x, y));
        Log.d("view",  "size" + GameActivity.pathsArray.size());
    }

    private void touch_move(float x, float y) {
        // Insert the next point in our current CustomPath, which should be at the end of the stack.
        // This is an array of CustomPaths, which contains points. We are currently adding points.
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
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        convertToPoints(new Path(mPath));
        // kill this so we don't double draw
        mPath.reset();




    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() * scaleX;
        float y = event.getY() * scaleY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                game.level.updateScore(new DataPoint(x, y));
                //GameActivity.score.update(new DataPoint(x, y));
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                game.level.updateScore(new DataPoint(x, y));
                // GameActivity.score.update(new DataPoint(x, y));
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                touch_up();
                invalidate();
                break;
        }

        return true;
    }

    public Bitmap getCanvasBitmap() {
        // Returns all the stuff that has been drawn so far.
        return mBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    // Used to draw the trace points (the score data). These should all be equidistant points
    public void drawTrace(ArrayList<DataPoint> tracePoints) {
        DataPoint point;
        for(int i = 0; i < tracePoints.size(); i++) {
            point = tracePoints.get(i);
            mCanvas.drawPoint(point.x, point.y, mPaint);
        }
    }

    // Used to draw the trace image and set the Game
    public void drawTrace(Bitmap bitmap) {
        game = GameActivity.game;
        Log.d("loading", "set trace");
        mBitmap = bitmap;
        mCanvas = new Canvas(mBitmap);
        invalidate();
    }


    // This is the method we can use for scoring purposes:
    //  1. get the length of a path using:
    //          PathMeasure measure = new PathMeasure(path, false);
    //          float length = measure.getLength();
    //  2. Now that we have the length, we can find coordinates on the path at specific intervals using:
    //          measure.getPosTan(float distance, float[] pos, float[] tan)
    //          (Pins distance to 0 <= distance <= getLength(), and then computes the corresponding position and tangent.)
    // 3. We finally have points through the path at equal intervals. We can now get the user's trace and do
    //          the same thing. Then we check for distances between original trace and user trace and
    //          start cancelling out points and giving the user points.

    /** Creates equally divided points along an android path, to be used for scoring trace accuracy, NOT
     * drawing. The drawing data is saved in the pathsArray **/
    public void convertToPoints(Path p) {
        PathMeasure measure = new PathMeasure(p, false);
        float length = measure.getLength();
        float[] pos = new float[2];
        for(int j = 0; j < length; j+= 10) {
            measure.getPosTan(j, pos, null);
            GameActivity.pointsArray.add(new DataPoint(pos[0], pos[1], 0));
        }
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setDashEffect() {
        mPaint.setPathEffect(new DashPathEffect(new float[] {30, 15}, 0));
    }

}
