package opengles.klines;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("game", MODE_PRIVATE);

        Button grid9 = findViewById(R.id.ninegrid);
        Button grid7 = findViewById(R.id.sevengrid);
        Intent intent = new Intent(MainActivity.this, OpenGLES30Activity.class);
        grid9.setOnClickListener(
                v -> {
                    setGridType(9);
                    startActivity(intent);
                }
        );
        grid7.setOnClickListener(
                v -> {
                    setGridType(7);
                    startActivity(intent);
                }
        );

        int highscore9 = prefs.getInt("highscore9", 0);
        int highscore7 = prefs.getInt("highscore7", 0);

        TextView score9Txt = findViewById(R.id.highscore9Text);
        TextView score7Txt = findViewById(R.id.highscore7Text);
        score9Txt.setText(getString(R.string.highscore9, highscore9));
        score7Txt.setText(getString(R.string.highscore7, highscore7));

    }

    private void setGridType(int type) {
        SharedPreferences.Editor editor  = prefs.edit();
        editor.putInt("gridType", type);
        editor.apply();
    }
}