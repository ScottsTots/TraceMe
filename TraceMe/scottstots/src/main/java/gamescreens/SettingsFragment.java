package gamescreens;

/**
 * Created by Matthew on 4/16/14.
 */

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import helperClasses.UsefulMethods;
import scotts.tots.traceme.R;

/**
 * Fragment that appears in the "content_frame". This fragment shows the game lobbies, and
 * game activity.
 */
public class SettingsFragment extends Fragment {// implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";

    Button notificationButton;
    Button saveSettingsButton;
    ImageButton profilePictureButton;
    Bitmap newImage;

    boolean notifications_on;
    boolean picture_changed;

    public SettingsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        notificationButton = (Button) view.findViewById(R.id.pushNotificationButton);
        saveSettingsButton = (Button) view.findViewById(R.id.saveSettings);
        profilePictureButton = (ImageButton) view.findViewById(R.id.changeImageButton);

        picture_changed = false;
        notificationButton.setOnClickListener(notificationListener);
        saveSettingsButton.setOnClickListener(saveListener);
        profilePictureButton.setOnClickListener(changePictureListener);

        // Load up the user's current picture
        byte[] imgBytes = ParseUser.getCurrentUser().getBytes("profile_picture");
        if (imgBytes != null) {
            Bitmap bitmap = UsefulMethods.getParseUserPicture(ParseUser.getCurrentUser(), getActivity().getApplicationContext());
            profilePictureButton.setImageBitmap(bitmap);
        }

        notifications_on = ParseUser.getCurrentUser().getBoolean("notification");
        setNotificationButton();
    }

    int RESULT_LOAD_IMAGE = 10;
    View.OnClickListener changePictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    };

    View.OnClickListener notificationListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setNotificationButton();
        }
    };

    private void setNotificationButton() {
        if (notifications_on) {
            notificationButton.setText("Off");
        } else {
            notificationButton.setText("On");
        }

        notifications_on = !notifications_on;
    }
    View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ParseUser currentUser = ParseUser.getCurrentUser();

            if (picture_changed) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                newImage.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] data = stream.toByteArray();
                currentUser.put("profile_picture", data);
            }
            currentUser.put("notifications", notifications_on);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        Log.d("Save", "Save successful");
                    else {
                        Log.d("Save", "Save unsuccessful");
                        Log.d("Error", e.getMessage());
                    }
                }
            });
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && data != null) {
            Uri imageUri = data.getData();
            try {
                newImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                newImage = Bitmap.createScaledBitmap(newImage, 150, 150, false);
                profilePictureButton.setImageBitmap(newImage);

                picture_changed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}