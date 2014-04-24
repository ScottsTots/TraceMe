
/**
 *
 * Based on the FingerPaint Sample from the Android SDK Samples.
 *
 *
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gamescreens;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;


/**
 * This DrawingBoard was put together with some sort of magic.
 * Android Paths get converted into my own paths made out of "points", which are
 * used and modified in certain calculations to create effects (see the Save Button)
 */
public class DrawingBoard extends View {

    private Paint mPaint;


    private Bitmap mBitmap;
    private Bitmap mBitmap2;
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

    boolean toggleDataPoints;

    ArrayList<DataPoint> playerTraceData;
    public int currentLevel;

    public DrawingBoard(Context c, AttributeSet attributeSet) {
        super(c,attributeSet);

        playerTraceData = new ArrayList<DataPoint>();
        currentLevel = 1; // this would be inside the "game class"
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
          mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(16);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);


        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        // For scaling to different screen sizes
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels;
        width = metrics.widthPixels;

        // Scale the canvas for all devices based on the screen dimensions
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        frameBufferWidth = isPortrait ? 480 : 800;
        frameBufferHeight = isPortrait ? 800 : 480;
        scaleX = (float) frameBufferWidth / width;
        scaleY = (float) frameBufferHeight / height;

        // When we finish drawing a path, we "save" it by just drawing it to this bitmap.
        mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
        mBitmap2 = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
        // This is only path object we use to draw. on touch_up, we save it by drawing it in mBitmap.
        mPath = new Path();
        mCanvas = new Canvas(mBitmap);
        // The array that we will use to draw our trace. It contains time info + datapoints.
        // This is not the array we use for scoring.
        GameActivity.pathsArray = new ArrayList<CustomPath>();
        toggleDataPoints = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        frameBufferWidth = isPortrait ? 480 : 800;
        frameBufferHeight = isPortrait ? 800 : 480;
    }


    @Override
    protected void onDraw(Canvas canvas) {
       // Log.d("view", "Drawing on");
        // TODO for some reason the xml file doesn't compile if we scale the canvas...
        canvas.scale((float) width / 480.0f, (float) height / 800.0f);
        canvas.drawColor(Color.WHITE);

        if(toggleDataPoints) {
            drawTrace(GameActivity.pointsArray);
            canvas.drawBitmap(mBitmap2, 0, 0, mBitmapPaint);
        }
        else {

            canvas.drawPath(mPath, mPaint);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }


        // Draw the score.
        if(GameActivity.score != null) {
            textPaint.setTextSize(GameActivity.score.getCombo());
            canvas.drawText("Score: " + Integer.toString(GameActivity.score.getScore()), 20, 120, textPaint);
        }

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
                if(GameActivity.score != null)
                    GameActivity.score.update(new DataPoint(x, y));
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(GameActivity.score != null)
                    GameActivity.score.update(new DataPoint(x, y));
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

    public void toggleDataPoints() {
        toggleDataPoints = !toggleDataPoints;
        postInvalidate();
    }

    public Bitmap getCanvasBitmap() {
        // Returns all the stuff that has been drawn so far.
        return mBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    // Used to draw the trace points (the score data). These should all be equidistant points
    public void drawTrace(ArrayList<DataPoint> tracePoints) {
        DataPoint point;
        mCanvas.setBitmap(mBitmap2);
        for(int i = 0; i < tracePoints.size(); i++) {
            point = tracePoints.get(i);
            mCanvas.drawPoint(point.x, point.y, mPaint);
        }
        mCanvas.setBitmap(mBitmap);
    }

    // Used to draw the trace image
    public void drawTrace(Bitmap bitmap) {
        Log.d("loading", "set trace");
        mBitmap = bitmap;
        mCanvas = new Canvas(mBitmap);
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
        int distance = 10; // 10
        for(int j = 0; j < length; j+= distance) {
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
