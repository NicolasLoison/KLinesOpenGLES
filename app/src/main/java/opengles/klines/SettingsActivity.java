package opengles.klines;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private NumberPicker nbNextPicker, nbAligned9, nbAligned7;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences("game", MODE_PRIVATE);

        setUpPicker();

        nbNextPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            prefs.edit().putInt("nbNext", i1).apply();
        });
        nbAligned9.setOnValueChangedListener((numberPicker, i, i1) -> {
            prefs.edit().putInt("nbAligned9", i1).apply();
        });
        nbAligned7.setOnValueChangedListener((numberPicker, i, i1) -> {
            prefs.edit().putInt("nbAligned7", i1).apply();
        });

        findViewById(R.id.home).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, MainActivity.class)));

        findViewById(R.id.setdefault).setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("nbNext", 3);
            editor.putInt("nbAligned9", 5);
            editor.putInt("nbAligned7", 4);
            editor.apply();
            nbNextPicker.setValue(3);
            nbAligned9.setValue(5);
            nbAligned7.setValue(4);
        });
        findViewById(R.id.resetHighscore).setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore9", 0);
            editor.putInt("highscore7", 0);
            editor.apply();
            Toast.makeText(this, R.string.highscore_reset, Toast.LENGTH_SHORT).show();
        });
    }

    private void setUpPicker() {
        nbNextPicker = findViewById(R.id.nextPicker);
        nbNextPicker.setMinValue(1);
        nbNextPicker.setMaxValue(5);
        nbNextPicker.setValue(prefs.getInt("nbNext", 3));

        nbAligned9 = findViewById(R.id.aligne9Picker);
        nbAligned9.setMinValue(2);
        nbAligned9.setMaxValue(9);
        nbAligned9.setValue(prefs.getInt("nbAligned9", 5));

        nbAligned7 = findViewById(R.id.aligne7Picker);
        nbAligned7.setMinValue(2);
        nbAligned7.setMaxValue(7);
        nbAligned7.setValue(prefs.getInt("nbAligned7", 4));
    }
}