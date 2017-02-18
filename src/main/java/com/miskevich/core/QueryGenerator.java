package com.miskevich.core;

import com.miskevich.annotations.Column;
import com.miskevich.annotations.Id;
import com.miskevich.annotations.Table;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public abstract class QueryGenerator {

    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String VALUES = " VALUES ";
    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String EQUALS = " = ";

    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";

    private static final String SCANNED_PACKAGE = "com" + PKG_SEPARATOR + "miskevich" + PKG_SEPARATOR + "data";

    public static Map<Class, Set<String>> columnNamesCache = new HashMap<>();

    static {
        initializeColumnNamesCache(SCANNED_PACKAGE);
    }

    public static String generateSave(Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(INSERT_INTO)
            .append(getTableName(value))
            .append(inlineWithBrackets(getColumnNames(value)))
            .append(VALUES)
            .append(inlineWithBrackets(getColumnValues(value)));

        return stringBuilder.toString();
    }

    public static String generateGetAll(Class tClass){
        return generateDefaultSelectPrefix(tClass);
    }

    public static <T> String generateGetById(Class<T> tClass, T object){
        String msg = "No field with @Id annotation was found for the object ";

        String idName;
        int idValue;
        idName = getIdColumnName(object);
        if(idName != null){
            idValue = getIdColumnValue(object);
        }else{
            throw new ClientException(msg + object.getClass().getSimpleName());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateDefaultSelectPrefix(tClass))
                .append(WHERE)
                .append(idName)
                .append(EQUALS)
                .append(idValue);

        return stringBuilder.toString();
    }

    static String inlineWithBrackets(String separatedString){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
            .append(separatedString)
            .append(")");
        return stringBuilder.toString();
    }



    static String generateDefaultSelectPrefix(Class tClass){
        Object value;
        try{
            value = tClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String separatedColumns = getColumnNames(value);
        String tableName = getTableName(value);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SELECT)
            .append(separatedColumns)
            .append(FROM)
            .append(tableName);

        return stringBuilder.toString();
    }



    static Set<String> getColumnNamesInObject(Class <?> aClass) {
        Object value;
        Set<String> columnSet = new LinkedHashSet<>();
        try {
            value = aClass.newInstance();
            Field[] objectFields = getObjectFields(value);
            for (Field field : objectFields) {
                String columnNameInObject = getColumnName(field);
                if(columnNameInObject != null){
                    columnSet.add(columnNameInObject);
                }
            }

        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return columnSet;
    }

    static String getColumnName(Field field){
        String columnName = null;
        Column annotation = field.getAnnotation(Column.class);
        if(annotation != null){
            if("".equals(annotation.name())){
                columnName = field.getName();
            }else {
                columnName  = annotation.name();
            }
        }
        return columnName;
    }

    static String getTableName(Object value){
        Class clazz = value.getClass();
        Table annotation = (Table) clazz.getAnnotation(Table.class);

        if(annotation != null){
            return annotation.name();
        }else {
            throw new ClientException("No table mapping for the object " + clazz.getSimpleName());
        }
    }

    static String getColumnNames(Object value) {
        String errorMsg = "No fields with @Column annotation were found for the object ";

        Set<String> columns = columnNamesCache.get(value.getClass());
        if(columns == null){
            throw new ClientException(errorMsg + value.getClass().getSimpleName());
        }
        List<String> columnList = new ArrayList<>();
        columns.forEach(columnList::add);

        return generateSeparatedString(columnList);
    }

    static <T> String getIdColumnName(T value){
        Field[] annotatedFields = getObjectFields(value);
        String columnId = null;
        for (Field field : annotatedFields) {
            Id annotation = field.getAnnotation(Id.class);
            if(annotation != null){
                columnId = getColumnName(field);
            }
        }
        return columnId;
    }

    static <T> int getIdColumnValue(T value){
        Field[] annotatedFields = getObjectFields(value);
        int columnIdValue = 0;
        for (Field field : annotatedFields) {
            Id annotation = field.getAnnotation(Id.class);
            if(annotation != null) {
                field.setAccessible(true);
                try {
                    columnIdValue = field.getInt(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                field.setAccessible(false);
            }
        }
        return columnIdValue;
    }

    static String getColumnValues(Object value)  {
        Field[] annotatedFields = getObjectFields(value);
        List<String> columnValueList = new ArrayList<>();
        String columnValue;
        try{
            for (Field field : annotatedFields) {
                Column annotation = field.getAnnotation(Column.class);
                if(annotation != null){
                    field.setAccessible(true);
                    columnValue = String.valueOf(field.get(value));
                    columnValueList.add(columnValue);
                    field.setAccessible(false);
                }
            }
        }catch (IllegalAccessException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return generateSeparatedString(columnValueList);
    }

    static Field[] getObjectFields(Object value){
        Class clazz = value.getClass();
        return clazz.getDeclaredFields();
    }

    static String generateSeparatedString(List<String> list){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if(i <list.size() - 1){
                stringBuilder.append(list.get(i))
                    .append(", ");
            }else {
                stringBuilder.append(list.get(i));
            }
        }
        return stringBuilder.toString();
    }

    private static void initializeColumnNamesCache(String scannedPackage) {

        String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        File scannedDir;
        File[] files;
        if (scannedUrl != null) {
            scannedDir = new File(scannedUrl.getFile());
            files = scannedDir.listFiles();

            if(files != null){
                for (File file : files) {
                    String resource = scannedPackage + PKG_SEPARATOR + file.getName();
                    if (resource.endsWith(CLASS_FILE_SUFFIX)) {
                        int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
                        String className = resource.substring(0, endIndex);
                        try {
                            Class<?> mappedClassesIntoTable = findMappedClassIntoTables(Class.forName(className));
                            if(mappedClassesIntoTable != null){
                                Set<String> columnNamesInObject = getColumnNamesInObject(mappedClassesIntoTable);
                                columnNamesCache.put(mappedClassesIntoTable, columnNamesInObject);
                            }

                        } catch (ClassNotFoundException e) {
                        }
                    }
                }
            }

        }




    }

    private static Class<?> findMappedClassIntoTables(Class <?> aClass){
        Table annotation = aClass.getAnnotation(Table.class);

        if(annotation != null){
            return aClass;
        }
        return null;
    }

}
