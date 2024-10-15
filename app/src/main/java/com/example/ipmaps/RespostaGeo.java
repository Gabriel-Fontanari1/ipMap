package com.example.ipmaps;

public class RespostaGeo {
    //atributos pra armazenar as informações que vão ser buscadas pela api
    private double lat;
    private double lon;
    private String country;
    private String regionName;
    private String city;
    private String isp;

    //getters e setters
    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getIsp() {
        return isp;
    }
}
