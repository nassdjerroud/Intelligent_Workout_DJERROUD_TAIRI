package intelligent_workout.nassimanis.paris8.intelligent_workout;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;



    public class MainActivity extends Activity {
        private GameView mIntelligentView;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mIntelligentView =(GameView)findViewById(R.id.gameView);
            mIntelligentView.setVisibility(View.VISIBLE);
        }
    }

