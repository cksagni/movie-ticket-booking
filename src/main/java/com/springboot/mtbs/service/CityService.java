package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.entity.City;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository){
        this.cityRepository = cityRepository;
    }
    public List<City> getAllCities(){
        return cityRepository.findAll();
    }
    public City saveCity(City city){
        return cityRepository.save(city);
    }
}
