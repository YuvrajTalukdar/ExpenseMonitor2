package com.yuvraj.expensemonitor.java;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class database_handler extends SQLiteOpenHelper {

    private static int DATABASE_VERSION =1;
    private static final String DATABASE_NAME="database.db";
    private static final String category_table_name="category_table",expense_table_name="expense_table";
    private static final String id="ID",name="NAME";
    private static final String day="DAY",month="MONTH",year="YEAR",cost="COST",item_name="ITEM_NAME",item_category="CATEGORY";
    private static final String[] default_category_names=new String[]{"Other","Power Bill","House Rent","Food","Travel","Water Bill"};

    public database_handler(@Nullable Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

        SQLiteDatabase db = getReadableDatabase();
        String check_presence_of_table="SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '"+category_table_name+"'";
        Cursor c=db.rawQuery(check_presence_of_table,null);
        c.moveToFirst();
        if(c.getInt(c.getColumnIndex("COUNT(*)"))==0)
        {
            c.close();
            String category_table_query="CREATE TABLE IF NOT EXISTS "+category_table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+name+" TEXT);";
            db.execSQL(category_table_query);
            for(int a=0;a<default_category_names.length;a++)
            {   add_new_category(default_category_names[a]);}
        }
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String expense_table_query="CREATE TABLE IF NOT EXISTS "+expense_table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+item_name+" TEXT, "+item_category+" TEXT, "+cost+" REAL, "+day+" INTEGER, "+month+" INTEGER, "+year+" INTEGER);";
        db.execSQL(expense_table_query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+category_table_name);
        db.execSQL("DROP TABLE IF EXISTS "+expense_table_name);
        onCreate(db);
    }

    public void rename_category(int category_id,String new_category_name)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues new_value= new ContentValues();
        new_value.put(name,new_category_name);
        db.update(category_table_name,new_value,"id='"+category_id+"'",null);

        //rename category in expense table
        db.close();
    }

    //category database functions

    public long add_new_category(String category_name)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(name,category_name);
        long l=db.insert(category_table_name,null,contentValues);
        db.close();
        return l;
    }

    public int remove_category(int category_id)
    {
        SQLiteDatabase db = getReadableDatabase();
        int i=db.delete(category_table_name,id+" = '"+category_id+"'",null);
        db.close();
        return i;
    }

    public data_handler get_category_list()
    {
        data_handler data=new data_handler();
        data.category_data_list.clear();
        data.expense_data_list.clear();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c=db.query(category_table_name,null,null,null,null,null,null,null);
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            data.category_data_list.add(new data_handler.category_data_handler(c.getInt(c.getColumnIndex(id)), c.getString(c.getColumnIndex(name))));
            c.moveToNext();
        }
        db.close();
        return data;
    }

    //expense database functions
}
