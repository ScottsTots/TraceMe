package gamescreens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import helperClasses.Level;

/**
 * Created by Aaron on 3/29/2014.
 * based on Lunar Lander sdk sample, as well as Mario Zechner's "Beginning Android Games" game loop framework
 * This class is the heartbeat of the game.
 */

// This class will also implement touch
public class GameLoop extends SurfaceView implements Runnable {
    SurfaceHolder holder;
    boolean running = false;
    Thread renderThread;
    Level level;
    public static float FPS = 60.0f; // how fast we want the game to run.
    public GameLoop(Context context, AttributeSet attrs)  {
        super(context, attrs);
        if(!isInEditMode()) {
            holder = getHolder();
            setFocusable(true);
            renderThread = new Thread(this);
        }
    }


    public void startLoop() {
        running = true;
        renderThread.start();
    }


    public void setLevel(Level level) {
        this.level = level;
    }

    public void stopThread() {
        running = false;
        while(true) {
            try {
                renderThread.join();
                Log.d("gameloop", " ended thread");
                break;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void run() {
        Log.d("gameloop", "runninggame");
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / FPS; //divides 1 nanosecond into 60 (60 times per second)
        double delta = 0;
        int frames = 0; //counts how many frames we can render per second
        int updates = 0; //how many times it updates per second (should be 60 every time).

        // These two are just for drawing
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
//                canvas.drawText("updates: " + Integer.toString(updates2) + " frames " + Integer.toString(frames2), 20, 320, thepaint);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}





//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        if(!gameThread.isAlive()) {
//            gameThread.setRunning(true);
//            gameThread.start();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        stopThread();
//    }
