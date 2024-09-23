package com.springboot.mtbs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "screens")
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "theater_id", nullable = false)
    private Theatre theatre;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "screen")
    private List<Seat> seats;

    public Screen() {}

    public Screen(String name, Theatre theatre, Integer totalSeats) {
        this.name = name;
        this.theatre = theatre;
        this.totalSeats = totalSeats;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Theatre getTheater() { return theatre; }
    public void setTheater(Theatre theater) { this.theatre = theater; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    @Override
    public String toString() {
        return "Screen{id=" + id + ", name='" + name + '\'' + ", theater=" + theatre +
                ", totalSeats=" + totalSeats + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
}
