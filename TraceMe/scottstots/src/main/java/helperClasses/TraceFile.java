package helperClasses;

/**
 * Created by Aaron on 3/10/14.
 */


import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Represents a trace object, composed of a bitmap, and the datapoints associated with it.
 */
public class TraceFile {
    // What we draw on the screen.
    Bitmap bmp;

    // What we use to score a trace.
    // Saves the actual data points that make up the bitmap drawing.
    ArrayList<PointManager> paths;
    public TraceFile(Bitmap bitmap, ArrayList<PointManager> paths) {
        bmp = bitmap;
        this.paths = paths;
    }
}
