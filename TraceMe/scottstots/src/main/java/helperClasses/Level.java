package helperClasses;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Aaron on 3/23/2014.
 */
public class Level {

    ArrayList<String> drawings;
    ArrayList<TraceFile> traceArray;
    ScoreManager scoreManager;

    public int totalTraces = 6;
    public int currentTrace = 0;
    public int timeLeft = 15;

    public Level(int levelNum) {
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


        traceArray = new ArrayList<TraceFile>();

    }

    // Loads level from internal storage
    public void loadSinglePlayerLevel(Context ctx) {
        for(int i = 1; i <= 4; i++) {
            TraceFile trace = getTraceFile(ctx, "trace" + i + ".txt");
            if(trace != null)
                traceArray.add(trace);
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


}
