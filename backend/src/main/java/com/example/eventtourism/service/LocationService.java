package com.example.eventtourism.service;

import com.example.eventtourism.entity.Location;
import com.example.eventtourism.integration.MapsService;
import com.example.eventtourism.repository.LocationRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final MapsService mapsService;

    public LocationService(LocationRepository locationRepository, MapsService mapsService) {
        this.locationRepository = locationRepository;
        this.mapsService = mapsService;
    }

    public Location create(String city, String address) {
        Location location = new Location();
        location.setCity(city);
        location.setAddress(address);
        location.setMapUrl(mapsService.buildMapUrl(city, address));
        return locationRepository.save(location);
    }
}
