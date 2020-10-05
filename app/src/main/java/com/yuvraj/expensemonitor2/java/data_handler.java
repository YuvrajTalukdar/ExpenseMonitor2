package com.yuvraj.expensemonitor2.java;

import java.util.ArrayList;

public class data_handler {

    public ArrayList<category_data_handler> category_data_list=new ArrayList<>();
    public ArrayList<expense_data_handler> expense_data_list=new ArrayList<>();

    public static class category_data_handler{
        public int id;
        public String category_name;
        public category_data_handler(int i,String name)
        {
            id=i;
            category_name=name;
        }
    }

    public static class expense_data_handler{
        public int id;
        public int day,month,year;
        public float cost;
        public ArrayList<item_data> item_data_list = new ArrayList();
    }

    public static class item_data
    {
        public int item_id;
        public String item_name,category;
        public float item_cost;
        public String data_id;
    }
}
