package com.example.wattwise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    TextView textGithubUrl;
    Button buttonBackAbout, buttonHomeAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        textGithubUrl = findViewById(R.id.textGithubUrl);
        buttonBackAbout = findViewById(R.id.buttonBackAbout);
        buttonHomeAbout = findViewById(R.id.buttonHomeAbout);

        buttonBackAbout.setOnClickListener(view -> finish());

        buttonHomeAbout.setOnClickListener(view -> {
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        textGithubUrl.setOnClickListener(view -> {
            String url = "https://github.com/rosinszn/WattWise";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}