package com.springboot.mtbs.controller;

import com.springboot.mtbs.dao.CountryRepository;
import com.springboot.mtbs.dto.CountryDTO;
import com.springboot.mtbs.entity.Country;
import com.springboot.mtbs.service.CountryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {
    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public List<CountryDTO> getAllCountries(){
        List<Country> countries = countryService.getAllCountries();
        List<CountryDTO> countryDTOS = new ArrayList<>();
        for (Country country: countries){
            CountryDTO countryDTO = new CountryDTO();
            countryDTO.setId(country.getId());
            countryDTO.setName(country.getName());
            countryDTOS.add(countryDTO);
        }
        return countryDTOS;
    }

    

}
