package com.example.wattwise;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wattwise.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "bills";
    private static final String COL_ID = "id";
    private static final String COL_MONTH = "month";
    private static final String COL_UNIT = "unit";
    private static final String COL_TOTAL = "total_charges";
    private static final String COL_REBATE = "rebate";
    private static final String COL_FINAL = "final_cost";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MONTH + " TEXT, " +
                COL_UNIT + " INTEGER, " +
                COL_TOTAL + " REAL, " +
                COL_REBATE + " REAL, " +
                COL_FINAL + " REAL)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertBill(String month, int unit, double totalCharges, double rebate, double finalCost) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_MONTH, month);
        values.put(COL_UNIT, unit);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_REBATE, rebate);
        values.put(COL_FINAL, finalCost);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public ArrayList<Bill> getAllBills() {

        ArrayList<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Bill bill = new Bill(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MONTH)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNIT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REBATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_FINAL))
                );

                billList.add(bill);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return billList;
    }

    public Bill getBillById(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        if (cursor != null && cursor.moveToFirst()) {

            Bill bill = new Bill(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_MONTH)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNIT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REBATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_FINAL))
            );

            cursor.close();
            return bill;
        }

        return null;
    }

    public boolean updateBill(int id, String month, int unit, double totalCharges, double rebate, double finalCost) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_MONTH, month);
        values.put(COL_UNIT, unit);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_REBATE, rebate);
        values.put(COL_FINAL, finalCost);

        int result = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteBill(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }
}