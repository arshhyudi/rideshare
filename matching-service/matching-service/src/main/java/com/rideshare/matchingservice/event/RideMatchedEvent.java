package com.rideshare.matchingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * event publish to kafka topic ride matched
 * consume by ride serivce to update ride with assigned driver
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideMatchedEvent {
    private String rideId;
    private String riderId;
    private String driverId;
    private double driverLatitude;
    private double driverLongitude;
    private double distanceToPicupKm;
}
