package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.exception.BadRequestException;
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
        if (city == null
                || city.getName() == null || city.getName().trim().isEmpty()
                || city.getState() == null || city.getState().trim().isEmpty()
                || city.getCountry() == null || city.getCountry().trim().isEmpty()) {
            throw new BadRequestException("INVALID_CITY", "City name/state/country are required");
        }

        city.setName(city.getName().trim());
        city.setState(city.getState().trim());
        city.setCountry(city.getCountry().trim());
        return cityRepository.save(city);
    }
}
