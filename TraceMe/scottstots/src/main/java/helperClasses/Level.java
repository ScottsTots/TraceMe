package helperClasses;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Aaron on 3/23/2014.
 */
public class Level {
    Bitmap framebuffer;
    Context context;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSED = 0;

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

    ArrayList<String> drawings;
    ArrayList<TraceFile> traceArray;
    ArrayList<Bitmap> traceBitmaps;
    ScoreManager scoreManager;

    Bitmap traceBitmap;

    public int totalTraces = 6;
    public int currentTrace = 0;
    public int timeLeft = 15;

    Context ctx;

    Paint paint;

    public Level(int levelNum, Context ctx) {
        String levelFile = "level" + levelNum;
        // Reads all level data from this filename.
        // File would look like:
        // trace1.txt 5 seconds    normal
        // trace2.txt 10 seconds   disappearing
        // trace3.txt 6 seconds    blinking

        // After it reads which traces it needs,
        // It builds an array of the traceFile names but does not load the actual data yet.
        // In the future, each of these traces could have more properties, such
        // as blinking, disappearing after a certain time (to make it harder), or
        // etcetera. Each of these trace objects also contains the amount of time
        // one would need to finish this specific drawing/trace.

        this.ctx = ctx;
        setUpDrawing();
        traceArray = new ArrayList<TraceFile>();
        traceBitmaps = new ArrayList<Bitmap>();
    }

    // Loads level from internal storage
    public void loadSinglePlayerLevel(Context ctx) {
        for(int i = 1; i <= 4; i++) {
            TraceFile trace = getTraceFile(ctx, "trace" + i + ".txt");
            if (trace != null) {
                traceArray.add(trace);
                traceBitmaps.add(traceArray.get(traceArray.size() - 1).getBitmap());
            }
            else {

                // UH OH.
            }
        }
        scoreManager = new ScoreManager(traceArray.get(0));
    }

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
        Gson gson = new Gson();
        trace = gson.fromJson(total.toString(), TraceFile.class);
        return trace;
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


    // First we update game logic...
    public void update(float deltaTime) {

    }


    // Then we paint stuff on the framebuffer
    public void paint() {
        mCanvas.drawColor(Color.WHITE);
        traceBitmap = traceBitmaps.get(currentTrace);
        if(traceBitmap != null) {
            mCanvas.drawBitmap(traceBitmap, 0, 0, mPaint);
        }
        mCanvas.drawText("yay", 300, 700, paint);
    }

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

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        paint = new Paint();
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

        framebuffer = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(framebuffer);
    }

    public Bitmap getFrameBuffer() {
        return framebuffer;
    }
}
