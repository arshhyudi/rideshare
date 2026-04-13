package com.rideshare.locationservice.service;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis key for all driver locations
    private static final String DRIVERS_GEO_KEY = "drivers:location";

    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {

        if (driverLocationRequest == null || driverLocationRequest.getDriverId() == null) {
            log.error("Invalid driver location driverLocationRequest");
            throw new IllegalArgumentException("Driver location driverLocationRequest is invalid");
        }

        try {
            log.info("Updating location for driverId: {}", driverLocationRequest.getDriverId());

            // Redis GEO uses (longitude, latitude)
            Point driverPoint;
            driverPoint = new Point(driverLocationRequest.getLongitude(), driverLocationRequest.getLatitude());

            redisTemplate.opsForGeo().add(
                    DRIVERS_GEO_KEY,
                    driverPoint,
                    driverLocationRequest.getDriverId()
            );

            log.info("Location updated successfully for driverId: {}", driverLocationRequest.getDriverId());

        } catch (Exception ex) {
            log.error("Failed to update location for driverId: {}", driverLocationRequest.getDriverId(), ex);
            throw new RuntimeException("Unable to update driver location", ex);
        }
    }

    /** find nearby driver  */
    public List<NearByDriverResponse> findNearbyDrivers(double latitude,
                                                        double longitude,
                                                        double radiusInKm) {

        log.info("Finding drivers near lat: {}, lon: {} within {} km",
                latitude, longitude, radiusInKm);

        // Create search area (IMPORTANT: longitude first)
        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        // Fetch results from Redis GEO
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();

        if (results != null) {
            results.getContent().forEach(result -> {

                RedisGeoCommands.GeoLocation<String> location = result.getContent();

                String driverId = location.getName();
                double lat = location.getPoint().getY(); // latitude
                double lon = location.getPoint().getX(); // longitude

                double distance = (result.getDistance() != null)
                        ? result.getDistance().getValue()
                        : 0.0;

                // Map to response DTO
                NearByDriverResponse response = new NearByDriverResponse(
                        driverId,
                        lat,
                        lon,
                        distance
                );

                nearbyDrivers.add(response);
            });
        }

        log.info("Found {} nearby drivers", nearbyDrivers.size());

        return nearbyDrivers;
    }


    //delete driver
    public void removeDriver(String driverId) {
        log.info("removing driver:{}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY,driverId);
    }
}
