package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.ScreenRepository;
import com.springboot.mtbs.dao.TheatreRepository;
import com.springboot.mtbs.dto.CreateScreenRequest;
import com.springboot.mtbs.dto.CreateTheatreRequest;
import com.springboot.mtbs.entity.Screen;
import com.springboot.mtbs.entity.Theatre;
import com.springboot.mtbs.exception.BadRequestException;
import com.springboot.mtbs.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    public TheatreService(TheatreRepository theatreRepository, ScreenRepository screenRepository) {
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
    }

    public Theatre createTheatre(CreateTheatreRequest request) {
        String name = request.name().trim();
        String location = request.location().trim();
        if (name.isEmpty() || location.isEmpty()) {
            throw new BadRequestException("INVALID_THEATRE", "Theatre name and location are required");
        }

        Theatre theatre = new Theatre(name, location, null);
        return theatreRepository.save(theatre);
    }

    public Screen addScreen(Long theatreId, CreateScreenRequest request) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new NotFoundException("THEATRE_NOT_FOUND", "Theatre not found for id=" + theatreId));

        String screenName = request.name().trim();
        if (screenName.isEmpty()) {
            throw new BadRequestException("INVALID_SCREEN", "Screen name is required");
        }

        if (request.totalSeats() == null || request.totalSeats() <= 0) {
            throw new BadRequestException("INVALID_SCREEN", "totalSeats must be greater than 0");
        }

        Screen screen = new Screen(screenName, theatre, request.totalSeats());
        return screenRepository.save(screen);
    }
}
