package gamescreens;

/**
 * Created by nlaz on 4/4/14.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;
import helperClasses.GameStatus;
import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<GameMenuListItem>> _listDataChild;
    Game game;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<GameMenuListItem>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        game = ((TraceMeApplication)context.getApplicationContext()).getGame();
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

        if (childPosition == this.getChildrenCount(groupPosition) - 1) {
            convertView.setPadding(0, 0, 0, 20);
        } else
            convertView.setPadding(0, 0, 0, 0);

        TextView txtListUsername = (TextView) convertView
                .findViewById(R.id.username_item);
        TextView txtListTime = (TextView) convertView
                .findViewById(R.id.time_item);
        TextView txtListSubtext = (TextView) convertView
                .findViewById(R.id.game_status);
        ImageView userImg = (ImageView) convertView
                .findViewById(R.id.username_img);

        txtListUsername.setText(child.getUsernameString());
        txtListTime.setText(child.getLastUpdatedString());
        txtListSubtext.setText(child.getStatusString());
        userImg.setImageBitmap(child.getGameImage(_context));

        convertView.setOnClickListener(getGameItemsListener(child));

        // Super duper cool animation
        Animation animation = AnimationUtils.loadAnimation(_context, R.anim.fadein);
        convertView.startAnimation(animation);

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
                convertView = infalInflater.inflate(R.layout.list_group_view, null);
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
        Game gameObj = gameListItem.getGameParseObject();


        // Listener for a Game that is awaiting an opponent.
        if (gameObj.getInt("game_status") == GameStatus.WAITING_FOR_OPPONENT.id) {
            return getWaitingOpponentListener(gameObj);
        } else if (gameObj.getInt("game_status") == GameStatus.CHALLENGED.id) {
            if (gameObj.getParseUser("player_one").getUsername().equals(ParseUser.getCurrentUser().getUsername()))
                return getChallengerListener(gameObj); // "waiting for an opponent...."
            else
                return getChallengedListener(gameObj); // "challenged by ...."
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
                                    // From Aaron: Instead of sending a notification to player B that the game was cancelled,
                                    // We can instead verify the game is still valid when we get it from the listview,
                                    // If it is NOT valid, then player A cancelled the game, so we notify player B
                                    // that A cancelled the game with a "toast" or a simple message instead of notification, and refresh the listview?
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

    public View.OnClickListener getChallengedListener(final Game obj) {
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

                // Accept
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
                                    // From Aaron: We should just send a push notification after player2 finishes his/her game.
                                    // So we don't spam the user. So for now I'll leave this blank and send a notification at the end of the game saying
                                    // "player B accepted your challenge, results are in!" when B finishes the game.
                                }
                                else
                                    e.printStackTrace();
                            }
                        });
                        dlog.dismiss();

                        // Start game
                        //game = obj;
                        ((TraceMeApplication)_context.getApplicationContext()).setGame(obj);
                        _context.startActivity(new Intent(_context, GameActivity.class));
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
                                    // Send the user a push notification for decline
                                    ParseUser p1 = obj.getParseUser("player_one");
                                    ParsePush push = new ParsePush();
                                    push.setChannel(p1.getUsername());
                                    push.setMessage("Player " + ParseUser.getCurrentUser().getUsername() + " has declined your challenge");
                                    push.sendInBackground();
                                    // TODO: refresh lists
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

    public View.OnClickListener getActiveGameListener(final  Game obj) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(_context,
                        "Starting game",
                        Toast.LENGTH_SHORT).show();
                // Starts game
               // game = obj;
                ((TraceMeApplication)_context.getApplicationContext()).setGame(obj);
                _context.startActivity(new Intent(_context, GameActivity.class));
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