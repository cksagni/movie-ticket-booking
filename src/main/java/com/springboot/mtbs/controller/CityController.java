package com.springboot.mtbs.controller;

import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.service.CityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService){
        this.cityService = cityService;
    }

    @GetMapping
    public List<City> getAllCities(){
        return cityService.getAllCities();
    }

    @PostMapping
    public City createCity(@RequestBody City city){
        return cityService.saveCity(city);
    }

}
