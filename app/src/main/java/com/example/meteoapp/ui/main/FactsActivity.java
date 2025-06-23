package com.example.meteoapp.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.meteoapp.BaseActivity;
import com.example.meteoapp.BaseActivity;
import com.example.meteoapp.R;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class FactsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facts);

        setTitle("Știați că...");

        LinearLayout layoutUv = findViewById(R.id.layoutUv);
        LinearLayout extraUvContent = findViewById(R.id.extraUvContent);

        layoutUv.setOnClickListener(v -> {
            if (extraUvContent.getVisibility() == View.GONE) {
                extraUvContent.setVisibility(View.VISIBLE);
            } else {
                extraUvContent.setVisibility(View.GONE);
            }
        });
        LinearLayout layoutPollution = findViewById(R.id.layoutPollution);
        LinearLayout extraPollutionContent = findViewById(R.id.extraPollutionContent);

        layoutPollution.setOnClickListener(v -> {
            if (extraPollutionContent.getVisibility() == View.GONE) {
                extraPollutionContent.setVisibility(View.VISIBLE);
            } else {
                extraPollutionContent.setVisibility(View.GONE);
            }
        });

        LinearLayout layoutPressure = findViewById(R.id.layoutPressure);
        LinearLayout extraPressureContent = findViewById(R.id.extraPressureContent);

        layoutPressure.setOnClickListener(v -> {
            if (extraPressureContent.getVisibility() == View.GONE) {
                extraPressureContent.setVisibility(View.VISIBLE);
            } else {
                extraPressureContent.setVisibility(View.GONE);
            }
        });

    }
}
