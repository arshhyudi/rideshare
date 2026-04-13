package com.rideshare.locationservice.controller;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import com.rideshare.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    /**
     * API: Update driver location (called every few seconds from driver app)
     */
    @PostMapping("/driver/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestBody DriverLocationRequest request) {

        log.info("Received request to update location for driverId: {}", request.getDriverId());

        locationService.updateDriverLocation(request);

        log.info("Successfully updated location for driverId: {}", request.getDriverId());

        return ResponseEntity.ok("Driver location updated successfully");
    }

    /**
     * API: Get nearby drivers (called by matching service)
     */
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radius) {

        log.info("Finding nearby drivers for lat: {}, lon: {}, radius: {} km",
                latitude, longitude, radius);

        List<NearByDriverResponse> drivers =
                locationService.findNearbyDrivers(latitude, longitude, radius);

        log.info("Found {} drivers near given location", drivers.size());

        return ResponseEntity.ok(drivers);
    }

    /**
     * API: Remove driver when offline/unavailable
     */
    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverId) {

        log.info("Received request to remove driverId: {}", driverId);

        locationService.removeDriver(driverId);

        log.info("Driver removed successfully: {}", driverId);

        return ResponseEntity.ok("Driver removed successfully");
    }
}