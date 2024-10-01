package com.springboot.mtbs.dto;

public class CountryDTO {
    private Integer id;
    private String name;

    public CountryDTO() {
    }

    public CountryDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
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
}
