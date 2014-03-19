package helperClasses;

/**
 * Created by Aaron on 3/9/14.
 */

import java.util.ArrayList;

/**
 * This class manages one array of DataPoints
 * One android path = 1 Point Manager = an array of datapoints
 * **/
public class PointManager {
    DataPoint point;
    ArrayList<DataPoint> pointArray;
    long startTime;
    // We create a new PointManager (an array) every time we create the first point in a path.
    public PointManager(float x, float y) {
        pointArray = new ArrayList<DataPoint>();
        startTime = System.currentTimeMillis();
        pointArray.add(new DataPoint(x, y, 0));
    }

    public void addPoint(float x, float y) {
        // We record the time difference between the last point and this one.
        pointArray.add(new DataPoint(x, y, System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
    }

    public DataPoint get(int index) {
        return pointArray.get(index);
    }

    public int size() {
        return pointArray.size();
    }

}