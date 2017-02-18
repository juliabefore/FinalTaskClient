package com.miskevich.core;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SessionFactory {

    private Socket socket;

    public SessionFactory(Socket socket){
        this.socket = socket;
    }

    public void save(Object object) {
        try{
            String query = QueryGenerator.generateSave(object);
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(query);
            System.out.println("Query was sent by client to server: " + query);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ResultSetParser.parseSaveResult(bufferedReader);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public <T> List<T> getAll(Class<T> tClass){
        OutputStream outputStream;
        BufferedReader bufferedReader;
        try {
            outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            String query = QueryGenerator.generateGetAll(tClass);
            bufferedWriter.write(query);
            System.out.println("Query was sent by client to server: " + query);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        List<T> objList = ResultSetParser.parseValuesToObject(bufferedReader, tClass);

        for (T object : objList) {
            System.out.println(object);
        }

        return objList;
    }

    public <T> T getById(Class<T> tClass, T object){
        T value;
        try {
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            String query = QueryGenerator.generateGetById(tClass, object);
            bufferedWriter.write(query);
            System.out.println("Query was sent by client to server: " + query);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            value = ResultSetParser.parseValuesToObjectById(bufferedReader, tClass);

            System.out.println(value);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return value;
    }
}
