package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.ScreenRepository;
import com.springboot.mtbs.dao.TheatreRepository;
import com.springboot.mtbs.dto.CreateScreenRequest;
import com.springboot.mtbs.dto.CreateTheatreRequest;
import com.springboot.mtbs.entity.Screen;
import com.springboot.mtbs.entity.Theatre;
import com.springboot.mtbs.exception.BadRequestException;
import com.springboot.mtbs.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TheatreServiceTest {

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private ScreenRepository screenRepository;

    private TheatreService theatreService;

    @BeforeEach
    void setUp() {
        theatreService = new TheatreService(theatreRepository, screenRepository);
    }

    @Test
    void createTheatreSucceedsWithTrimmedInput() {
        when(theatreRepository.save(any(Theatre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Theatre theatre = theatreService.createTheatre(new CreateTheatreRequest("  PVR  ", "  Downtown  "));

        assertEquals("PVR", theatre.getName());
        assertEquals("Downtown", theatre.getLocation());
    }

    @Test
    void createTheatreThrowsForBlankValues() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> theatreService.createTheatre(new CreateTheatreRequest(" ", "Downtown")));

        assertEquals("INVALID_THEATRE", exception.getCode());
    }

    @Test
    void addScreenThrowsWhenTheatreMissing() {
        when(theatreRepository.findById(10L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> theatreService.addScreen(10L, new CreateScreenRequest("Screen 1", 100)));

        assertEquals("THEATRE_NOT_FOUND", exception.getCode());
    }

    @Test
    void addScreenThrowsForInvalidSeats() {
        Theatre theatre = new Theatre("PVR", "Downtown", null);
        ReflectionTestUtils.setField(theatre, "id", 1L);
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> theatreService.addScreen(1L, new CreateScreenRequest("Screen 1", 0)));

        assertEquals("INVALID_SCREEN", exception.getCode());
    }

    @Test
    void addScreenSucceedsForValidRequest() {
        Theatre theatre = new Theatre("PVR", "Downtown", null);
        ReflectionTestUtils.setField(theatre, "id", 1L);
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(screenRepository.save(any(Screen.class))).thenAnswer(invocation -> {
            Screen screen = invocation.getArgument(0);
            ReflectionTestUtils.setField(screen, "id", 88L);
            return screen;
        });

        Screen screen = theatreService.addScreen(1L, new CreateScreenRequest("  Screen X ", 120));

        assertEquals(88L, screen.getId());
        assertEquals("Screen X", screen.getName());
        assertEquals(120, screen.getTotalSeats());
        assertEquals(1L, screen.getTheater().getId());
    }
}
