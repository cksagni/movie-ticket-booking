package com.springboot.mtbs.controller;

import com.springboot.mtbs.dto.*;
import com.springboot.mtbs.entity.City;
import com.springboot.mtbs.entity.Screen;
import com.springboot.mtbs.entity.Theatre;
import com.springboot.mtbs.service.CityService;
import com.springboot.mtbs.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminSetupController {

    private final CityService cityService;
    private final TheatreService theatreService;

    public AdminSetupController(CityService cityService, TheatreService theatreService) {
        this.cityService = cityService;
        this.theatreService = theatreService;
    }

    @PostMapping("/cities")
    public CityResponse createCity(@Valid @RequestBody CreateCityRequest request) {
        City city = new City(request.name().trim(), request.state().trim(), request.country().trim());
        City saved = cityService.saveCity(city);
        return new CityResponse(saved.getId(), saved.getName(), saved.getState(), saved.getCountry());
    }

    @PostMapping("/theatres")
    public TheatreResponse createTheatre(@Valid @RequestBody CreateTheatreRequest request) {
        Theatre theatre = theatreService.createTheatre(request);
        return new TheatreResponse(theatre.getId(), theatre.getName(), theatre.getLocation());
    }

    @PostMapping("/theatres/{theatreId}/screens")
    public ScreenResponse addScreen(@PathVariable Long theatreId,
                                    @Valid @RequestBody CreateScreenRequest request) {
        Screen screen = theatreService.addScreen(theatreId, request);
        return new ScreenResponse(
                screen.getId(),
                screen.getTheater().getId(),
                screen.getName(),
                screen.getTotalSeats()
        );
    }
}
