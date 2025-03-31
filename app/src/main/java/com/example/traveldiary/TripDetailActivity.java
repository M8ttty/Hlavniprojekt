package com.example.traveldiary;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
//vytvori se verejna trida, aktivita zacne fungovat az bdue ready mapa
public class TripDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
//deklarace promennych
    EditText tripNameEditText, tripDescriptionEditText;
    ImageView photoDetail;
    DBHelper dbHelper;
    int tripId;
    double latitude, longitude;
//zobrazuje mapu s mistem vyletu
    MapView mapView;
//uklada stav mapy, nevedel jsem jak to udelat tak jsem si pomohl pomoci AI
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
//inicializace, nacitani rozvrzeni, propojuje xml s kodem
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        tripNameEditText = findViewById(R.id.tripNameEditText);
        tripDescriptionEditText = findViewById(R.id.tripDescriptionEditText);
        photoDetail = findViewById(R.id.photoDetail);
        mapView = findViewById(R.id.mapView);
        dbHelper = new DBHelper(this);
//inicializace mapy, zjisti nazev ohledne mapy, pomohl jsem si s AI
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        String tripName = getIntent().getStringExtra("tripName");
        loadTripDetails(tripName);
    }
//nacte detaili vyletu z databaze
    private void loadTripDetails(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, description, latitude, longitude, photoUri FROM trips WHERE name=?", new String[]{name});
//sloupce
        if (cursor.moveToFirst()) {
            tripId = cursor.getInt(0);
            String description = cursor.getString(1);
            latitude = cursor.getDouble(2);
            longitude = cursor.getDouble(3);
            String photoUriString = cursor.getString(4);

            tripNameEditText.setText(name);
            tripDescriptionEditText.setText(description);
//jestli ma vylet fotku, nacte fotku
            if (photoUriString != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(photoUriString));
                    Drawable drawable = Drawable.createFromStream(inputStream, photoUriString);
                    photoDetail.setImageDrawable(drawable);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Nelze zobrazit fotku", Toast.LENGTH_SHORT).show();
                }
            }
        }

        cursor.close();
        db.close();
    }
//metoda, ktera se vola kdyz je mapa ready, nacte mapu i s markerem a pomaha pro editaci markeru na mape, tady jsem si nevedel rady a pomohl jsem si s AI
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Místo výletu"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

        googleMap.setOnMapClickListener(latLng -> {
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Nové místo výletu"));
        });
    }

    private boolean tripNameExistsForOtherId(String name, int currentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM trips WHERE name = ? AND id != ?",
                new String[]{name, String.valueOf(currentId)}
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
//pro overeni jestli existuje nazev  vyletu, ktery chci zrovna vyuzit, otevre databazi a hleda
    public void updateTrip(View view) {
        String newName = tripNameEditText.getText().toString().trim();
        String newDescription = tripDescriptionEditText.getText().toString().trim();

        if (newName.isEmpty() || newDescription.isEmpty()) {
            Toast.makeText(this, "Vyplňte název i popis", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripNameExistsForOtherId(newName, tripId)) {
            Toast.makeText(this, "Jiný výlet už má tento název!", Toast.LENGTH_SHORT).show();
            return;
        }
//pro upravu vylet v databazi
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("description", newDescription);
        values.put("latitude", latitude);
        values.put("longitude", longitude);

        int updatedRows = db.update("trips", values, "id = ?", new String[]{String.valueOf(tripId)});
        db.close();
//jestli byl vylet upraven tak to napise vylet upraven, pokud vylet neexistuje nebo se nic neupravilo napise to chybu
        if (updatedRows > 0) {
            Toast.makeText(this, "Výlet upraven", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Chyba při úpravě", Toast.LENGTH_SHORT).show();
        }
    }
//pro odstraneni vyletu, zepta se jestli chci odstranit vylet nebo ne a jestli jo tak to napise vylet smazan
    public void deleteTrip(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Smazat výlet")
                .setMessage("Opravdu chcete smazat tento výlet?")
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("trips", "id=?", new String[]{String.valueOf(tripId)});
                        db.close();
                        Toast.makeText(TripDetailActivity.this, "Výlet smazán", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Ne", null)
                .show();
    }
    //pomoc pro mapu, aby fungovala spravne, toto jsem nevedel jak vytvorit a pouzil jsem na pomoc AI
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
