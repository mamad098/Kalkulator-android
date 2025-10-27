package com.example.kalkulator;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Sederhana: 1 Activity + 1 XML.
 * ListView pakai layout bawaan: android.R.layout.simple_list_item_multiple_choice
 */
public class activity_keempat extends AppCompatActivity {

    private static final String PREF = "todo_prefs";
    private static final String KEY_LIST = "todos";

    private EditText etTodo;
    private Button btnAdd;
    private ListView lvTodos;

    private ArrayList<String> data = new ArrayList<>();
    private ArrayList<Boolean> done = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keempat);

        etTodo = findViewById(R.id.etTodo);
        btnAdd = findViewById(R.id.btnAdd);
        lvTodos = findViewById(R.id.lvTodos);

        // Adapter pakai layout bawaan yang bisa dicentang
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                data
        );
        lvTodos.setAdapter(adapter);

        // Muat data tersimpan
        loadTodos();
        // Terapkan status ceklist ke ListView
        refreshChecks();

        // Tambah item
        btnAdd.setOnClickListener(v -> {
            String text = etTodo.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                etTodo.setError("Tuliskan tugasnya dulu");
                return;
            }
            data.add(0, text);
            done.add(0, false);
            adapter.notifyDataSetChanged();
            refreshChecks();
            etTodo.setText("");
            saveTodos();
            lvTodos.smoothScrollToPosition(0);
        });

        // Tap item = toggle ceklist
        lvTodos.setOnItemClickListener((parent, view, position, id) -> {
            boolean current = done.get(position);
            done.set(position, !current);
            lvTodos.setItemChecked(position, !current);
            saveTodos();
        });

        // Tahan lama = hapus item
        lvTodos.setOnItemLongClickListener((parent, view, position, id) -> {
            String itemText = data.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Tugas")
                    .setMessage("Hapus: \"" + itemText + "\" ?")
                    .setPositiveButton("Hapus", (DialogInterface dialog, int which) -> {
                        data.remove(position);
                        done.remove(position);
                        adapter.notifyDataSetChanged();
                        refreshChecks();
                        saveTodos();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
            return true; // event dikonsumsi
        });
    }

    /** Terapkan status done[] ke tampilan ListView (checked/unchecked). */
    private void refreshChecks() {
        // Pastikan size sama
        while (done.size() < data.size()) done.add(false);
        while (done.size() > data.size()) done.remove(done.size() - 1);

        for (int i = 0; i < data.size(); i++) {
            lvTodos.setItemChecked(i, done.get(i));
        }
    }

    /** Simpan ke SharedPreferences sebagai JSON. */
    private void saveTodos() {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            JSONObject o = new JSONObject();
            try {
                o.put("text", data.get(i));
                o.put("done", done.get(i));
                arr.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences sp = getSharedPreferences(PREF, MODE_PRIVATE);
        sp.edit().putString(KEY_LIST, arr.toString()).apply();
    }

    /** Muat dari SharedPreferences. */
    private void loadTodos() {
        data.clear();
        done.clear();

        SharedPreferences sp = getSharedPreferences(PREF, MODE_PRIVATE);
        String json = sp.getString(KEY_LIST, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                data.add(o.optString("text", ""));
                done.add(o.optBoolean("done", false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}