package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.client.LocationServiceClient;
import com.rideshare.matchingservice.dto.NearByDriverResponse;
import com.rideshare.matchingservice.event.RideMatchedEvent;
import com.rideshare.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
  private final LocationServiceClient locationServiceClient;
  private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

  private static final String RIDE_MATCHED_TOPIC = "ride.match";

  private static final double DEFAULT_SEARCH_RADIUS_KM =  5.0;

  /**
   * main matching algorithams called when RideRequest is consuned from kafka @para event
   * */
  public void matchDriver(RideRequestedEvent event){
      List<NearByDriverResponse> nearByDriver = locationServiceClient.getNearByDriver(
              event.getPickupLatitude(),
              event.getPickupLongitude(),
              DEFAULT_SEARCH_RADIUS_KM
      );
      if(nearByDriver.isEmpty()){
          log.warn("No drivers found near ride:{}");
          return;
      }
      //score each driver and picj the best one
      Optional<NearByDriverResponse> bestDriver  = findBestDriver(nearByDriver);
      if(bestDriver.isEmpty()){
          log.info("Could not find suitable driver for ride");
          return;
      }
      NearByDriverResponse assigedDriver  = bestDriver.get();

      //publish Ride MatchedEvent to kafka
      RideMatchedEvent matchedEvent = new RideMatchedEvent(
              event.getRideId(),
              event.getRiderId(),
              assigedDriver.getDriverId(),
              assigedDriver.getLatitude(),
              assigedDriver.getLongitude(),
              assigedDriver.getDistanceInKm()

      );
      kafkaTemplate.send(RIDE_MATCHED_TOPIC,event.getRideId(), matchedEvent);
      log.info("RideMatchEvent pubnlished");
  }
    /***
     * driver scoring algorithms
     * distance  70%
     * rating  30%
     * score = (1/distance) + ratingWeight
     */

  private Optional<NearByDriverResponse> findBestDriver(
List<NearByDriverResponse> drivers
  ){
  double distanceWeight = 0.7;
  double ratingWeight = 0.3;
  return drivers.stream().max(Comparator.comparing(driver->{
      //distacne score closer = higher score
      // add 0.1 to avoid division by zero
      double distanceScore  = 1.0/(driver.getDistanceInKm() + 0.1);

      //simu;ated rating between 4.0 and 5.0
      // In production from driver services
      double simulatedRating  = 4.0 + Math.random();
      //final weight score
      return (distanceScore * distanceWeight)+ (simulatedRating * ratingWeight );
  }));

  }
}
