package com.miskevich.core;

import com.miskevich.annotations.Column;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class ResultSetParser {

    static String parseSaveResult(Reader reader){
        String serverOutput = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            serverOutput = bufferedReader.readLine();
            if(serverOutput.contains("Exception:")){
                throw new ClientException(serverOutput.substring(11));
            }else {
                System.out.println(serverOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverOutput;
    }

    static <T> List<T> parseValuesToObject(Reader reader, Class tClass){
        String columnValue;
        int fieldCounter = 0;
        List<T> generatedObjectList = new ArrayList<>();
        Field[] fields = tClass.getDeclaredFields();
        BufferedReader bufferedReader = new BufferedReader(reader);

        try {
            T obj = (T) tClass.newInstance();

            columnValue = bufferedReader.readLine();

            if(columnValue.contains("Exception:")){
                throw new ClientException(columnValue.substring(11));
            }else {
                do{
                    fieldCounter = setValueIntoObjectField(columnValue, fields, obj, fieldCounter);
                    if(fieldCounter==fields.length){
                        fieldCounter = 0;
                        generatedObjectList.add(obj);
                        obj = (T) tClass.newInstance();
                    }
                }while ((columnValue = bufferedReader.readLine()) != null);
            }
        } catch (IllegalAccessException | IOException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return generatedObjectList;
    }

    private static int setValueIntoObjectField(String columnValue, Field[] fields, Object obj, int fieldCounter){
        String colNameFromServer = columnValue.substring(0, columnValue.indexOf("="));
        String colValueFromServer = columnValue.substring(columnValue.indexOf("=") + 1);
        String columnNameFromObject;
        boolean isValueSet;
        for (Field field : fields) {
            Class<?> type = field.getType();
            Column annotation = field.getAnnotation(Column.class);
            if(annotation != null){
                if("".equals(annotation.name())){
                    columnNameFromObject = field.getName();
                    isValueSet = setValueIntoField(colNameFromServer, columnNameFromObject, field, type, obj, colValueFromServer);
                    if(isValueSet){
                        fieldCounter++;
                    }

                }else {
                    String columnNameFromAnnotation  = annotation.name();
                    isValueSet = setValueIntoField(colNameFromServer, columnNameFromAnnotation, field, type, obj, colValueFromServer);
                    if(isValueSet){
                        fieldCounter++;
                    }
                }
            }
        }

        return fieldCounter;
    }

    private static boolean setValueIntoField(String colNameFromServer, String columnNameFromObject, Field field,
                                      Class<?> type, Object obj, String colValueFromServer) {
        try{
            if(colNameFromServer.equals(columnNameFromObject)){
                field.setAccessible(true);
                if(int.class.equals(type)){
                    field.setInt(obj, Integer.parseInt(colValueFromServer));
                }else if(String.class.equals(type)){
                    field.set(obj, colValueFromServer);
                }
                field.setAccessible(false);
                return true;
            }else {return false;}
        }catch (IllegalAccessException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    static <T> T parseValuesToObjectById(Reader reader, Class tClass){
        BufferedReader bufferedReader = new BufferedReader(reader);
        T obj;
        int fieldCounter = 0;
        try {
            String columnValue;

            Field[] fields = tClass.getDeclaredFields();
            obj = (T) tClass.newInstance();
            columnValue = bufferedReader.readLine();

            if(columnValue.contains("Exception:")){
                throw new ClientException(columnValue.substring(11));
            }else {
                do{
                    setValueIntoObjectField(columnValue, fields, obj, fieldCounter);
                }while ((columnValue = bufferedReader.readLine()) != null);
            }
        } catch (IllegalAccessException | IOException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return obj;
    }
}

