package com.rideshare.rideservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//event publish to kafka when a rise is reqeusted the topic ride.requested
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {
  private String rideId;
  private String riderId;


  //pickup
  private double pickupLatitude;
  private double pickupLongitude;
  private String pickupAddress;

// drop
  private double dropLatitude;
  private double dropLongitude;
  private String dropAddress;

}
