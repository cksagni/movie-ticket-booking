package com.springboot.mtbs.controller;

import com.springboot.mtbs.dto.ShowResponse;
import com.springboot.mtbs.dto.ShowSeatResponse;
import com.springboot.mtbs.service.ShowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/movies/{movieId}/shows")
    public List<ShowResponse> getShowsByMovieAndDate(@PathVariable Long movieId,
                                                      @RequestParam Long cityId,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showService.getShows(movieId, cityId, date);
    }

    @GetMapping("/shows/{showId}/seats")
    public List<ShowSeatResponse> getShowSeats(@PathVariable Long showId) {
        return showService.getSeats(showId);
    }
}
