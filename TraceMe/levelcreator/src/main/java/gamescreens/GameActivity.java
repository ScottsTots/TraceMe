package gamescreens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import helperClasses.CustomPath;
import helperClasses.DataPoint;
import helperClasses.ScoreManager;
import helperClasses.TraceFile;
import scotts.tots.levelcreator.R;

/**
 * Created by Aaron on 3/9/14.
 */

/**
 * Manages the DrawingBoard and ViewingBoard, which are connected to this via xml.
 * The pathsArray is an object full of arrays of points that are probably not equidistant.
 * This is the buffer we use to save our point data and redraw it in ViewingBoard
 *
 * The pointsArray is used for saving a trace's points.
 * It is different from a regular DataPoint array in that all points we compute are equidistant
 * See DrawingBoard.convertToPoints for the details.
 */
public class GameActivity extends Activity implements View.OnClickListener {
    // Contains all points for the trace separated by the path they were at.
    // This array is used to do the drawing animation in ViewingBoard
    public static ArrayList<CustomPath> pathsArray;

    // When we load, we put our points here. The scoring manager uses this
    // to calculate score. When we save, this array should already be full with
    // the trace data.
    public static ArrayList<DataPoint> pointsArray;

    public static ArrayList<Integer> pathLengthsArray;
    DrawingBoard drawingBoard;
    ViewingBoard viewingBoard;
    Button playButton;
    ViewFlipper flipper;
    TextView scoreText;
    static TextView numTraceText;

    Button saveTraceButton;
    TraceFile trace;

    ProgressDialog loadingDialog;
    SharedPreferences mPrefs;

    public static ScoreManager score;
    // Same thing as the traceArray, except it just has the traceFile bitmaps (used for optimization)
    public static ArrayList<Bitmap> traceBitmaps;
    public static ArrayList<TraceFile> traceArray;

    Handler handler;
    Context ctx;
    Dialog saveOrLoadDlog;
    Button switchViewButton;
    EditText levelText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        pathsArray = new ArrayList<CustomPath>();
        pointsArray = new ArrayList<DataPoint>();
        ctx = this;
        drawingBoard = (DrawingBoard) findViewById(R.id.draw);
        viewingBoard = (ViewingBoard) findViewById(R.id.view);
        scoreText = (TextView) findViewById(R.id.scoreText);
        flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        numTraceText = (TextView) findViewById(R.id.numTraces);

        // Buttons
        Button b1 = (Button) findViewById(R.id.saveTraceButton);
        b1.setOnClickListener(this);
        Button b2 = (Button) findViewById(R.id.clearFrameButton);
        b2.setOnClickListener(this);
        Button b3 = (Button) findViewById(R.id.loadLevelButton);
        b3.setOnClickListener(this);
        switchViewButton = (Button) findViewById(R.id.viewButton);
        switchViewButton.setOnClickListener(this);
        Button b5 = (Button) findViewById(R.id.saveLevelButton);
        b5.setOnClickListener(this);
        Button b6 = (Button) findViewById(R.id.toggleButton);
        b6.setOnClickListener(this);
        Button b7 = (Button) findViewById(R.id.clearAllButton);
        b7.setOnClickListener(this);
        Button b8 = (Button) findViewById(R.id.removeTraceButton);
        b8.setOnClickListener(this);
        Button b9 = (Button) findViewById(R.id.toggleAdditiveButton);
        b9.setOnClickListener(this);


        loadingDialog = new ProgressDialog(GameActivity.this);
        loadingDialog.setMessage("Loading...");

        traceArray = new ArrayList<TraceFile>();
        traceBitmaps = new ArrayList<Bitmap>();

        saveOrLoadDlog = new android.app.Dialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        saveOrLoadDlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        saveOrLoadDlog.setContentView(R.layout.dialog_save_load);
         levelText = (EditText) saveOrLoadDlog.findViewById(R.id.editLevelText);

        // Load level, if any
      //  trace = new TraceFile(null, new ArrayList<DataPoint>());
      //  mPrefs = getSharedPreferences("gameprefs", MODE_PRIVATE);
      //  new LoadOrSaveTask().execute("load");

        // Switch into the viewingBoard using the viewFlipper if we press "play"
    }

    int currView = 0;
    int levelNum = 0;
    // If additive mode is enabled, then everytime you "touch up", the trace is saved into bitmap as a diff color,
    // and remains in the canvas.
    public static boolean additiveMode = false;
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.saveTraceButton: //TODO need individual save trace button..and how many traces we have saved so far.
                if(additiveMode) {
                    Toast.makeText(ctx, "Disable additive mode to save traces manually", Toast.LENGTH_SHORT).show();
                } else {
                    TraceFile file = new TraceFile(drawingBoard.getCanvasBitmap(), new ArrayList<DataPoint>(pointsArray));
                    traceArray.add(file);  //add new trace
                    traceBitmaps.add(drawingBoard.getCanvasBitmap());

                    pathsArray.clear(); // clear all trace data
                    pointsArray.clear();
                    drawingBoard.clear(); // clear view
                }
                break;
            case R.id.clearFrameButton: // aka clearViewButton clears the view and point datas, but doesnt remove previous trace.
                pathsArray.clear(); //clear all trace data
                pointsArray.clear();
                drawingBoard.clear();
                break;

            case R.id.loadLevelButton:
                // Clear current path and equidistant point data.
                pathsArray.clear();
                pointsArray.clear();
                // Clear arrays of traces, both bitmap and data
                traceBitmaps.clear();
                traceArray.clear();
                drawingBoard.clear();
                // show dialog to choose which level.
                Button okButton = (Button) saveOrLoadDlog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!(levelText.getText().toString().equals(""))) {
                            levelNum = Integer.parseInt(levelText.getText().toString());
                            saveOrLoadDlog.dismiss();
                            new LoadOrSaveTask().execute("load");
                            Toast.makeText(ctx, "Loading game", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                saveOrLoadDlog.show();
                break;
            case R.id.viewButton: //toggles between viewing and drawing modes.
                if(currView == 0) {
                    flipper.setDisplayedChild(1); // show view
                    viewingBoard.startDrawing();
                    switchViewButton.setText("draw");
                    currView = 1;
                } else {
                    flipper.setDisplayedChild(0);
                    switchViewButton.setText("view");
                    currView = 0;
                }
                Log.d("game", " viewButton pressed");
                // set the drawing data to the viewingBoard
                break;
            case R.id.saveLevelButton:
                // show dialog to choose which level.
                final EditText levelText = (EditText) saveOrLoadDlog.findViewById(R.id.editLevelText);
                okButton = (Button) saveOrLoadDlog.findViewById(R.id.okButton);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!(levelText.getText().toString().equals(""))) {
                            levelNum = Integer.parseInt(levelText.getText().toString());
                            saveOrLoadDlog.dismiss();
                            new LoadOrSaveTask().execute("save");
                            Toast.makeText(ctx, "Saving game", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                saveOrLoadDlog.show();
                break;
            case R.id.toggleButton:
                Log.d("toggle", "pressed toggle");
                drawingBoard.toggleDataPoints();
                break;
            case R.id.clearAllButton:
                // Clear current path and equidistant point data.
                pathsArray.clear();
                pointsArray.clear();
                // Clear arrays of traces, both bitmap and data
                traceBitmaps.clear();
                traceArray.clear();
                drawingBoard.clear();
                break;
            case R.id.removeTraceButton: //removes most recent trace.
                if(traceArray.size() > 0) {
                    traceBitmaps.remove(traceBitmaps.size() - 1);
                    traceArray.remove(traceArray.size() - 1);
                    pathsArray.clear();
                    pointsArray.clear();
                    drawingBoard.clear();
                }
                break;
            case R.id.toggleAdditiveButton:
                additiveMode = !additiveMode;
                if(additiveMode) {
                    // disable other buttons here..

                }
                break;

        }
        numTraceText.setText("Num traces " + Integer.toString(traceArray.size()));

    }

    private class LoadOrSaveTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params[0].equals("load")) {
                loadLevelFromParse(levelNum);
            }
            else if(params[0].equals("save")) {
                createLevel(levelNum);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
           // Log.d("score", "siiiize" + trace.getPointArray().size());
          //  score = new ScoreManager(trace);
            loadingDialog.dismiss();
        }
    }

    /** Saves this level to parse. To be used only when trying to create new levels **/
    // TODO save trace lengths... using pathmeasure.
    int filesUploaded = 0;
    ParseObject levelObject;
    ArrayList<ParseFile> fileArray;
    public void createLevel(int levelNum) {
        levelObject = new ParseObject("Level");
        // Step 1: build trace data.-----------------------
        JSONArray levelTraces = new JSONArray();
        // Iterate through all the traces
        for(int i = 0; i < traceArray.size(); i++) {
            // Get the scoring points of each trace
            ArrayList<DataPoint> pointsArray = traceArray.get(i).points;
            // points are saved into this array.
            JSONArray jsonPoints = new JSONArray();
            for(int j = 0; j < pointsArray.size(); j++) {
                JSONObject point = new JSONObject();
                try {
                    point.put("x", (int)(pointsArray.get(j).x));
                    point.put("y", (int)(pointsArray.get(j).y));
                    point.put("time", 0);
                    jsonPoints.put(j, point);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Add the trace data to the array that holds all traces
            try {
                levelTraces.put(i, jsonPoints);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Step 2, build image data into parseFiles and upload to cloud.-------------------------
        // When this is done, associate the files to our levelObject.
        fileArray = new ArrayList<ParseFile>();

        // Now save bitmaps for this level.
        for (int i = 0; i < traceBitmaps.size(); i++) {

            // Compress into PNG
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean compress = traceBitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 80, stream);
            if (compress)
                Log.d("parseNetwork", "compression success");
            else
                Log.d("parseNetwork", "compression failed");

            // Convert bytes into ParseFile
            final ParseFile file = new ParseFile("img.png", stream.toByteArray());
            // Save each file to the database
            try {
                file.save();
                fileArray.add(file);
            } catch (ParseException e) {
                Log.d("parseNetwork", "couldn't save file!");
                e.printStackTrace();
            }
        } // end for

        // step 3: associate files to the parse object
        for(int i = 0; i < fileArray.size(); i++) {
            Log.d("parseNetwork", "associating file " + i);
            levelObject.put("trace" + Integer.toString(i), fileArray.get(i));
        }
        // Add other level data
        levelObject.put("trace_array", levelTraces); // this is a JSON array(traces) of JSONArrays(datapoints for each trace) that contain JSON objects with x and y keys(point coords for each datapoint)
        levelObject.put("time_allowed", 20);
        levelObject.put("level_number", levelNum);
        levelObject.put("number_traces", traceBitmaps.size());
        levelObject.saveInBackground();
    }

    ParseObject retrievedLevel;
    /** Loading from online **/
    public void loadLevelFromParse(int levelNum) {
        ParseQuery<ParseObject> levelQuery = ParseQuery.getQuery("Level");
        levelQuery.whereEqualTo("level_number", levelNum);
        levelQuery.setLimit(1);
        try {
            retrievedLevel = levelQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // STEP 1: Retrieve images ----------------------------
        ArrayList<ParseFile> files = new ArrayList<ParseFile>();
        int totalImages = retrievedLevel.getInt("number_traces");
        for(int i = 0; i < totalImages; i++) {
            files.add(retrievedLevel.getParseFile("trace" + Integer.toString(i)));
        }

        // Retrieve parsefile bytes and turn them into bitmaps.
        traceBitmaps = new ArrayList<Bitmap>();
        Bitmap bmp;
        for (int j = 0; j < totalImages; j++) {
            try {
                byte[] data = files.get(j).getData();
                bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                traceBitmaps.add(bmp);
                Log.d("parseNetwork", "Downloaded image " + j);
            } catch (ParseException e) {
                Log.d("parseNetwork", "Download image failure");
                e.printStackTrace();
            }
        } // end all image downloads

        // Step 2: Retrieve trace data -------------------------------
        // this is a JSON array(traces) of JSONArrays(datapoints for each trace) that contain JSON objects with x and y keys(point coords
        JSONArray jsonTraces = retrievedLevel.getJSONArray("trace_array");
        for(int i = 0; i < jsonTraces.length(); i++) {
            JSONArray jsonPointData = null;
            try {
                // Get the array of jsonObjects
                jsonPointData = jsonTraces.getJSONArray(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<DataPoint> tracePoints = new ArrayList<DataPoint>();
            // Iterate through all the points and build a pointArray to add to our traceFile
            for(int j = 0; j < jsonPointData.length(); j++) {
                try {
                    JSONObject jsonPoint = jsonPointData.getJSONObject(j);
                    int x = jsonPoint.getInt("x");
                    int y = jsonPoint.getInt("y");
                    int time = jsonPoint.getInt("time");
                    // Add this datapoint to our pointsarray for this trace
                    tracePoints.add(new DataPoint(x, y, time));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Add it to our buffer of traceFiles.
            TraceFile trace = new TraceFile(traceBitmaps.get(i), tracePoints);
            traceArray.add(trace);
        }
        // Finally initialize the score manager
        score = new ScoreManager(traceArray.get(0));
    }



    public static void updateNumTraces() {
        Log.d("additivemode", " updating numtraces to " + traceArray.size());
        numTraceText.setText("Num traces " + Integer.toString(traceArray.size()));
    }














    // loads from shared prefs
    public void loadFromPrefs() {
        // If there is a game level/trace present in our shared prefs
        Gson gson = new Gson();
        String json = mPrefs.getString("Trace1", "");
        Log.d("loading", "got json");
        if (json != null) {
            trace = gson.fromJson(json, TraceFile.class);
            Log.d("loading", "got trace");
            // draw it on the canvas.
            if (trace != null) {
                Log.d("loading", "drawing trace...");
                drawingBoard.drawTrace(trace.getBitmap());
                // drawingBoard.drawTrace(trace.points);
            }
            else {
                trace = new TraceFile(null, new ArrayList<DataPoint>());

                drawingBoard.setPaintColor(Color.BLUE);
            }
        }
    }

    // Saves in shared prefs
    public void saveToPrefs() {
        Bitmap bmp = drawingBoard.getCanvasBitmap();
        trace = new TraceFile(bmp, pointsArray);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trace);
        prefsEditor.putString("Trace1", json);
        prefsEditor.commit();
    }


    // Save in external folder Android/data/scotts.tots.traceme/files/trace_files/
    public void saveToExternal() {
        // Convert to a string
        Bitmap bmp = drawingBoard.getCanvasBitmap();
        trace = new TraceFile(bmp, pointsArray);
        Gson gson = new Gson();
        String json = gson.toJson(trace);

        File dir = getExternalFilesDir(null);
        File file = new File(dir + "/trace_files/", "trace0.txt"); //trace1 is filename

        try {
            if(file.getParentFile().mkdirs()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.flush();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        // filesystem refresh
        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
    }

    // load from external folder
    public void loadFromExternal() {
        StringBuilder total = new StringBuilder();
        try {
            File inputFile = new File(getExternalFilesDir(null) + "/trace_files/", "trace8.txt");
            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
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
        Log.d("loading", "got trace");
        // draw it on the canvas.
        if (trace != null) {
            Log.d("loading", "drawing trace...");
            drawingBoard.drawTrace(trace.getBitmap());
        }
        else {
            trace = new TraceFile(null, new ArrayList<DataPoint>());
            drawingBoard.setPaintColor(Color.BLUE);
        }

    }


    // load from the raw folder in this project.
    // We can't save into the raw folder.. we must save into external, then
    // transfer to raw if we want to add more levels.
    public void loadFromResources() {
        // current level to load would be something like "game.getCurrLevel()"

        // For now we use the drawingBoards curr level.
        String traceFileName = "trace" + drawingBoard.currentLevel;
        StringBuilder total = new StringBuilder();
        try {
            InputStream inputStream = this.getResources().openRawResource(getResources().getIdentifier(traceFileName,
                    "raw", getPackageName()));
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
        Log.d("loading", "got trace");
        // draw it on the canvas.
        if (trace != null) {
            Log.d("loading", "drawing trace...");
            drawingBoard.drawTrace(trace.getBitmap());
           // drawingBoard.drawTrace(trace.points);
        }
        else {
            trace = new TraceFile(null, new ArrayList<DataPoint>());
            drawingBoard.setPaintColor(Color.BLUE);
        }

    }
}






/*
trace = new TraceFile(null, new ArrayList<DataPoint>());
        drawingBoard.setPaintColor(Color.BLACK);
        drawingBoard.setDashEffect();
        score = new ScoreManager(trace, handler);
 */

