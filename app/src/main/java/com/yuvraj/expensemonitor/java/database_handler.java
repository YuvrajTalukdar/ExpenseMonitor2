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
    private static final String day="DAY",month="MONTH",year="YEAR",item_cost="COST",item_name="ITEM_NAME",item_category="CATEGORY",days_old="NO_OF_DAYS_OLD";
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
        String expense_table_query="CREATE TABLE IF NOT EXISTS "+expense_table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+item_name+" TEXT, "+item_category+" TEXT, "+item_cost+" REAL, "+day+" INTEGER, "+month+" INTEGER, "+year+" INTEGER, "+days_old+" INTEGER);";
        db.execSQL(expense_table_query);
        //db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+category_table_name);
        db.execSQL("DROP TABLE IF EXISTS "+expense_table_name);
        onCreate(db);
        //db.close();
    }

    //category database functions

    public void rename_category(int category_id,String new_category_name)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues new_value= new ContentValues();
        new_value.put(name,new_category_name);
        db.update(category_table_name,new_value,"id='"+category_id+"'",null);

        //rename category in expense table
        db.close();
    }

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

    private int get_month_size_in_days(int month,int year)
    {
        int feb_size;
        if(month%2==0)
        {
            if(year%4==0 && year%400!=0 && year%100==0)
            {   feb_size=28;}
            else if(year%4==0)
            {   feb_size=29;}
            else
            {   feb_size=28;}
        }
        else
        {   feb_size=31;}
        int days=0;
        for(int a=1;a<month;a++)
        {
            if(a==2)
            {   days+=feb_size;}
            else if(a%2==0)
            {   days+=30;}
            else
            {   days+=31;}
        }
        return days;
    }
    private int get_year_size_in_days(int year)
    {
        if(year%4==0 && year%400!=0 && year%100==0)
        {   return 365;}
        else if(year%4==0)
        {   return 366;}
        else
        {   return 365;}
    }

    public long add_expense_data(String item_name1,float cost,String category,int purchase_day,int purchase_month,int purchase_year)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(item_name,item_name1);
        contentValues.put(item_cost,cost);
        contentValues.put(item_category,category);
        contentValues.put(day,purchase_day);
        contentValues.put(month,purchase_month);
        contentValues.put(year,purchase_year);
        contentValues.put(days_old,purchase_year*get_year_size_in_days(purchase_year)+get_month_size_in_days(purchase_month,purchase_year)+purchase_day);
        long l=db.insert(expense_table_name,null,contentValues);
        db.close();
        contentValues.clear();
        return l;
    }

    public int get_last_entered_expense_data_id()
    {
        SQLiteDatabase db=getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX("+id+") FROM "+expense_table_name, null);
        int maxId;
        if(!c.moveToFirst())
        {   maxId=-1;}
        else
        {   maxId=c.getInt(0);}
        c.close();
        db.close();
        return maxId;
    }

    public int delete_expense_data(int data_id)
    {
        SQLiteDatabase db = getReadableDatabase();
        int i=db.delete(expense_table_name,id+" = '"+data_id+"'",null);
        db.close();
        return i;
    }

    public data_handler get_expense_data()
    {
        data_handler data = new data_handler();
        data.expense_data_list.clear();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+expense_table_name+" ORDER BY "+days_old+" DESC",null);
        c.moveToFirst();
        int days_old_last=0;
        int a=0;
        while(!c.isAfterLast())
        {
            data_handler.item_data item = new data_handler.item_data();
            item.category=c.getString(c.getColumnIndex(item_category));
            item.item_cost=c.getFloat(c.getColumnIndex(item_cost));
            item.item_name=c.getString(c.getColumnIndex(item_name));
            item.item_id=c.getInt(c.getColumnIndex(id));
            if(days_old_last==c.getInt(c.getColumnIndex(days_old)))
            {
                data.expense_data_list.get(a-1).item_data_list.add(item);
                data.expense_data_list.get(a-1).cost+=item.item_cost;
            }
            else
            {
                days_old_last=c.getInt(c.getColumnIndex(days_old));
                data_handler.expense_data_handler single_day_expense = new data_handler.expense_data_handler();
                single_day_expense.item_data_list.add(item);
                single_day_expense.id=a;
                single_day_expense.cost=c.getFloat(c.getColumnIndex(item_cost));
                single_day_expense.day=c.getInt(c.getColumnIndex(day));
                single_day_expense.month=c.getInt(c.getColumnIndex(month));
                single_day_expense.year=c.getInt(c.getColumnIndex(year));
                data.expense_data_list.add(single_day_expense);
                a++;
            }
            c.moveToNext();
        }

        c.close();
        db.close();
        return data;
    }
}
