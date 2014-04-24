package gamescreens;

/**
 * Created by nlaz on 4/4/14.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import helperClasses.Game;
import helperClasses.GameMenuListItem;

import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<GameMenuListItem>> _listDataChild;
    Game game;
    Typeface roboto_light;
    Typeface roboto_regular;
    Typeface roboto_medium;
    Typeface roboto_lightitalic;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<GameMenuListItem>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        game = ((TraceMeApplication)context.getApplicationContext()).getGame();
        roboto_light = Typeface.createFromAsset(context.getAssets(),"Roboto/Roboto-Light.ttf");
        roboto_regular = Typeface.createFromAsset(context.getAssets(),"Roboto/Roboto-Regular.ttf");
        roboto_medium = Typeface.createFromAsset(context.getAssets(),"Roboto/Roboto-Medium.ttf");
        roboto_lightitalic = Typeface.createFromAsset(context.getAssets(),"Roboto/Roboto-LightItalic.ttf");
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
            convertView.setBackgroundResource(R.drawable.main_screen_card_bottom);
            int padding_in_dp = 20;  // 6 dps
            final float scale = _context.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            convertView.setPadding(0, 0, 0, padding_in_px);
        } else {
            convertView.setPadding(0, 0, 0, 0);
            convertView.setBackgroundResource(R.drawable.main_screen_card_middle);
        }
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

        txtListUsername.setTypeface(roboto_regular);
        txtListSubtext.setTypeface(roboto_lightitalic);
        txtListTime.setTypeface(roboto_lightitalic);
        userImg.setImageBitmap(child.getGameImage(_context));

        if(child.getUsernameString().equals("Loading...")){
            txtListUsername.setTextColor(_context.getResources().getColor(R.color.dark_grey));
            txtListSubtext.setTextColor(_context.getResources().getColor(R.color.dark_grey));
        } else {
            txtListUsername.setTextColor(_context.getResources().getColor(R.color.black));
            txtListSubtext.setTextColor(_context.getResources().getColor(R.color.black));
        }
        // Super duper cool animation
//        Animation animation = AnimationUtils.loadAnimation(_context, R.anim.fadein);
//        convertView.startAnimation(animation);

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
//        if (headerTitle.equals("New Game")){ // NEW GAME BUTTON, Separate Actions
//            if(convertView == null){
//                LayoutInflater infalInflater = (LayoutInflater) this._context
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = infalInflater.inflate(R.layout.new_game_button, null);
//            }
//
//        }
//        else{
////            if(convertView == null){
//                LayoutInflater infalInflater = (LayoutInflater) this._context
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = infalInflater.inflate(R.layout.list_group_view, null);
////            }
//            assert convertView != null;
//            TextView lblListHeader = (TextView) convertView
//                    .findViewById(R.id.lblListHeader);
//            lblListHeader.setTypeface(roboto_medium);
//            lblListHeader.setText(headerTitle);
//
//        }

        if(convertView == null){
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group_view, null);
        }
        assert convertView != null;

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.expListLayout);
        lblListHeader.setTypeface(roboto_medium);
        lblListHeader.setText(headerTitle);

        int padding_in_dp = 20;  // 6 dps
        final float scale = _context.getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        if(getChildrenCount(groupPosition) == 0){
            convertView.setPadding(0, padding_in_px, 0, 0);
        }

        if (headerTitle.equals("New Game!")){
//            lblListHeader.setBackgroundResource(R.drawable.sign_up_selector);
            layout.setBackgroundResource(R.drawable.new_game_selector);
            layout.setPadding(0, padding_in_px/2, 0, padding_in_px/2 );
            lblListHeader.setTextColor(_context.getResources().getColor(R.color.white));
//            convertView.setPadding(0, padding_in_px, 0, padding_in_px);
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

    // Matt: This may be a workaround, but to get things to happen on the spot and
    // update without refreshing everything I had to do this.
    public void delete(int group, int child) {
        _listDataChild.get(_listDataHeader.get(group)).remove(child);
        notifyDataSetChanged();
    }

    public void addGameListItem(int group, GameMenuListItem item) {
        _listDataChild.get(_listDataHeader.get(group)).add(0, item);
        notifyDataSetChanged();
    }
}