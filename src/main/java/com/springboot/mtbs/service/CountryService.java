package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CountryRepository;
import com.springboot.mtbs.entity.Country;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {
    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> getAllCountries(){
        return countryRepository.findByIsActiveTrue();
    }

    public Country addCountry(Country country){
        return countryRepository.save(country);
    }

    public Country activateCountry(Integer id){
        Optional<Country> countryOptional = countryRepository.findById(id);
        if(countryOptional.isPresent()){
            Country country = countryOptional.get();
            country.setActive(true);
            return countryRepository.save(country);
        } else {
            throw new RuntimeException("Country not found");
        }
    }

    public Country getCountryDetails(Integer id){
        Optional<Country> country = countryRepository.findById(id);
        if(country.isPresent()){
            return country.get();
        } else {
            throw new RuntimeException("Country not found");
        }
    }
}
