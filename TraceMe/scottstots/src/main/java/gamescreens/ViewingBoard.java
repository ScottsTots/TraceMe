package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import helperClasses.DataPoint;
import helperClasses.PointManager;

/**
 * Created by Aaron on 3/9/14.
 */



public class ViewingBoard extends View {
    ArrayList<PointManager> paths;
    PointManager currentPath;

    public Bitmap mBitmap;
    public Canvas mCanvas;

    // The path we'll be drawing using our points.
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;

    private static final int secondsPerFrame = (int) (1.0 / 60.0f * 1000); // 60fps
    int currPathNumber;
    int currPointNumber;
    private long previous;

    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;



    private static PathEffect makeDash(float phase) {
        return new DashPathEffect(new float[] { 15, 15}, 0);
    }


    public ViewingBoard(Context c, AttributeSet attrs) {
        super(c, attrs);
        paths = GameActivity.pathsArray;
        currentPath = new PointManager(0, 0);
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
        //mPaint.setPathEffect(makeDash(0));

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


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale((float) width / 480.0f, (float) height / 800.0f);


        // If we still have paths to draw
        if(currPathNumber < paths.size()) {
            // Retrieve the current path
            currentPath = paths.get(currPathNumber);
            // If there's still points in this path to draw
            if(currPointNumber < currentPath.size()) {        // WE'RE GOOD TO DRAW
                DataPoint point = currentPath.get(currPointNumber);
                // Draw the saved paths from the framebuffer.
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                // draw the current path on top of that.
                drawPath(canvas);
                // canvas.drawPoint(point.x, point.y, mPaint);
                // See if enough time has passed to move on to the next point:
                long timeNow = System.currentTimeMillis();
                if(timeNow - previous > point.time) {
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
            // TODO every time we reset the canvas there's an empty frame and it flashes all white.

            // Clear/Reset our actual Bitmap buffer, which had our saved paths
            mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
            // Now we set this new, clear buffer to the mCanvas, which is used to save our paths into a bitmap.
            mCanvas = new Canvas(mBitmap);
        }
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
        DataPoint point = currentPath.get(currPointNumber);

        // touch_start
        if(currPointNumber == 0) {
            mPath.reset();
            mPath.moveTo(point.x, point.y);

            mX = point.x;
            mY = point.y;
        }
        // touch_move
        else {
            float dx = Math.abs(point.x - mX);
            float dy = Math.abs(point.y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

                // Since we're drawing smooth curves we need the previous point to get a good average.
                DataPoint prevPoint = currentPath.get(currPointNumber-1); //same thing as mx,mY
                mPath.quadTo(point.x, point.y, (point.x + prevPoint.x) / 2, (point.y + prevPoint.y) / 2);
            }
        }

        // TODO also fix dashed lines not being the same as original drawing...
        // touch_up
        if(currPointNumber == currentPath.size() - 1)
        {
            mPath.lineTo(point.x, point.y);
            // This saves the path we have into the buffer, so we don't lose this path when we
            // go to the next.
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }
        //draw path on the actual canvas.
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void startDrawing() {
        paths = GameActivity.pathsArray;
        postInvalidate();
    }
}