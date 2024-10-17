package com.springboot.mtbs.controller;

import com.springboot.mtbs.dto.CountryDTO;
import com.springboot.mtbs.dto.StateDTO;
import com.springboot.mtbs.entity.Country;
import com.springboot.mtbs.entity.State;
import com.springboot.mtbs.service.CountryService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {
    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    private List<CountryDTO> createDTOs(List<Country> countries){
        List<CountryDTO> countryDTOS = new ArrayList<>();
        for (Country country: countries){
            CountryDTO countryDTO = new CountryDTO();
            countryDTO.setId(country.getId());
            countryDTO.setName(country.getName());
            countryDTO.setActive(country.isActive());
            countryDTOS.add(countryDTO);
        }
        return countryDTOS;
    }

    @GetMapping
    public List<CountryDTO> getAllActiveCountries(){
        List<Country> countries = countryService.getAllActiveCountries();
        return createDTOs(countries);
    }

    @GetMapping("/all")
    public List<CountryDTO> getAllCountries(){
        List<Country> countries = countryService.getAllCountries();
        return createDTOs(countries);
    }

    @PostMapping
    public CountryDTO addOrUpdateCountry(@RequestBody CountryDTO countryDTO){
        Country country = new Country(countryDTO.getId(), countryDTO.getName(), countryDTO.isActive());
        country = countryService.addOrUpdateCountry(country);
        countryDTO.setId(country.getId());
        return countryDTO;
    }

    @GetMapping("/{countryId}/states")
    public List<StateDTO> getStatesByCountry(@PathVariable Integer countryId){
        Country country = countryService.getCountryDetails(countryId);
        List<State> states = country.getStates();
        List<StateDTO> stateDTOS = new ArrayList<>();
        for(State state : states){
            StateDTO stateDTO = new StateDTO();
            stateDTO.setId(state.getId());
            stateDTO.setName(state.getName());
            stateDTO.setActive(state.isActive());
            stateDTO.setCountryId(state.getCountry().getId());
            stateDTOS.add(stateDTO);
        }
        return stateDTOS;
    }
}
