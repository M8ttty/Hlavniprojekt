package com.example.traveldiary;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView tripListView;
    ArrayList<String> tripList;
    ArrayAdapter<String> adapter;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tripListView = findViewById(R.id.tripListView);
        dbHelper = new DBHelper(this);
        tripList = dbHelper.getTrips();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tripList);
        tripListView.setAdapter(adapter);

        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, TripDetailActivity.class);
                intent.putExtra("tripName", tripList.get(position));
                startActivity(intent);
            }
        });
    }

    public void addTrip(View view) {
        Intent intent = new Intent(this, AddTripActivity.class);
        startActivity(intent);
    }
}
