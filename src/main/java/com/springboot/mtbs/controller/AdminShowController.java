package com.springboot.mtbs.controller;

import com.springboot.mtbs.dto.CreateShowRequest;
import com.springboot.mtbs.dto.InitShowSeatsResponse;
import com.springboot.mtbs.entity.Show;
import com.springboot.mtbs.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shows")
public class AdminShowController {

    private final ShowService showService;

    public AdminShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public Show createShow(@Valid @RequestBody CreateShowRequest request) {
        return showService.createShow(request);
    }

    @PostMapping("/{showId}/show-seats/init")
    public InitShowSeatsResponse initShowSeats(@PathVariable Long showId) {
        return showService.initializeShowSeats(showId);
    }
}
