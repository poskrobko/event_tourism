package com.example.eventtourism.integration;

import org.springframework.stereotype.Service;

@Service
public class MapsService {
    public String buildMapUrl(String city, String address) {
        return "https://maps.google.com/?q=" + city.replace(" ", "+") + "," + address.replace(" ", "+");
    }
}
