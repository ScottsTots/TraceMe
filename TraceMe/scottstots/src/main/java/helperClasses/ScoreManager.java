package helperClasses;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aaron on 3/11/14.
 */
public class ScoreManager {
    int totalScore;

    DataPoint currentPoint;

    // number of points to sample
    int RADIUS = 4;
    // minimum distance needed between two points to increase score
    int DISTANCE = 10;
    // default score text size
    int TEXT_SIZE = 30;
    int MAX_TEXT_SIZE = 68;

    // more combos means textsize increases, looks kinda kewl.
    int combo = TEXT_SIZE;

    // error wiggle room (we don't want to cancel out all combo score with just one error)
    int ERROR_MAX = 90;

    int errors = 0;
    ArrayList<DataPoint> traceData;

    int totalPossibleScore;

    int inkBonus;

    Handler handler;
    Message msg;

    public ScoreManager(TraceFile file) {
        traceData = file.getPointArray();
        totalScore = 0;
        totalPossibleScore = 0;
        // sorts by x
     //   Collections.sort(traceData);
    }

    public void setData(ArrayList<DataPoint> data) {
        traceData = data;
        //Collections.sort(data);
        totalPossibleScore += data.size();
    }

    public void update(DataPoint touchPoint) {
        // Get appropriate range.
        int index = 0;
        float distance = 0.0f;
        DataPoint p;
        int offset = 0;
        //Log.d("score", "tracesize " + traceData.size());

        // Naive solution for scoring
        for (int i = 0; i < traceData.size(); i++) {
            p = traceData.get(i);
           // Log.d("score", "data " + p.x + " " + p.y);
            if (!(traceData.get(i).touched) && distance(touchPoint, traceData.get(i)) < DISTANCE) {
                totalScore++;
                combo++;
                // invalidate point so we don't count it again.
                traceData.get(i).touched = true;
            }
            else {
                combo = 12;
            }
        }
/*
        // Searching a small subset of the array.
        // search range is from original x coordinate +- RADIUS
        for (int i = 0; i < RADIUS; i++) {
            index = Collections.binarySearch(traceData, new DataPoint(touchPoint.x + offset, touchPoint.y, 0));
            if (i < RADIUS / 2) {
                offset -= 1;
            } else if (i == (RADIUS / 2)) {
                offset = 0;
            } else {
                offset += 1;
            }

            if (index < 0 && index > -(traceData.size())) {
                p = traceData.get(-index);
                if (!p.touched && (distance(p, touchPoint)) < DISTANCE) {
                    totalScore++;
                    combo++;

                    Log.d("score", "score          " + totalScore);
                    // invalidate point so we don't count it again.
                    p.touched = true;
                    //handler.sendMessage(msg);
                }
                else {
                    errors++;
                    if(errors > ERROR_MAX) {
                        combo -= 5;
                        if(combo < TEXT_SIZE)
                            combo = TEXT_SIZE;
                        errors = 0;
                    }
                }
            }

            if (index >= 0 && index < traceData.size()) {
                p = traceData.get(index);
                if (!p.touched && (distance(p, touchPoint)) < DISTANCE) {
                    totalScore++;
                    combo++;
                  //  Log.d("score", "score          " + totalScore);
                    // invalidate point so we don't count it again.
                    traceData.get(index).touched = true;
                }
            } else {
                errors++;
                if(errors > ERROR_MAX) {
                    combo -= 5;
                    // decrease textsize if there is an error
                    if(combo < TEXT_SIZE)
                        combo = TEXT_SIZE;
                    errors = 0;
                }
            }
        }
        */
    }

    public double distance(DataPoint a, DataPoint b) {
        return Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2));
    }

    public int getScore() {
        return totalScore;
    }

    public int getTotalPossibleScore() {
        return totalPossibleScore;
    }

    public double getTotalPercentage() {
        double percent = (double)getScore() / (double)getTotalPossibleScore();
        return percent * 100;
    }

    public int getCombo() {
        if (combo > 62)
            combo = 20;
        return combo;
    }

    public void addInkBonus(int bonus) {
        inkBonus += bonus;
    }

    public int getInkBonus() {
        return inkBonus;
    }



}
