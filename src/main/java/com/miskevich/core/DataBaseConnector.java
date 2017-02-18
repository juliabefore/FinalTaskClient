package com.miskevich.core;

import com.miskevich.data.Person;
import com.miskevich.data.Phone;

import java.io.IOException;
import java.net.Socket;

public class DataBaseConnector {

    public static void main(String[] args){

        Class<Person> tClass = Person.class;
        Class<Phone> zClass = Phone.class;
        try{
            Socket socket = new Socket("localhost", 3000);
            SessionFactory sessionFactory = new SessionFactory(socket);
            sessionFactory.save(new Person(8, "name_8", 88));
//            sessionFactory.getAll(tClass);
//            sessionFactory.getById(tClass, new Person(7));
        }catch (IOException e){
            e.printStackTrace();
        }

    }



}
