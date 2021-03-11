package com.savvy.apartments.Model;

import java.io.Serializable;

public class Building implements Serializable {
    String id, name, apartments_no, apartments, address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApartments() {
        return apartments;
    }

    public void setApartments(String apartments) {
        this.apartments = apartments;
    }
    /* public void setApartments(JSONArray apartments) {
        this.apartments = apartments;
    }

    public JSONArray getApartments() {
        return apartments;
    }*/

    public String getApartments_no() {
        return apartments_no;
    }

    public void setApartments_no(String apartments_no) {
        this.apartments_no = apartments_no;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
