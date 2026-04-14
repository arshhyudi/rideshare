package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer  {
    private  final MatchingService matchingService;
    /**
     * listen to ride request kafka topic
     * trigered every time ride servie publish a new request
     *
     * ride service first and -> kafka->this consumer -> matching service */

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"

    )
    public void consumeRideRequestEvent(RideRequestedEvent event){
        try{
            matchingService.matchDriver(event);
        } catch (Exception e) {
           log.info("error processing ride event :{} - {}", event.getRideId(), e.getMessage());

           //production send to dead letter queue for retry
        }
    }
}
