package com.rideshare.rideservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ride {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private String riderId;

    private String driverId;

    @Column(nullable = false)
    private double pickupLatitude;

    @Column(nullable = false)
    private double pickupLongitude;

    @Column(nullable = false)
    private String pickupAddress;
    @Column(nullable = false)
    private double dropLatitude;
    @Column(nullable = false)
    private double dropLongitude;
    @Column(nullable = false)
    private String dropAddress;

    //status for lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    //face details
    private double estimatedFare;
    private double actualFare;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    private LocalDateTime startedAt;
    private LocalDateTime completedAt;




}

