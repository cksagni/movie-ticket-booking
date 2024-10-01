package com.springboot.mtbs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "city_movie", // Join table name
            joinColumns = @JoinColumn(name = "movie_id"), // Foreign key in the join table referencing Movie
            inverseJoinColumns = @JoinColumn(name = "city_id") // Foreign key referencing Movie
    )
    private Set<Movie> movies = new HashSet<>();

    public City() {}

    public City(String name, State state, Country country) {
        this.name = name;
        this.state = state;
        this.country = country;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void setMovies(Set<Movie> movies) {
        this.movies = movies;
    }

    // Add a helper method to associate a city with a movie
    public void addMovie(Movie movie) {
        this.movies.add(movie);
    }

    @Override
    public String toString() {
        return "City{id=" + id + ", name='" + name + '\'' + ", state='" + state.getName() + '\'' +
                ", country='" + country.getName() + '\'' + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
}
