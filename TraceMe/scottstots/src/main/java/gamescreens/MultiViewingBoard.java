package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;
import helperClasses.Game;

/**
 * Created by Aaron on 4/11/2014.
 */

    public class MultiViewingBoard extends View {
        public Bitmap mBitmap;
        public Canvas mCanvas;

        private Paint mBitmapPaint;
        private Paint mPaint;

        private static final int secondsPerFrame = (int) (1.0 / 60.0f * 1000); // 60fps
        float scaleX;
        float scaleY;
        int width;
        int height;
        int frameBufferWidth;
        int frameBufferHeight;
    Player playerOne;
    Player playerTwo;


    public MultiViewingBoard(Context c, AttributeSet attrs) {
        super(c, attrs);
        if(!isInEditMode()) {

            Log.d("view", "ViewingBoard start");
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


            mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

    }

    public void setGameData(Game game) {
        // shrink it even smaller to 1/4 of current framebuffer screen.

        // Player one's traces will be drawn on upper left.
        float frameBufferW2 = 480 / 2;
        float frameBufferH2 = 800 / 2;
        float scaleX2 = (float)  frameBufferW2 / frameBufferWidth;
        float scaleY2 = (float) frameBufferH2 / frameBufferHeight;


        playerOne = new Player(game.playerOneData, scaleX2, scaleY2, 0, 0);


        // Player two's traces will be drawn on lower right
        ;

        playerTwo = new Player(game.playerTwoData, scaleX2, scaleY2, frameBufferW2, frameBufferH2 );
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
        playerOne.updatePlayerData(canvas);
        playerTwo.updatePlayerData(canvas);
        postInvalidate(); //force a redraw
    }

    /**
     * This method is based off of the DrawingBoard's three stages:
     * touch_start, touch_move, and touch_up. We simulate these stages by figuring out which point
     * we're drawing.
     **/
//    float mX;
//    float mY;
    private static final float TOUCH_TOLERANCE = 4;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void startDrawing() {
        postInvalidate();
    }


    /** Each player will maintain it's own bitmap, and drawing data.
     * The view will send each player the canvas and they will draw their bitmaps on it
     * In addition to their current paths on the actual canvas.
     */
    private class Player {
        private long timeNow;
        int currPathNumber;
        int currPointNumber;
        private long previous;
        CustomPath currentPath;

        // the current point
        DataPoint point;
        float mX;
        float mY;

        ArrayList<CustomPath> paths;
        Bitmap mBitmap2;
        Canvas mCanvas2;
        private Path mPath;

        float scaleX, scaleY, translateX, translateY;
        public Player(ArrayList<CustomPath> p, float scaleX, float scaleY, float translateX, float translateY) {
            paths = p;
            mCanvas2 = new Canvas();
            mBitmap2 = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
            mCanvas2.setBitmap(mBitmap2);
            currentPath = new CustomPath(0, 0);
            currPathNumber = 0;
            previous = System.currentTimeMillis();
            mPath = new Path();
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.translateX = translateX;
            this.translateY = translateY;
        }

        public void updatePlayerData(Canvas canvas) {
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
                    //TODO need to also take into account time passed between two paths...
                    timeNow = System.currentTimeMillis();
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

                // Clear/Reset our actual Bitmap buffer, which had our saved paths
                mBitmap2.eraseColor(Color.TRANSPARENT);
            }
            canvas.drawBitmap(mBitmap2, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }

        // Draw this player's bitmap
        public void drawPath(Canvas canvas) {
            if(currentPath.size() < 3)
                return;

            float pointX = point.x * scaleX + translateX;
            float pointY = point.y * scaleY + translateY;
            // touch_start ---------------------------
            if(currPointNumber == 0) {
                mPath.reset();
                mPath.moveTo(pointX, pointY);

                mX = pointX;
                mY = pointY;
            }

            // touch_move ----------------------------
            if(currPointNumber > 0 && currPointNumber < currentPath.size() -1) {
                float dx = Math.abs(pointX - mX);
                float dy = Math.abs(pointY - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    // Since we're drawing smooth curves we need the previous point to get a good average.
                    DataPoint prevPoint = currentPath.get(currPointNumber-1); //same thing as mx,mY
                    float prevPointX = prevPoint.x * scaleX + translateX;
                    float prevPointY = prevPoint.y * scaleY + translateY;
                    mPath.quadTo(prevPointX, prevPointY, (pointX + prevPointX) / 2, (pointY + prevPointY) / 2);
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
                mCanvas2.drawPath(mPath, mPaint);
                mPath.reset();
            }
        }


    }

}