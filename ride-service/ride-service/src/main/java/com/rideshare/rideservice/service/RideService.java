package com.rideshare.rideservice.service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    private static final String RIDE_REQUEST_TOPIC = "ride.requested";

    //create ride in db with requested status

    public RideResponse requestRide(RideRequest request){
        log.info("New Ride request from rider : {}", request.getRiderId());

        //save ride to data base
        Ride ride  = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));
        Ride savedRide = rideRepository.save(ride);

        //publish event to kafka
        // matching services  will consume this and find nearest driver

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );
        kafkaTemplate.send(RIDE_REQUEST_TOPIC, savedRide.getId(), event);
        log.info("Rdie RequestedEvent publish to kafka for ride  : {}", savedRide.getId());

        //updated  status to matching
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);
    }
// update the status
    public void updateRideWithDriver(String rideId, String driverId){
     Ride ride = rideRepository.findById(rideId).orElseThrow(()->new RuntimeException("Ride not found"));
     ride.setDriverId(driverId);
     ride.setStatus(RideStatus.ACCEPTED);
     rideRepository.save(ride);
    }
    private double calculateEstimateFare(RideRequest request){
        //simplyfiy hacersine distance calculation
        double lat1 = Math.toRadians(request.getPickupLatitude());
        double lat2 = Math.toRadians(request.getDropLatitude());


        double len1 = Math.toRadians(request.getPickupLongitude());
        double len2 = Math.toRadians(request.getDropLongitude());

        double dlat = lat2-lat1;
        double dlen = len2-len1;

        double a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1)* Math.cos(lat2)* Math.pow(Math.sin(dlen/2),2);

        double c  = 2*Math.asin(Math.sqrt(a));
        double distanceKm = 6371 *c;

        //base fare 50pr + 12 rs perkm
        double fare = 50 + (distanceKm *12);
        return Math.round(fare*100.0)/100.0;

    }

    private RideResponse mapToResponse(Ride ride){
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }

    public  RideResponse startRide(String rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(()->new RuntimeException("Ride not found"));
        if(ride.getStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride cannot be started, Current status :" + ride.getStatus());
        }
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);
        return mapToResponse(ride);

    }

    public RideResponse completedRide(String rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("ride  completed"));

        if (ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride cannot be completed:" + ride.getStatus());
        }
        ride.setStatus(RideStatus.COMPLETED);
        ride.setStartedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapToResponse(ride);
    }

    public RideResponse cancelRide(String rideId) {


        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("ride cancelled"));

        if (ride.getStatus() != RideStatus.CANCELLED) {
            throw new RuntimeException("Ride cannot be cancelled:" + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Get Ride"));
        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
