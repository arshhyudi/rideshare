package com.rideshare.matchingservice.dto;
// response received from location service
//when qurey

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearByDriverResponse {
private String driverId;
private double latitude;
private double longitude;
private double distanceInKm;
}
