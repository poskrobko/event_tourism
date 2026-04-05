package com.example.eventtourism.repository;

import com.example.eventtourism.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {}
