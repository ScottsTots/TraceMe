
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
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
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

import helperClasses.DataPoint;
import helperClasses.PointManager;


/**
 * This DrawingBoard was put together with some sort of magic.
 * Android Paths get converted into my own paths made out of "points", which are
 * used and modified in certain calculations to create effects (see the Save Button)
 */
public class DrawingBoard extends View {

    private Paint mPaint;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;

    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;




    private static PathEffect makeDash(float phase) {
        return new DashPathEffect(new float[] { 15, 15}, 0);
    }

    public DrawingBoard(Context c, AttributeSet attributeSet) {
        super(c,attributeSet);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        mPaint.setPathEffect(makeDash(0));

        mPath = new Path();

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


        mBitmap = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        GameActivity.pathsArray = new ArrayList<PointManager>();
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
        Log.d("view", "Drawing on");
        // TODO for some reason the xml file doesn't compile if we scale the canvas...
        canvas.scale((float) width / 480.0f, (float) height / 800.0f);
        canvas.drawColor(0xFAAAAAAA);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        GameActivity.pathsArray.add(new PointManager(x, y));
        Log.d("view",  "size" + GameActivity.pathsArray.size());
    }

    private void touch_move(float x, float y) {
        // Insert the next point in our current path, which should be at the end of the stack.
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
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
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


}
