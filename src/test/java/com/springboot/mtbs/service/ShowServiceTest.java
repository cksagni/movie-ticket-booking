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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ScreenRepository screenRepository;

    private ShowService showService;

    @BeforeEach
    void setUp() {
        showService = new ShowService(
                showRepository,
                cityRepository,
                seatRepository,
                showSeatRepository,
                movieRepository,
                screenRepository
        );
    }

    @Test
    void getShowsReturnsEmptyWhenMovieNotInCity() {
        City city = new City("Austin", "Texas", "USA");
        Movie cityMovie = movieWithId(11L, "Movie in city");
        city.setMovies(Set.of(cityMovie));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        List<ShowResponse> result = showService.getShows(22L, 1L, LocalDate.now());

        assertTrue(result.isEmpty());
        verify(showRepository, never()).findShowsByMovieAndDate(anyLong(), any(), any());
    }

    @Test
    void getShowsThrowsWhenCityMissing() {
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> showService.getShows(1L, 99L, LocalDate.now()));

        assertEquals("CITY_NOT_FOUND", exception.getCode());
    }

    @Test
    void getShowsMapsShowData() {
        City city = new City("Austin", "Texas", "USA");
        Movie movie = movieWithId(1L, "Movie A");
        city.setMovies(Set.of(movie));

        Theatre theatre = new Theatre("Theatre 1", "Downtown", 4);
        ReflectionTestUtils.setField(theatre, "id", 1L);

        Screen screen = new Screen("Screen 1", theatre, 100);
        ReflectionTestUtils.setField(screen, "id", 5L);

        Show show = new Show(movie, screen, LocalDateTime.now().plusDays(1), 250.0);
        ReflectionTestUtils.setField(show, "id", 10L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(showRepository.findShowsByMovieAndDate(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(show));

        List<ShowResponse> result = showService.getShows(1L, 1L, LocalDate.now().plusDays(1));

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("Movie A", result.get(0).movieTitle());
    }

    @Test
    void getSeatsThrowsWhenShowMissing() {
        when(showRepository.findById(77L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> showService.getSeats(77L));

        assertEquals("SHOW_NOT_FOUND", exception.getCode());
    }

    @Test
    void getSeatsReturnsMappedSeats() {
        Show show = new Show();
        ReflectionTestUtils.setField(show, "id", 1L);

        Seat seat = seatWithId(2L, 7L);

        ShowSeat showSeat = new ShowSeat();
        ReflectionTestUtils.setField(showSeat, "id", 9L);
        showSeat.setShow(show);
        showSeat.setSeat(seat);
        showSeat.setStatus(ShowSeatStatus.AVAILABLE);
        showSeat.setPrice(300.0);

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowId(1L)).thenReturn(List.of(showSeat));

        List<ShowSeatResponse> result = showService.getSeats(1L);

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).showSeatId());
        assertEquals("A", result.get(0).seatRow());
    }

    @Test
    void createShowThrowsForInvalidPrice() {
        CreateShowRequest request = new CreateShowRequest(1L, 1L, LocalDateTime.now().plusDays(1), 0.0);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieWithId(1L, "Movie")));
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screenWithId(1L)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> showService.createShow(request));

        assertEquals("INVALID_PRICE", exception.getCode());
    }

    @Test
    void createShowThrowsWhenMovieMissing() {
        CreateShowRequest request = new CreateShowRequest(55L, 1L, LocalDateTime.now().plusDays(1), 100.0);
        when(movieRepository.findById(55L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> showService.createShow(request));

        assertEquals("MOVIE_NOT_FOUND", exception.getCode());
    }

    @Test
    void createShowThrowsWhenScreenMissing() {
        CreateShowRequest request = new CreateShowRequest(1L, 77L, LocalDateTime.now().plusDays(1), 100.0);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieWithId(1L, "Movie")));
        when(screenRepository.findById(77L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> showService.createShow(request));

        assertEquals("SCREEN_NOT_FOUND", exception.getCode());
    }

    @Test
    void createShowThrowsWhenScreenIsNotLinkedToTheatre() {
        CreateShowRequest request = new CreateShowRequest(1L, 2L, LocalDateTime.now().plusDays(1), 100.0);
        Movie movie = movieWithId(1L, "Movie");
        Screen screen = screenWithId(2L);
        screen.setTheater(null);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screenRepository.findById(2L)).thenReturn(Optional.of(screen));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> showService.createShow(request));

        assertEquals("INVALID_SCREEN", exception.getCode());
    }

    @Test
    void createShowThrowsWhenShowTimeIsPast() {
        CreateShowRequest request = new CreateShowRequest(1L, 2L, LocalDateTime.now().minusMinutes(10), 100.0);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieWithId(1L, "Movie")));
        when(screenRepository.findById(2L)).thenReturn(Optional.of(screenWithId(2L)));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> showService.createShow(request));

        assertEquals("INVALID_SHOW_TIME", exception.getCode());
    }

    @Test
    void createShowSucceedsForValidRequest() {
        CreateShowRequest request = new CreateShowRequest(1L, 2L, LocalDateTime.now().plusHours(2), 150.0);
        Movie movie = movieWithId(1L, "Movie");
        Screen screen = screenWithId(2L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screenRepository.findById(2L)).thenReturn(Optional.of(screen));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Show created = showService.createShow(request);

        assertEquals(movie.getId(), created.getMovie().getId());
        assertEquals(screen.getId(), created.getScreen().getId());
        assertEquals(150.0, created.getPrice());
    }

    @Test
    void initializeShowSeatsReturnsExistingCountWhenAlreadyInitialized() {
        Show show = showWithId(5L, 3L, 200.0);
        ShowSeat existing = new ShowSeat();
        ReflectionTestUtils.setField(existing, "id", 88L);

        when(showRepository.findById(5L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowId(5L)).thenReturn(List.of(existing));

        InitShowSeatsResponse response = showService.initializeShowSeats(5L);

        assertEquals(5L, response.showId());
        assertEquals(1, response.seatsInitialized());
        verify(showSeatRepository, never()).saveAll(anyList());
    }

    @Test
    void initializeShowSeatsCreatesSeatsFromScreenLayout() {
        Show show = showWithId(5L, 3L, 200.0);
        Seat seat1 = seatWithId(1L, 3L);
        Seat seat2 = seatWithId(2L, 3L);

        when(showRepository.findById(5L)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowId(5L)).thenReturn(List.of());
        when(seatRepository.findByScreenId(3L)).thenReturn(List.of(seat1, seat2));
        when(showSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        InitShowSeatsResponse response = showService.initializeShowSeats(5L);

        assertEquals(2, response.seatsInitialized());

        ArgumentCaptor<List<ShowSeat>> captor = ArgumentCaptor.forClass(List.class);
        verify(showSeatRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(ShowSeatStatus.AVAILABLE, captor.getValue().get(0).getStatus());
    }

    private Movie movieWithId(Long id, String title) {
        Movie movie = new Movie(title, "desc", "Action", 120, "PG", null, "English");
        ReflectionTestUtils.setField(movie, "id", id);
        return movie;
    }

    private Screen screenWithId(Long id) {
        Theatre theatre = new Theatre("Theatre", "Location", 3);
        ReflectionTestUtils.setField(theatre, "id", 99L);
        Screen screen = new Screen("Screen", theatre, 100);
        ReflectionTestUtils.setField(screen, "id", id);
        return screen;
    }

    private Show showWithId(Long showId, Long screenId, Double price) {
        Movie movie = movieWithId(7L, "Movie");
        Screen screen = screenWithId(screenId);
        Show show = new Show(movie, screen, LocalDateTime.now().plusDays(1), price);
        ReflectionTestUtils.setField(show, "id", showId);
        return show;
    }

    private Seat seatWithId(Long seatId, Long screenId) {
        Seat seat = new Seat();
        ReflectionTestUtils.setField(seat, "id", seatId);
        seat.setRow("A");
        seat.setSeatNumber(1);
        seat.setScreen(screenWithId(screenId));
        return seat;
    }
}
