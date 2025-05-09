package com.assessment.postcode.controller;

import com.assessment.postcode.model.UpdateCoordApiRq;
import com.assessment.postcode.service.PostcodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/postcodes/uk")
@Slf4j
public class UkPostcodeController {

  private final PostcodeService postcodeService;

  public UkPostcodeController(PostcodeService postcodeService) {
    this.postcodeService = postcodeService;
  }

  @GetMapping("/distance")
  public ResponseEntity<?> getPostcodesDistance(
    @RequestParam(value = "postcode_1", required = true) String postcode1,
    @RequestParam(value = "postcode_2", required = true) String postcode2) {

    log.debug("Calculating distance between p1: {}, p2: {}", postcode1, postcode2);

    try {
      var apiRs = this.postcodeService.constructPostcodesDistance(postcode1, postcode2);
      log.debug("Calculated distance between p1: {}, p2: {}, Distance: {}{}", postcode1, postcode2,
        apiRs.getDistance().getValue(), apiRs.getDistance().getUnit());
      return ResponseEntity.ok(apiRs);
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }
  }

  @GetMapping("/{postcode}")
  public ResponseEntity<?> queryUkPostcode(
    @PathVariable(value = "postcode", required = true) String postcode) {

    try {
      var dto = this.postcodeService.getByPostcode(postcode);
      return ResponseEntity.ok(dto);
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }
  }

  @PatchMapping("/{postcode}/coordinates")
  public ResponseEntity<?> updateTrxDesc(
    @PathVariable(value = "postcode", required = true) String postcode,
    @RequestBody UpdateCoordApiRq apiRq) {

    try {
      return ResponseEntity.ok(
        this.postcodeService.updateCoordinatesByPostcode(postcode, apiRq.getLatitude(),
          apiRq.getLongitude()));
    } catch (ResponseStatusException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }
  }
}
