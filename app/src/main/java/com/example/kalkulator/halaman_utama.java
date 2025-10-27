package com.example.kalkulator;

import android.content.Intent;
import android.os.Bundle;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;



        });
        // setOnClickListener adalah sebuah inner interface yang digunakan untuk menangani klik pada tombol.
        Button button = findViewById(R.id.btn_kalkulator);
        button.setOnClickListener(v -> {  // Menggunakan lambda untuk menangani klik
            Intent intent = new Intent(halaman_utama.this, Kalkulator.class);
            startActivity(intent);
        });

        Button button2 = findViewById(R.id.btnWarna);
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(halaman_utama.this, Activity_Ketiga.class);
            startActivity(intent);
        });


        Button button3 = findViewById(R.id.button);
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(halaman_utama.this, activity_keempat.class);
            startActivity(intent);
        });



    }
}