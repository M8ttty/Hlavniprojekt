package com.example.traveldiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TripDetailActivity extends AppCompatActivity {
    TextView tripNameTextView, tripDescriptionTextView;
    DBHelper dbHelper;
    String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        tripNameTextView = findViewById(R.id.tripNameTextView);
        tripDescriptionTextView = findViewById(R.id.tripDescriptionTextView);
        dbHelper = new DBHelper(this);

        // Získání názvu výletu z Intentu
        tripName = getIntent().getStringExtra("tripName");
        loadTripDetails();
    }

    private void loadTripDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT description FROM trips WHERE name=?", new String[]{tripName});

        if (cursor.moveToFirst()) {
            String description = cursor.getString(0);
            tripNameTextView.setText(tripName);
            tripDescriptionTextView.setText(description);
        }
        cursor.close();
        db.close();
    }

    public void deleteTrip(View view) {
        // Potvrzení mazání výletu
        new AlertDialog.Builder(this)
                .setTitle("Smazat výlet")
                .setMessage("Opravdu chcete smazat tento výlet?")
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("trips", "name=?", new String[]{tripName});
                        db.close();
                        Toast.makeText(TripDetailActivity.this, "Výlet smazán", Toast.LENGTH_SHORT).show();
                        finish(); // Zavře aktivitu a vrátí se zpět
                    }
                })
                .setNegativeButton("Ne", null)
                .show();
    }
}