package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import scotts.tots.traceme.DispatchActivity;

/**
 * Created by Aaron on 3/9/14.
 */


public class ViewingBoard extends View {
    public Bitmap mBitmap;
    public Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;

    private static final int secondsPerFrame = (int) (1.0 / 60.0f * 1000); // 60fps
    int curr_frame;
    private long previous;

    float scaleX;
    float scaleY;
    int width;
    int height;
    int frameBufferWidth;
    int frameBufferHeight;

    public ViewingBoard(Context c, AttributeSet attrs) {
        super(c, attrs);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(9);

        // Smooth lines
        mPaint.setFilterBitmap(true);

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

        curr_frame = 0;
        previous = System.currentTimeMillis();

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
        long timeNow = System.currentTimeMillis();
        if(timeNow - previous > secondsPerFrame) {
            curr_frame++;
            //if(curr_frame > totalnumPoints)
             //   curr_frame = 0; //loops around so we can debug/test
        }
        // DRAW THE PATH THAT NEEDS TO BE DRAWN HERE, CURR_FRAME IS THE
        // index of what point we're drawing.

    }

    private void touch_up() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}