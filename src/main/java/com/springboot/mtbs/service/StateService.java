package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.StateRepository;
import com.springboot.mtbs.entity.State;

import java.util.List;

public class StateService {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }
    public List<State> getAllStatesOfACountry(Integer countryId){
        return stateRepository.findAllActiveStatesOfACountry(countryId);
    }
}
