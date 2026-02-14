package com.springboot.mtbs.controller;

import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.entity.Movie;
import com.springboot.mtbs.service.CityService;
import com.springboot.mtbs.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/cities")
public class CityController {
    private final CityService cityService;
    private final MovieService movieService;

    public CityController(CityService cityService, MovieService movieService){
        this.cityService = cityService;
        this.movieService = movieService;
    }

    @GetMapping
    public List<City> getAllCities(){
        return cityService.getAllCities();
    }

    @PostMapping
    public City createCity(@RequestBody City city){
        return cityService.saveCity(city);
    }

    @GetMapping("/{cityId}/movies")
    public Set<Movie> getMoviesByCity(@PathVariable Long cityId) {
        return movieService.getMoviesByCity(cityId);
    }

}
