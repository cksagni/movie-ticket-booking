package com.springboot.mtbs.dto;

public class StateDTO {
    private Integer id;
    private String name;
    private Integer countryId;
    private boolean isActive;

    public StateDTO() {
    }

    public StateDTO(Integer id, String name, Integer countryId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.countryId = countryId;
        this.isActive = isActive;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
