package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CountryRepository;
import com.springboot.mtbs.dao.StateRepository;
import com.springboot.mtbs.entity.Country;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;

    public CountryService(CountryRepository countryRepository, StateRepository stateRepository) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
    }

    public List<Country> getAllActiveCountries(){
        return countryRepository.findByIsActiveTrue();
    }

    public List<Country> getAllCountries(){
        return countryRepository.findAll();
    }

    @Transactional
    public Country addOrUpdateCountry(Country country){
        if (country.getId() != 0){
            Country existingCountry = countryRepository.findById(country.getId()).orElseThrow(
                    () -> new EntityNotFoundException("Country not found")
            );
            if (!country.isActive() && existingCountry.isActive())
                deactivateStates(country.getId());
        }
        return countryRepository.save(country);
    }

    private void deactivateStates(Integer countryId){
        stateRepository.deactivateStatesByCountryId(countryId);
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
