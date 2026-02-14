package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.CityRepository;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    private CityService cityService;

    @BeforeEach
    void setUp() {
        cityService = new CityService(cityRepository);
    }

    @Test
    void getAllCitiesReturnsRepositoryData() {
        List<City> expected = List.of(
                new City("Austin", "Texas", "USA"),
                new City("Seattle", "Washington", "USA")
        );
        when(cityRepository.findAll()).thenReturn(expected);

        List<City> result = cityService.getAllCities();

        assertEquals(2, result.size());
        assertEquals("Austin", result.get(0).getName());
        verify(cityRepository).findAll();
    }

    @Test
    void getAllCitiesReturnsEmptyListWhenNoCitiesExist() {
        when(cityRepository.findAll()).thenReturn(List.of());

        List<City> result = cityService.getAllCities();

        assertTrue(result.isEmpty());
    }

    @Test
    void saveCityDelegatesToRepository() {
        City city = new City("Boston", "Massachusetts", "USA");
        when(cityRepository.save(city)).thenReturn(city);

        City saved = cityService.saveCity(city);

        assertSame(city, saved);
        verify(cityRepository).save(city);
    }

    @Test
    void saveCityTrimsValuesBeforeSave() {
        City city = new City("  Boston  ", "  Massachusetts ", " USA ");
        when(cityRepository.save(city)).thenReturn(city);

        City saved = cityService.saveCity(city);

        assertEquals("Boston", saved.getName());
        assertEquals("Massachusetts", saved.getState());
        assertEquals("USA", saved.getCountry());
    }

    @Test
    void saveCityThrowsWhenRequiredFieldsMissing() {
        City city = new City(" ", "Texas", "USA");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> cityService.saveCity(city));

        assertEquals("INVALID_CITY", exception.getCode());
        verify(cityRepository, never()).save(any());
    }
}
