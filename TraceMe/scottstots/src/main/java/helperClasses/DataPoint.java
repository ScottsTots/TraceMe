package helperClasses;

/**
 * Created by Aaron on 3/9/14.
 */

/**
 * This object represents a point drawn to the canvas during the drawing phase of the game.
 * It also contains the time of when the point was drawn, so we can animate it later.
 */
public class DataPoint {
    public float x;
    public float y;
    public long time;
    public DataPoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

}
