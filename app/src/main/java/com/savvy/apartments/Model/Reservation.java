package com.savvy.apartments.Model;

public class Reservation {
    String id, start_date, end_date, reserver_name, reserver_phone, day_price, apartment_id, status, days,note;

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApartment_id() {
        return apartment_id;
    }

    public void setApartment_id(String apartment_id) {
        this.apartment_id = apartment_id;
    }

    public String getDay_price() {
        return day_price;
    }

    public void setDay_price(String day_price) {
        this.day_price = day_price;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getReserver_name() {
        return reserver_name;
    }

    public void setReserver_name(String reserver_name) {
        this.reserver_name = reserver_name;
    }

    public String getReserver_phone() {
        return reserver_phone;
    }

    public void setReserver_phone(String reserver_phone) {
        this.reserver_phone = reserver_phone;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
