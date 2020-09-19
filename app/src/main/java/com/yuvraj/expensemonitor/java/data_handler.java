package com.yuvraj.expensemonitor.java;

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

    public class expense_data_handler{

    }
}
