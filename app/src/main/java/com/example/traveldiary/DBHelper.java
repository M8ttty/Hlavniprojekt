package com.example.traveldiary;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
//vytvori tridu DBHelper, zdedena z SQLiteOpenHelper
public class DBHelper extends SQLiteOpenHelper {
//kam se ulozi databaze
    private static final String DATABASE_NAME = "TravelDiary.db";
    //pro aktualizace
    private static final int DATABASE_VERSION = 1;
//pro nove instance
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
//metoda, ktera se vytvori, kdyz je databaze poprve vytvorena a vytvori tabulku
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE trips (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "description TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "photoUri TEXT)";
        db.execSQL(createTableQuery);
    }
    //aktivuje se, kdyz se zmeni verze databaze, vytvoril jsem pomoci AI
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trips");
        onCreate(db);
    }
//nacte vsechny vylety
    public ArrayList<String> getTrips() {
        ArrayList<String> tripList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM trips", null);

        while (cursor.moveToNext()) {
            tripList.add(cursor.getString(0));
        }

        cursor.close();
        db.close();
        return tripList;
    }
//vyhleda vylet v databazi
    public Cursor getTripByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM trips WHERE name = ?", new String[]{name});
    }
}
