package gamescreens;

/**
 * Created by nlaz on 4/4/14.
 */

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<GameMenuListItem>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<GameMenuListItem>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final GameMenuListItem child = (GameMenuListItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_multiplayer, null);
        }

        TextView txtListUsername = (TextView) convertView
                .findViewById(R.id.username_item);
        TextView txtListTime = (TextView) convertView
                .findViewById(R.id.time_item);

        txtListUsername.setText(child.getStatusString());
        txtListTime.setText(child.getLastUpdatedString());

        convertView.setOnClickListener(getGameItemsListener(child));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (groupPosition == 0){ // NEW GAME BUTTON, Separate Actions
            if(convertView == null){
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.new_game_button, null);
            }

        }
        else{
            if(convertView == null){
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.group_view, null);
            }
            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public View.OnClickListener getGameItemsListener(GameMenuListItem gameListItem) {
        ParseObject gameObj = gameListItem.getGameParseObject();


        // Listener for a Game that is awaiting an opponent.
        if (gameObj.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
            return getWaitingOpponentListener(gameObj);
        } else if (gameObj.getInt("game_status") == GameStatus.CHALLENGED.id) {
            if (gameObj.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return getChallengerListener(gameObj);
            else
                return getChallengedListener(gameObj);
        } else if (gameObj.getInt("game_status") == GameStatus.IN_PROGRESS.id) {
            return getActiveGameListener(gameObj);
        }

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("getChildView", "onClickPressed");

            }
        };
    }

    public View.OnClickListener getWaitingOpponentListener(final ParseObject obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("getWaitingOpponentListener", "Waiting for opponent pressed.");

                final Dialog dlog = new Dialog(_context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
                dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dlog.setContentView(R.layout.generic_dialog);

                View cancelGameButton  = dlog.findViewById(R.id.yes);
                View dismissDlogButton = dlog.findViewById(R.id.no);

                ((TextView) dlog.findViewById(R.id.prompt)).setText("Would you like to stop looking for an opponent?");

                cancelGameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        obj.put("game_status", GameStatus.INVALID.id);
                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("getWaitingOpponentListener", "game cancelled successfully");
                                    Toast.makeText(_context,
                                            "Cancelled Successfully",
                                            Toast.LENGTH_LONG).show();
                                }
                                else
                                    e.printStackTrace();
                            }
                        });
                        dlog.dismiss();
                    }
                });

                dismissDlogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlog.dismiss();
                    }
                });
                dlog.show();
            }
        };
    }

    public View.OnClickListener getChallengerListener(final ParseObject obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("getChallengerListener", "Challenger Listener Pressed");

                final Dialog dlog = new Dialog(_context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
                dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dlog.setContentView(R.layout.generic_dialog);

                View yesDlogButton  = dlog.findViewById(R.id.yes);
                View noDlogButton = dlog.findViewById(R.id.no);

                ((TextView) dlog.findViewById(R.id.prompt)).setText("Would you like to cancel your challenge?");

                yesDlogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        obj.put("game_status", GameStatus.INVALID.id);
                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("getWaitingOpponentListener", "game cancelled successfully");
                                    Toast.makeText(_context,
                                            "Challenge Cancelled Successfully",
                                            Toast.LENGTH_LONG).show();

                                    // TODO: Send the user a push notification for cancelled game.
                                }
                                else
                                    e.printStackTrace();
                            }
                        });
                        dlog.dismiss();
                    }
                });

                noDlogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlog.dismiss();
                    }
                });
                dlog.show();
            }
        };
    }

    public View.OnClickListener getChallengedListener(final ParseObject obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("getChallengerListener", "Challenged Listener Pressed");

                final Dialog dlog = new Dialog(_context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
                dlog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dlog.setContentView(R.layout.generic_dialog);

                View acceptDlogButton  = dlog.findViewById(R.id.yes);
                View declineDlogButton = dlog.findViewById(R.id.no);

                ((TextView) dlog.findViewById(R.id.prompt)).setText("Accept the challenge and play?");
                ((Button) acceptDlogButton).setText("Accept");
                ((Button) declineDlogButton).setText("Decline");

                acceptDlogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        obj.put("game_status", GameStatus.IN_PROGRESS.id);
                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("getWaitingOpponentListener", "game accepted successfully");
                                    // TODO: Send the user a push notification for accepted
                                }
                                else
                                    e.printStackTrace();
                            }
                        });
                        dlog.dismiss();

                        // TODO: Render the actual game. The game has been accepted.
                    }
                });

                declineDlogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        obj.put("game_status", GameStatus.INVALID.id);
                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(_context,
                                            "Declined Successfully",
                                            Toast.LENGTH_LONG).show();
                                    // TODO: Send the user a push notification for decline
                                }
                                else
                                    e.printStackTrace();
                            }
                        });
                        dlog.dismiss();
                    }
                });
                dlog.show();
            }
        };
    }

    public View.OnClickListener getActiveGameListener(final  ParseObject obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(_context,
                        "TODO: Show actual game.",
                        Toast.LENGTH_LONG).show();
            }
        };
    }

    // TODO: Currently no game over status.
    public View.OnClickListener getGameOverListener(final  ParseObject obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(_context,
                        "TODO: Show GameOver.",
                        Toast.LENGTH_LONG).show();
            }
        };
    }
}