package gamescreens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import helperClasses.Game;
import scotts.tots.traceme.R;
import scotts.tots.traceme.TraceMeApplication;

public class LevelSelectActivity extends Activity {
    private TextView level1;

    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        level1 = (TextView) findViewById(R.id.level1);

        game = ((TraceMeApplication)this.getApplicationContext()).getGame();
        level1.setOnClickListener(viewListener);
    }

    View.OnClickListener viewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.level1:
                    game.setLevel(1);
                    startActivity(new Intent(LevelSelectActivity.this, GameActivity.class));
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.level_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
