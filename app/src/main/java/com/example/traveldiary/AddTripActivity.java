package com.example.traveldiary;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
//vytvori se verejna trida, aktivita bude fungovat az bude ready mapa
public class AddTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    //deklarace promennych
    EditText editTripName, editTripDescription;
    DBHelper dbHelper;
    MapView mapView;
    GoogleMap gMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LatLng selectedLocation;
    Uri selectedPhotoUri;
    ImageView photoPreview;

    //identifikace na opravneni k poloze, nevedel jsem jak to udelat tak jsem si pomohl pomoci AI
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    //id na vyber fotky, nevedel jsem jak to udelat tak jsem si pomohl pomoci AI
    private static final int PHOTO_PICK_REQUEST = 2001;
    //klic pro ulozeni stavu mapy, nevedel jsem jak to udelat tak jsem si pomohl pomoci AI
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

//inicializace aktivity, nastavi vse pro pridani vyletu, propojuje xml s kodem
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        editTripName = findViewById(R.id.editTripName);
        editTripDescription = findViewById(R.id.editTripDescription);
        photoPreview = findViewById(R.id.photoPreview);
        mapView = findViewById(R.id.mapView);
        dbHelper = new DBHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

//tato cast slouzi k vyhledavani mista na mape, nevedel jsem jak to udelat tak jsem si pomohl pomoci AI
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
//spravne zobrazeni mapy
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }
//pro mapu, pridava marker, zvetseni zmenenseni
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setZoomGesturesEnabled(true);
        gMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            gMap.clear();
            gMap.addMarker(new MarkerOptions().position(latLng).title("Vybraná poloha"));
        });

        requestLocation();
    }
//zepta se na polohu, na toto jsem pouzil AI
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
                gMap.addMarker(new MarkerOptions().position(selectedLocation).title("Moje poloha"));
            }
        });
    }
//kdyz se hleda mesto ci cast mesta, toto to hleda, nevedel jsem jak to vytvorit tak jsem pouzil AI
    private void searchLocation(String query) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(location).title("Výsledek hledání"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                selectedLocation = location;
            } else {
                Toast.makeText(this, "Místo nenalezeno", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Chyba při hledání", Toast.LENGTH_SHORT).show();
        }
    }
//overeuje jestli je stejny nazev vyletu v databazi
    private boolean tripNameExists(String name) {
        Cursor cursor = dbHelper.getTripByName(name);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
//vyvola se, kdyz clovek klikne na tlacitko ulozit vylet a ulozi to vsechny informace, popripadne napise, jestli clovek zadal vsechny informace
    public void saveTrip(View view) {
        String name = editTripName.getText().toString().trim();
        String description = editTripDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || selectedLocation == null) {
            Toast.makeText(this, "Vyplňte všechna pole a vyberte místo!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripNameExists(name)) {
            Toast.makeText(this, "Výlet s tímto názvem už existuje!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("latitude", selectedLocation.latitude);
        values.put("longitude", selectedLocation.longitude);

        if (selectedPhotoUri != null) {
            values.put("photoUri", selectedPhotoUri.toString());
        }

        long result = db.insert("trips", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Výlet uložen!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Chyba při ukládání!", Toast.LENGTH_SHORT).show();
        }
    }
//metoda pro vybyrani fotky
    public void selectPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_PICK_REQUEST);
    }
//metoda, ktera zpracovava vysledek z galerie (zjisti jestli je fotka v poho atd.)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri originalUri = data.getData();
            selectedPhotoUri = saveImageToInternalStorage(originalUri);
            if (selectedPhotoUri != null) {
                photoPreview.setImageURI(selectedPhotoUri);
            } else {
                Toast.makeText(this, "Chyba při ukládání fotky", Toast.LENGTH_SHORT).show();
            }
        }
    }
//uklada fotku do InternalStorage a zmensi ji a komprimuje, aby nebyla moc velka a byla dobre videt cela. Na toto jsem pouzil AI, protoze jsem nevedel jak to udelat.
    private Uri saveImageToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Zmenšení velikosti (např. max 1024px na výšku/šířku)
            int maxSize = 1024;
            int width = original.getWidth();
            int height = original.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);

            int newWidth = Math.round(scale * width);
            int newHeight = Math.round(scale * height);

            Bitmap scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);

            // Uložení jako JPEG s kompresí 80 %
            String fileName = "trip_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//pomoc pro mapu, aby fungovala spravne, toto jsem nevedel jak vytvorit a pouzil jsem na pomoc AI
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}
