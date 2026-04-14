package com.rideshare.rideservice.model;

/** request matching -> accepted -> driver arriving -> ride start -> completed */
public enum RideStatus {
     REQUESTED,
     MATCHING,
    ACCEPTED,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED

}
