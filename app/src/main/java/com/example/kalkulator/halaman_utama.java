package com.example.kalkulator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class halaman_utama extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.halaman_utama);

        // biar tampilan gak ketimpa status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 TOMBOL 1 — pakai anonymous class
        Button button1 = findViewById(R.id.btnWarna);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ini versi anonimous class (langsung di tempat)
                Intent intent = new Intent(halaman_utama.this, Activity_Ketiga.class);
                startActivity(intent);
            }
        });

        // 🔹 TOMBOL 2 — pakai lambda
        Button button2 = findViewById(R.id.todo_button);
        button2.setOnClickListener(v -> {



    }
}



