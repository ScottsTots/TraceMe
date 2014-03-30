package gamescreens;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import helperClasses.Game;
import helperClasses.Level;

/**
 * Created by Aaron on 3/30/2014.
 */
public class GameThread extends Thread {

    Level level;

    private SurfaceHolder holder;
    volatile private boolean running = false;
    Context context;

    public GameThread (SurfaceHolder holder, Context ctx) {
        this.holder = holder;
        this.context = ctx;
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public void setLevel(Level l) {
        level = l;
    }

    @Override
    public void run() {
        Log.d("gameloop", "runninggame");
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / 60.0; //divides 1 nanosecond into 60 (60 times per second)
        double delta = 0;
        int frames = 0; //counts how many frames we can render per second
        int updates = 0; //how many times it updates per second (should be 60 every time).

        int frames2 = 0;
        int updates2 = 0;
        long now;
        Rect dest = new Rect();
        while(running) {
           // Log.d("gameloop", " checking valid..");
            if (!holder.getSurface().isValid() || level == null)
                continue;
         //   Log.d("gameloop", " valid!..");
            now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) { //update 60 times every second
                level.update((float) delta);
                updates++;
                delta--;
            } // if we already updated 60 times, draw the rest of the time.
            level.paint(); //displays images, not restricted in speed
            frames++;
            /** FPS runs every second **/
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000; //update/reset timer to current time.
                Log.d("gameloop", updates + "ups " + frames + "fps");

                frames2 = frames;
                updates2 = updates;
                updates = 0;
                frames = 0;
            }

            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                canvas.getClipBounds(dest);
                canvas.drawColor(Color.BLUE);
               // Log.d("gameloop", "canvas not null");
                Paint thepaint = new Paint();
                thepaint.setTextSize(40);
                //thepaint.setColor(Color.WHITE);
                // canvas.scale((float) width / 480.0f, (float) height / 800.0f);
                canvas.drawBitmap(level.getFrameBuffer(), null, dest, null);
                canvas.drawText("updates: " + Integer.toString(updates2) + " frames " + Integer.toString(frames2), 20, 320, thepaint);
                holder.unlockCanvasAndPost(canvas);
            }

        }
    }
}