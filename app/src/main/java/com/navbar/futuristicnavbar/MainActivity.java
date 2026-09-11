package com.navbar.futuristicnavbar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.navbar.futuristicbottomnav.FuturisticBottomNav;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FuturisticBottomNav bottomNav = findViewById(R.id.bottomNav);
        TextView contentLabel = findViewById(R.id.contentLabel);

        bottomNav.setOnItemSelectedListener((index, item) ->
                contentLabel.setText(item.getLabel()));
    }
}