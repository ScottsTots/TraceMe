package helperClasses;

/**
 * Created by Aaron on 3/9/14.
 */

import java.util.ArrayList;

/**
 * This class manages one array of DataPoints
 * One android path = 1 Point Manager = an array of datapoints
 * **/
public class CustomPath {
    DataPoint point;
    ArrayList<DataPoint> pointArray;
    long startTime;
    // We create a new CustomPath (an array) every time we create the first point in a path.
    public CustomPath(float x, float y) {
        pointArray = new ArrayList<DataPoint>();
        startTime = System.currentTimeMillis();
        pointArray.add(new DataPoint(x, y, 0));
    }

    public void addPoint(float x, float y) {
        // We record the time difference between the last point and this one.
        pointArray.add(new DataPoint(x, y, System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
    }

    // When we load a user's customPath we don't do any time analysis so we use these two methods
    // below
    public CustomPath() {
        pointArray = new ArrayList<DataPoint>();
    }

    public void addUserPoint(float x, float y, long time, int score) {
        pointArray.add(new DataPoint(x, y, time, score));
    }




    public DataPoint get(int index) {
        return pointArray.get(index);
    }

    public int size() {
        return pointArray.size();
    }

}
