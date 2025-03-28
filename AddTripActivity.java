package com.example.traveldiary;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddTripActivity extends AppCompatActivity {
    EditText editTripName, editTripDescription;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        editTripName = findViewById(R.id.editTripName);
        editTripDescription = findViewById(R.id.editTripDescription);
        dbHelper = new DBHelper(this);
    }

    public void saveTrip(View view) {
        String name = editTripName.getText().toString().trim();
        String description = editTripDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vyplňte všechna pole!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);

        long result = db.insert("trips", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Výlet uložen!", Toast.LENGTH_SHORT).show();
            finish(); // Zavře aktivitu a vrátí se do hlavní obrazovky
        } else {
            Toast.makeText(this, "Chyba při ukládání!", Toast.LENGTH_SHORT).show();
        }
    }
}