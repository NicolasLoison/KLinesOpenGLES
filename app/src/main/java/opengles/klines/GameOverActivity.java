package opengles.klines;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game_over);

        findViewById(R.id.retry).setOnClickListener(v ->
            startActivity(new Intent(GameOverActivity.this, OpenGLES30Activity.class)));

        findViewById(R.id.home).setOnClickListener(v ->
                startActivity(new Intent(GameOverActivity.this, MainActivity.class)));


        int lastscore = prefs.getInt("lastScore", 0);

        TextView scoreTxt = findViewById(R.id.score);
        scoreTxt.setText(getString(R.string.final_score, lastscore));

        TextView newBest = findViewById(R.id.isbest);
        if (lastscore != 0 && prefs.getBoolean("isNewBest", false)) {
            newBest.setVisibility(View.VISIBLE);
            prefs.edit().putBoolean("isNewBest", false).apply();
        }

    }
}