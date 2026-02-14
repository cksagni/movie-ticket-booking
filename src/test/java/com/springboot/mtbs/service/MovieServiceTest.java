package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.dao.MovieRepository;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.entity.Movie;
import com.springboot.mtbs.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CityRepository cityRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(movieRepository, cityRepository);
    }

    @Test
    void getMoviesByCityReturnsMoviesWhenCityExists() {
        City city = new City("Austin", "Texas", "USA");
        Movie movie = new Movie("Movie A", "desc", "Action", 120, "PG", null, "English");
        city.setMovies(Set.of(movie));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        Set<Movie> result = movieService.getMoviesByCity(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getMoviesByCityThrowsWhenCityMissing() {
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> movieService.getMoviesByCity(99L));

        assertEquals("CITY_NOT_FOUND", exception.getCode());
    }
}
