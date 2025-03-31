package com.example.traveldiary;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    ListView tripListView;
    ArrayList<String> tripList;
    ArrayAdapter<String> adapter;
    DBHelper dbHelper;
    SearchView searchView;
//system zavola onCreate metodu. vytvori dbHelper. udela aby kazdy vylet byl na jednom radku. nastavi co se stane, kdyz nekdo klikne na neco.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tripListView = findViewById(R.id.tripListView);
        searchView = findViewById(R.id.searchView);
        dbHelper = new DBHelper(this);

        tripList = dbHelper.getTrips();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(tripList));
        tripListView.setAdapter(adapter);

        tripListView.setOnItemClickListener((parent, view, position, id) -> {
            String name = adapter.getItem(position);
            Cursor cursor = dbHelper.getTripByName(name);
//pokud byl vylet nalezen, vytvori intent, ktery prepne na TripDetailActivity a zavre kurzor, aby se uvolnila pamet
            if (cursor != null && cursor.moveToFirst()) {
                double lat = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double lon = cursor.getDouble(cursor.getColumnIndex("longitude"));

                Intent intent = new Intent(MainActivity.this, TripDetailActivity.class);
                intent.putExtra("tripName", name);
                intent.putExtra("latitude", lat);
                intent.putExtra("longitude", lon);
                startActivity(intent);
            }

            if (cursor != null) {
                cursor.close();
            }
        });

        //vyhledava podle nazvu
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
//vytvori novy intent, ktery prepne z aktivity MainActivity na AddTripActivity
    public void addTrip(View view) {
        Intent intent = new Intent(this, AddTripActivity.class);
        startActivity(intent);
    }
//zivotni cyklus, vola se vzdy, kdyz se MainActivity objevi. nacte aktualni seznam.
    @Override
    protected void onResume() {
        super.onResume();
        tripList.clear();
        tripList.addAll(dbHelper.getTrips());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(tripList));
        tripListView.setAdapter(adapter);
    }
}
