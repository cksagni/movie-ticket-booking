package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.*;
import com.springboot.mtbs.dto.CreateShowRequest;
import com.springboot.mtbs.dto.InitShowSeatsResponse;
import com.springboot.mtbs.dto.ShowResponse;
import com.springboot.mtbs.dto.ShowSeatResponse;
import com.springboot.mtbs.entity.*;
import com.springboot.mtbs.entity.enums.ShowSeatStatus;
import com.springboot.mtbs.exception.BadRequestException;
import com.springboot.mtbs.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final CityRepository cityRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowService(ShowRepository showRepository,
                       CityRepository cityRepository,
                       SeatRepository seatRepository,
                       ShowSeatRepository showSeatRepository,
                       MovieRepository movieRepository,
                       ScreenRepository screenRepository) {
        this.showRepository = showRepository;
        this.cityRepository = cityRepository;
        this.seatRepository = seatRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    public List<ShowResponse> getShows(Long movieId, Long cityId, LocalDate date) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException("CITY_NOT_FOUND", "City not found for id=" + cityId));

        boolean movieInCity = city.getMovies().stream().anyMatch(movie -> movie.getId().equals(movieId));
        if (!movieInCity) {
            return List.of();
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        return showRepository.findShowsByMovieAndDate(movieId, dayStart, dayEnd).stream()
                .map(show -> new ShowResponse(
                        show.getId(),
                        show.getMovie().getId(),
                        show.getMovie().getTitle(),
                        show.getShowTime(),
                        show.getPrice(),
                        show.getScreen().getId(),
                        show.getScreen().getTheater().getName()
                ))
                .toList();
    }

    public List<ShowSeatResponse> getSeats(Long showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("SHOW_NOT_FOUND", "Show not found for id=" + showId));

        return showSeatRepository.findByShowId(showId).stream()
                .map(showSeat -> new ShowSeatResponse(
                        showSeat.getId(),
                        showSeat.getSeat().getId(),
                        showSeat.getSeat().getRow(),
                        showSeat.getSeat().getSeatNumber(),
                        showSeat.getStatus().name(),
                        showSeat.getPrice(),
                        showSeat.getLockExpiresAt()
                ))
                .toList();
    }

    public Show createShow(CreateShowRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new NotFoundException("MOVIE_NOT_FOUND", "Movie not found for id=" + request.movieId()));

        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> new NotFoundException("SCREEN_NOT_FOUND", "Screen not found for id=" + request.screenId()));

        if (screen.getTheater() == null) {
            throw new BadRequestException("INVALID_SCREEN", "Screen must be linked to a theatre");
        }

        if (request.price() <= 0) {
            throw new BadRequestException("INVALID_PRICE", "Show price must be greater than 0");
        }

        if (request.showTime() == null || !request.showTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("INVALID_SHOW_TIME", "Show time must be in the future");
        }

        Show show = new Show(movie, screen, request.showTime(), request.price());
        return showRepository.save(show);
    }

    @Transactional
    public InitShowSeatsResponse initializeShowSeats(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("SHOW_NOT_FOUND", "Show not found for id=" + showId));

        List<ShowSeat> existing = showSeatRepository.findByShowId(showId);
        if (!existing.isEmpty()) {
            return new InitShowSeatsResponse(showId, existing.size());
        }

        List<Seat> screenSeats = seatRepository.findByScreenId(show.getScreen().getId());
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : screenSeats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeat.setPrice(show.getPrice());
            showSeats.add(showSeat);
        }

        List<ShowSeat> saved = showSeatRepository.saveAll(showSeats);
        return new InitShowSeatsResponse(showId, saved.size());
    }
}
