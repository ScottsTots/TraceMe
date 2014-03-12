package helperClasses;

/**
 * Created by Aaron on 3/10/14.
 */


import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Represents a trace object, composed of a bitmap, the datapoints associated with it and
 * we could possibly save more info, such as android paths, animation points (which are not equidistant)
 */
public class TraceFile {
    // What we draw on the screen.
    //public Bitmap bitmap;
    public int[] pixels;
    int width = 800;
    int height = 480;
    // What we use to score a trace.
    // Saves the equidistant points that make up the bitmap drawing, used for scoring purposes
    public ArrayList<DataPoint> points;
    public TraceFile(Bitmap bitmap, ArrayList<DataPoint> points) {
        if(bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        this.points = points;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        // The one we retrieve from a resource is immutable, so we have to make a mutable copy.
        Bitmap bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        Bitmap mutable= bmp.copy(Bitmap.Config.ARGB_8888, true);
        return mutable;
    }

    public ArrayList<DataPoint> getPointArray() {
        return points;
    }

}
