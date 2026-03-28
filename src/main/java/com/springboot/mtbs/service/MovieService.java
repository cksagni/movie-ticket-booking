package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.dao.MovieRepository;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.entity.Movie;
import com.springboot.mtbs.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final CityRepository cityRepository;

    public MovieService(MovieRepository movieRepository, CityRepository cityRepository){
        this.movieRepository = movieRepository;
        this.cityRepository = cityRepository;
    }

    public Set<Movie> getMoviesByCity(Integer cityId){
        Optional<City> city = cityRepository.findById(cityId);
        if (city.isPresent()){
            return city.get().getMovies();
        } else {
            throw new NotFoundException("CITY_NOT_FOUND", "City not found for id=" + cityId);
        }
    }

}
