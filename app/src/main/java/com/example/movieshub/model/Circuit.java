package com.example.movieshub.model;

public class Circuit {
    private String cinema;
    private String name;
    private String region;
    private String address;
    private String tel;

    public Circuit(String cinema, String name, String region, String address, String tel) {
        this.cinema = cinema;
        this.name = name;
        this.region = region;
        this.address = address;
        this.tel = tel;
    }

    public String getCinema() {
        return cinema;
    }

    public void setCinema(String cinema) {
        this.cinema = cinema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
