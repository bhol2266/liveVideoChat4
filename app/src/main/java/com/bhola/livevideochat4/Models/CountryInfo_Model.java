package com.bhola.livevideochat4.Models;
public class CountryInfo_Model {
    private String nationality;
    private String flagUrl;
    private String country;
    private String countryCode;
    private boolean isSelected;

    // Constructors
    public CountryInfo_Model() {
    }


    public CountryInfo_Model(String nationality, String flagUrl, String country, String countryCode, boolean isSelected) {
        this.nationality = nationality;
        this.flagUrl = flagUrl;
        this.country = country;
        this.countryCode = countryCode;
        this.isSelected = isSelected;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
