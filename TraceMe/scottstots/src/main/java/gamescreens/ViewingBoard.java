package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;

/**
 * Created by Aaron on 3/9/14.
 */



public class ViewingBoard extends View {
    ArrayList<CustomPath> paths; // player one's drawings
    ArrayList<CustomPath> paths2; // player two's drawings
    CustomPath currentPath;

    public Bitmap mBitmap;
    public Canvas mCanvas;

    // The path we'll be drawing using our points.
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;

    private static final int secondsPerFrame = (int) (1.0 / 60.0f * 1000); // 60fps
    private final float DRAWING_SPEED = .3f; // the less, the faster the replay.
    private final boolean REPEAT_ANIM = false;

    int currPathNumber;
    int currPointNumber;
    private long previous;
    private Context context;

    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;
    private Handler handler;

    // The current point of a path being drawn.
    DataPoint point;


    public ViewingBoard(Context c, AttributeSet attrs) {
        super(c, attrs);
        if(!isInEditMode()) {
            context = c;
            paths = GameActivity.pathsArray;
            currentPath = new CustomPath(0, 0);
            Log.d("view", "ViewingBoard start");
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFF000000);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            mPath = new Path();

            // Scale window for all devices
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            height = metrics.heightPixels;
            width = metrics.widthPixels;

            // Scale the window size
            boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            frameBufferWidth = isPortrait ? 480 : 800;
            frameBufferHeight = isPortrait ? 800 : 480;
            scaleX = (float) frameBufferWidth
                    / width;
            scaleY = (float) frameBufferHeight
                    / height;

            currPathNumber = 0;
            previous = System.currentTimeMillis();
            mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }




    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    int pointCounter = 0;
    long timeNow = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale((float) width / 480.0f, (float) height / 800.0f);

        /* Uncomment this and comment out the rest of the canvas.draws() below
           to see how the scoring mechanism looks (shows all points equidistant in the drawing)

        if(pointCounter < GameActivity.pointsArray.size()) {
            DataPoint point = GameActivity.pointsArray.get(pointCounter);
            mCanvas.drawPoint(point.x, point.y, mPaint);
            // canvas.drawPoint(point.x, point.y, mPaint);
            pointCounter++;
        }
        */
        // If we still have paths to draw
        if(currPathNumber < paths.size()) {
            // Retrieve the current path
            currentPath = paths.get(currPathNumber);
            // If there's still points in this path to draw
            if(currPointNumber < currentPath.size()) {        // WE'RE GOOD TO DRAW
                // Get the latest point on this path.
                point = currentPath.get(currPointNumber);
                // draw all path stuff to our framebuffer: mBitmap
                drawPath(canvas);
                // See if enough time has passed to move on to the next point:
                timeNow = System.currentTimeMillis();
                if(timeNow - previous > (point.time * DRAWING_SPEED)) {
                    previous = System.currentTimeMillis();
                    currPointNumber++;
                }
            }
            else {
                // no more points in this path, so we can reset the currentPointNumber to go to the beginning of the
                // next path,
                currPointNumber = 0;
                currPathNumber++;
            }
        }
        else {
            // no more paths, no mo points!!
            // Reset to 0 so we loop around once more, and reset the canvas buffer (which saves paths
            // that have been drawn so far).
            currPathNumber = 0;
            currPointNumber = 0;


            // Clear/Reset our actual Bitmap buffer, which had our saved paths
            // mBitmap.eraseColor(Color.WHITE); // doesn't work, we need to create a new bitmap to clear it well...(not sure why)
            // mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
            // Now we set this new, clear buffer to the mCanvas, which is used to draw into our bitmap.
            mBitmap.eraseColor(Color.TRANSPARENT);
            if(!REPEAT_ANIM) {
                endReplay();
            }
        }
        // Draw the actual framebuffer.
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        postInvalidate(); //force a redraw
    }

    /**
     * This method is based off of the DrawingBoard's three stages:
     * touch_start, touch_move, and touch_up. We simulate these stages by figuring out which point
     * we're drawing.
     **/
    float mX;
    float mY;
    private static final float TOUCH_TOLERANCE = 4;

    public void drawPath(Canvas canvas) {
        if(currentPath.size() < 3)
            return;
        // touch_start ---------------------------
        if(currPointNumber == 0) {
            mPath.reset();
            mPath.moveTo(point.x, point.y);

            mX = point.x;
            mY = point.y;
        }

        // touch_move ----------------------------
        if(currPointNumber > 0 && currPointNumber < currentPath.size() -1) {
            float dx = Math.abs(point.x - mX);
            float dy = Math.abs(point.y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                // Since we're drawing smooth curves we need the previous point to get a good average.
                DataPoint prevPoint = currentPath.get(currPointNumber-1); //same thing as mx,mY
                mPath.quadTo(prevPoint.x, prevPoint.y, (point.x + prevPoint.x) / 2, (point.y + prevPoint.y) / 2);
            }
        }
        // touch_up ------------------------------
        if(currPointNumber == currentPath.size() - 1)
        {
            // For some strange reason we don't do these 2 lines or it glitches out.. but seems to work fine without them.
            // DataPoint prevPoint = currentPath.get(currPointNumber-1);
            // mPath.lineTo(prevPoint.x, prevPoint.y);
            // This saves the path we have into the buffer, so we don't lose this path when we
            // go to the next.
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
            return;
        }
        //draw path on the actual canvas.
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        endReplay();
        return true;
    }

    public void endReplay() {
        // TODO call the end game dialog here. Maybe set up a handler so we can call back UI or use a static method..
        // There is no need to save any game state or anything at this point.. just show the end game results, etc etc.

        handler.sendEmptyMessage(6000);
//         ((Activity)getContext()).finish();
    }


    public void startDrawing(Handler handler) {
        this.handler = handler;
        previous = System.currentTimeMillis();
        paths = GameActivity.pathsArray;
        postInvalidate();
    }
}