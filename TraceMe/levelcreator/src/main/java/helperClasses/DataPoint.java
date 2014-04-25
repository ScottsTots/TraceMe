package helperClasses;

/**
 * Created by Aaron on 3/9/14.
 */

/**
 * This object represents a point drawn to the canvas during the drawing phase of the game.
 * It also contains the time of when the point was drawn, so we can animate it later.
 */
public class DataPoint implements Comparable<DataPoint>{
    public float x;
    public float y;
    public long time;
    public boolean touched;
    public int score;
    public DataPoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
        touched = false;
    }

    public DataPoint(float x, float y) {
        this.x = x;
        this.y = y;
        this.time = 0;
        touched = false;
    }

    /** Creates a new DataPoint with coordintes, time it was drawn, and score at that time **/
    public DataPoint(float x, float y, long time, int score) {
        this.x = x;
        this.y = y;
        this.time = time;
        touched = false;
        this.score = score;
    }

    // sorts by x coordinate
    @Override
    public int compareTo(DataPoint p) {
        // multiply by 10 because these are floats. (for more precision).
        //return (int)(x * 10) - (int)(p.x * 10);
        return (int)x - (int)p.x;
    }



}