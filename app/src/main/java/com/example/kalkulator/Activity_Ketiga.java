package com.example.kalkulator;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Activity_Ketiga extends AppCompatActivity {

    private ConstraintLayout mainLayout;
    private boolean isWhite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ketiga);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainLayout = findViewById(R.id.main);
        Button button = findViewById(R.id.ubahwarna);
        Button btnBrowser = findViewById(R.id.btnBrowser); // misal tombol baru buat buka Google

        // 🔹 Tombol untuk ubah warna background
        button.setOnClickListener(v -> {
            if (isWhite) {
                mainLayout.setBackgroundColor(Color.parseColor("#CAF0F8"));
            } else {
                mainLayout.setBackgroundColor(Color.parseColor("#FAF3FF"));
            }
            isWhite = !isWhite;
        });

        // 🔹 Tombol lain untuk buka browser
        btnBrowser.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
            startActivity(intent);
        });
    }
}
