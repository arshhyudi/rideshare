package com.rideshare.rideservice.service;

import com.rideshare.rideservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer{

    private  final RideService rideService;
    @KafkaListener(
        topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumeRideMatchingEvent(RideRequestedEvent event){
        rideService.updateRideWithDriver(
                event.getRideId(),
                event.getRiderId()
        );
    }
}
