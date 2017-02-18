package com.miskevich.data;

import com.miskevich.annotations.Column;
import com.miskevich.annotations.Id;
import com.miskevich.annotations.Table;

@Table(name = "phones")
public class Phone {
    @Id
    @Column
    private int id;
    @Column
    private int code;
    @Column
    private int number;

    public Phone(int id, int code, int number) {
        this.id = id;
        this.code = code;
        this.number = number;
    }

    public Phone(int id) {
        this.id = id;
    }

    public Phone(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Phone phone = (Phone) o;

        if (code != phone.code) return false;
        if (id != phone.id) return false;
        if (number != phone.number) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + code;
        result = 31 * result + number;
        return result;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "id=" + id +
                ", code=" + code +
                ", number=" + number +
                '}';
    }
}

