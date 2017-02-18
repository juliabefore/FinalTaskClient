package com.miskevich.core;

import com.miskevich.data.Person;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class QueryGeneratorTest {

    private Object testObject;
    private Class testClass;

    @BeforeClass
    public void initializeTestData(){
        this.testObject = new Person(1, "name1", 11);
        this.testClass = Person.class;
    }

    @Test
    public void testInlineWithBrackets(){
        String actual = QueryGenerator.inlineWithBrackets("name1, name2, name3");
        assertEquals(actual, "(name1, name2, name3)");
    }

    @Test
    public void testGenerateSave(){
        String actual = QueryGenerator.generateSave(testObject);
        assertEquals(actual, "INSERT INTO persons(p_id, p_name, age) VALUES (1, name1, 11)");
    }

    @Test
    public void testGenerateSeparatedString(){
        List<String> list = new ArrayList<String>(){{add("column1"); add("column2"); add("column3");}};
        String actual = QueryGenerator.generateSeparatedString(list);
        assertEquals(actual, "column1, column2, column3");
    }

    @Test
    public void testGetColumnValues(){
        String actual = QueryGenerator.getColumnValues(testObject);
        assertEquals(actual, "1, name1, 11");
    }

    @Test
    public void testGetTableName(){
        String actual = QueryGenerator.getTableName(testObject);
        assertEquals(actual, "persons");
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "No table mapping for the object String")
    public void testGetTableNameNotMappedObject(){
        QueryGenerator.getTableName("");
    }

    @Test
    public void testGetColumnNames(){
        String actual = QueryGenerator.getColumnNames(testObject);
        assertEquals(actual, "p_id, p_name, age");
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "No fields with @Column annotation " +
            "were found for the object String")
    public void testGetColumnNamesNotMappedObject(){
        QueryGenerator.getColumnNames("");
    }

    @Test
    public void testGetColumnNamesInObject(){
        Set<String> actual = QueryGenerator.getColumnNamesInObject(testObject.getClass());
        assertEquals(actual, new LinkedHashSet<String>(){{add("p_id"); add("p_name"); add("age");}});
    }

    @Test
    public void testGetAnnotatedFields(){
        Field[] actual = QueryGenerator.getObjectFields(testObject);
        assertEquals(actual, testObject.getClass().getDeclaredFields());
    }

    @Test
    public void testGenerateDefaultSelectPrefix(){
        String actual = QueryGenerator.generateDefaultSelectPrefix(testClass);
        assertEquals(actual, "SELECT p_id, p_name, age FROM persons");
    }

    @Test
    public void testGenerateGetAll(){
        String actual = QueryGenerator.generateGetAll(testClass);
        assertEquals(actual, "SELECT p_id, p_name, age FROM persons");
    }

    @Test
    public void testGenerateGetById(){
        String actual = QueryGenerator.generateGetById(testClass, testObject);
        assertEquals(actual, "SELECT p_id, p_name, age FROM persons WHERE p_id = 1");
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "No field with @Id annotation was found for the object String")
    public void testGenerateGetByIdNotMappedObject(){
        QueryGenerator.generateGetById(String.class, "");
    }

    @Test
    public void testGetIdColumnName(){
        String actual = QueryGenerator.getIdColumnName(testObject);
        assertEquals(actual, "p_id");
    }

    @Test
    public void testGetColumnName(){
        Field[] annotatedFields = QueryGenerator.getObjectFields(testObject);
        for (int i = 0; i < annotatedFields.length; i++) {
            String actual = QueryGenerator.getColumnName(annotatedFields[i]);
            switch (i){
                case 0: assertEquals(actual, "p_id"); break;
                case 1: assertEquals(actual, "p_name"); break;
                case 2: assertEquals(actual, "age"); break;
            }
        }
    }

    @Test
    public void testGetIdColumnValue(){
        int actual = QueryGenerator.getIdColumnValue(testObject);
        assertEquals(actual, 1);
    }
}