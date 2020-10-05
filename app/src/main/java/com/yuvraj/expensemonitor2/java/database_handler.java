package com.yuvraj.expensemonitor2.java;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class database_handler extends SQLiteOpenHelper {

    private static int DATABASE_VERSION =1;
    private static final String DATABASE_NAME="database.db";
    private static final String category_table_name="category_table",expense_table_name="expense_table";
    private static final String id="ID",name="NAME",unique_id="DATA_ID";
    private static final String day="DAY",month="MONTH",year="YEAR",item_cost="COST",item_name="ITEM_NAME",item_category="CATEGORY",days_old="NO_OF_DAYS_OLD";
    private static final String[] default_category_names=new String[]{"Other","Power Bill","House Rent","Food","Travel","Water Bill"};
    private Context context;

    public database_handler(@Nullable Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context=context;

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
        String expense_table_query="CREATE TABLE IF NOT EXISTS "+expense_table_name+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+item_name+" TEXT, "+item_category+" TEXT, "+item_cost+" REAL, "+day+" INTEGER, "+month+" INTEGER, "+year+" INTEGER, "+days_old+" INTEGER, "+unique_id+" TEXT);";
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
        if(year%4==0 && year%400!=0 && year%100==0)
        {   feb_size=28;}
        else if(year%4==0)
        {   feb_size=29;}
        else
        {   feb_size=28;}

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
    /*private int get_year_size_in_days(int year)
    {
        if(year%4==0 && year%400!=0 && year%100==0)
        {   return 365;}
        else if(year%4==0)
        {   return 366;}
        else
        {   return 365;}
    }*/

    private String generate_password(int length,boolean c_letter,boolean s_letters,boolean numbers,boolean spl_char)
    {
        StringBuffer string_buff = new StringBuffer();
        if(c_letter==true || s_letters==true || numbers==true || spl_char==true) {
            while (length != 0) {
                byte[] array = new byte[256];
                new Random().nextBytes(array);
                String random_string = new String(array, Charset.forName("UTF-8"));
                int a;
                for (a = 0; a < random_string.length(); a++) {

                    char ch = random_string.charAt(a);

                    if ((((ch >= 'a') && (ch <= 'z') && (s_letters == true))
                            || ((ch >= 'A') && (ch <= 'Z') && (c_letter == true))
                            || ((ch >= '0') && (ch <= '9') && (numbers == true))
                            || ((ch >= '!') && (ch <= '/') && (spl_char == true))
                    )
                            && (length > 0)) {

                        string_buff.append(ch);
                        length--;
                    }
                }
            }
        }
        return string_buff.toString();
    }
    private String create_unique_data_entry_id()
    {
        String unique_id1=generate_password(5,true,true,true,false);
        int y= Calendar.getInstance().get(Calendar.YEAR);
        int m=Calendar.getInstance().get(Calendar.MONTH)+1;
        int d=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        long t=Calendar.getInstance().getTime().getTime();
        return ""+d+"_"+m+"_"+y+"_"+t+"_"+unique_id1;
    }

    public long add_expense_data(boolean for_restore,String unique_id1,String item_name1,float cost,String category,int purchase_day,int purchase_month,int purchase_year)
    {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(item_name,item_name1);
        contentValues.put(item_cost,cost);
        contentValues.put(item_category,category);
        contentValues.put(day,purchase_day);
        contentValues.put(month,purchase_month);
        contentValues.put(year,purchase_year);
        contentValues.put(days_old,purchase_year*365+get_month_size_in_days(purchase_month,purchase_year)+purchase_day);
        if(for_restore)
        {   contentValues.put(unique_id,unique_id1);}
        else
        {   contentValues.put(unique_id,create_unique_data_entry_id());}
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
            item.data_id=c.getString(c.getColumnIndex(unique_id));
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

    //backup and restore data

    public void backup_data(OutputStream out)
    {
        data_handler dataHandler = get_expense_data();
        String heading="ID,Item_Name,Cost,Category,Day,Month,Year,"+unique_id+"\n";
        try {
            out.write(heading.getBytes());
            for (int a = 0; a < dataHandler.expense_data_list.size(); a++) {
                for (int b = 0; b < dataHandler.expense_data_list.get(a).item_data_list.size(); b++) {
                    String line = "" + dataHandler.expense_data_list.get(a).item_data_list.get(b).item_id + "," +
                            dataHandler.expense_data_list.get(a).item_data_list.get(b).item_name + "," +
                            dataHandler.expense_data_list.get(a).item_data_list.get(b).item_cost + "," +
                            dataHandler.expense_data_list.get(a).item_data_list.get(b).category + "," +
                            dataHandler.expense_data_list.get(a).day + "," +
                            dataHandler.expense_data_list.get(a).month + "," +
                            dataHandler.expense_data_list.get(a).year + "," +
                            dataHandler.expense_data_list.get(a).item_data_list.get(b).data_id+"\n";

                    out.write(line.getBytes());
                }
            }
            out.close();
            dataHandler.expense_data_list.clear();
            dataHandler.category_data_list.clear();
        }
        catch(Exception e)
        {   e.printStackTrace();}
    }

    private void safety_check_and_add_data(boolean for_restore,String unique_id1,String item_name1,float cost,String category,int purchase_day,int purchase_month,int purchase_year)
    {
        SQLiteDatabase db = getReadableDatabase();
        if(unique_id1.equals(""))
        {   unique_id1="no_id";}
        Cursor c = db.rawQuery("SELECT * FROM "+expense_table_name+" WHERE "+unique_id+" = '"+unique_id1+"'",null);
        if(!c.moveToFirst())
        {
            add_expense_data(for_restore,unique_id1,item_name1,cost,category,purchase_day,purchase_month,purchase_year);
            SQLiteDatabase db2 = getReadableDatabase();
            Cursor c2 = db2.rawQuery("SELECT * FROM "+category_table_name+" WHERE "+name+" = '"+category+"'",null);
            if(!c2.moveToFirst())
            {   add_new_category(category);}
            c2.close();
            db2.close();
        }
        c.close();
        db.close();
    }

    public Task<Boolean> restore_data(InputStream in)
    {
        Executor mExecutor = Executors.newSingleThreadExecutor();
        return Tasks.call(mExecutor, () -> {
            try{
                String line="";
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                if(in != null)
                {
                    int spending_id=-1,purchase_day=-1,purchase_month=-1,purchase_year=-1;
                    float item_cost=0.0F;
                    String item_name="",item_category="",unique_id1="",temp="";
                    int col_id=0,row_id=0;
                    while((line = reader.readLine()) != null)
                    {
                        //System.out.println("line= "+line);
                        if(row_id==0)//for skipping the first line
                        {   row_id++;continue;}
                        temp="";
                        col_id=0;
                        for(int a=0;a<line.length();a++)
                        {
                            if(line.charAt(a)==','||a==line.length()-1)
                            {
                                if(a==line.length()-1)
                                {   temp+=line.charAt(a);}
                                if(col_id==0)
                                {   spending_id=Integer.parseInt(temp);}
                                else if(col_id==1)
                                {   item_name=temp;}
                                else if(col_id==2)
                                {   item_cost=Float.parseFloat(temp);}
                                else if(col_id==3)
                                {   item_category=temp;}
                                else if(col_id==4)
                                {   purchase_day=Integer.parseInt(temp);}
                                else if(col_id==5)
                                {   purchase_month=Integer.parseInt(temp);}
                                else if(col_id==6)
                                {   purchase_year=Integer.parseInt(temp);}
                                else if(col_id==7)
                                {   unique_id1=temp;}
                                temp="";
                                col_id++;
                            }
                            else
                            {   temp+=line.charAt(a);}
                        }
                        if(col_id==8)
                        {   safety_check_and_add_data(true,unique_id1,item_name,item_cost,item_category,purchase_day,purchase_month,purchase_year);}
                        else
                        {   throw new Exception("Invalid backup file.");}
                        row_id++;
                    }
                    if(row_id==1)
                    {   throw new Exception("No data present.");}
                }
                reader.close();
                in.close();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
