package com.serviceonwheels.auth_service.controller;

import com.serviceonwheels.auth_service.dto.MechanicResponse;
import com.serviceonwheels.auth_service.service.MechanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for mechanic data.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;

    @GetMapping
    public ResponseEntity<List<MechanicResponse>> listAll() {
        return ResponseEntity.ok(mechanicService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MechanicResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(mechanicService.findById(id));
    }
}
