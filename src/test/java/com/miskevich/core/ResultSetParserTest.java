package com.miskevich.core;

import com.miskevich.data.Person;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


import static org.testng.Assert.*;

public class ResultSetParserTest {

    private Class testClass;
    private String responseForGetAll;
    private String responseForGetById;

    @BeforeClass
    public void initializeTestData(){
        this.testClass = Person.class;
        this.responseForGetAll = new StringBuilder()
                .append("p_id=5\n")
                .append("p_name=Adam\n")
                .append("age=55\n")
                .append("p_id=6\n")
                .append("p_name=Eva\n")
                .append("age=66\n")
                .toString();
        this.responseForGetById = new StringBuilder()
                .append("p_id=5\n")
                .append("p_name=Adam\n")
                .append("age=55\n")
                .toString();
    }

    @Test
    public void testParseSaveResult(){
        Reader reader = new StringReader("Object was saved in the file persons");
        String actual = ResultSetParser.parseSaveResult(reader);
        assertEquals(actual, "Object was saved in the file persons");
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "Unique constraint \\(PERSONS.P_ID\\) violated")
    public void testParseSaveResultUniqueConstraint(){
        Reader reader = new StringReader("Exception: Unique constraint (PERSONS.P_ID) violated");
        ResultSetParser.parseSaveResult(reader);
    }

    @Test
    public void testParseValuesToObject(){
        List<Person> expected = new ArrayList<Person>(){
            {
                add(new Person(5, "Adam", 55));
                add(new Person(6, "Eva", 66));
            }
        };

        Reader reader = new StringReader(responseForGetAll);
        List<Object> actual = ResultSetParser.parseValuesToObject(reader, testClass);
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "No data in the table persons")
    public void testParseValuesToObjectException(){
        String serverResponse = "Exception: No data in the table persons";
        Reader reader = new StringReader(serverResponse);
        ResultSetParser.parseValuesToObject(reader, testClass);
    }

    @Test
    public void testParseValuesToObjectById(){
        Person expected = new Person(5, "Adam", 55);

        Reader reader = new StringReader(responseForGetById);
        Object actual = ResultSetParser.parseValuesToObjectById(reader, testClass);
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = ClientException.class, expectedExceptionsMessageRegExp = "No data in the table persons with id value = 100")
    public void testParseValuesToObjectByIdException(){
        String serverResponse = "Exception: No data in the table persons with id value = 100";

        Reader reader = new StringReader(serverResponse);
        ResultSetParser.parseValuesToObjectById(reader, testClass);
    }

}