package com.sedadurmus.weatherapp.model;

public class Users {

    private String id;
    private String ad;
    private String mail;

    public Users() {
    }

    public Users(String id, String ad, String mail) {
        this.id = id;
        this.ad = ad;
        this.mail = mail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
