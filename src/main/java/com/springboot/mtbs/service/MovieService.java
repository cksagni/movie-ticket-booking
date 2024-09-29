package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.dao.MovieRepository;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.entity.Movie;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Set<Movie> getMoviesByCity(Long cityId){
        Optional<City> city = cityRepository.findById(cityId);
        if (city.isPresent()){
            return city.get().getMovies();
        } else {
            throw new RuntimeException("City Not Found");
        }
    }

}
