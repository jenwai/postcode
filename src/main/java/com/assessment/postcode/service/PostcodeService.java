package com.assessment.postcode.service;

import com.assessment.postcode.dto.UkPostcodeDto;
import com.assessment.postcode.mapper.PostcodeMapper;
import com.assessment.postcode.model.DistanceApiRs;
import com.assessment.postcode.model.GetDistanceApiRs;
import com.assessment.postcode.repository.UkPostcodeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@Slf4j
public class PostcodeService {

  private final UkPostcodeRepo ukPostcodeRepo;
  private final PostcodeMapper postcodeMapper;

  private static final double EARTH_RADIUS = 6371; // radius in kilometers

  public PostcodeService(UkPostcodeRepo ukPostcodeRepo, PostcodeMapper postcodeMapper) {
    this.ukPostcodeRepo = ukPostcodeRepo;
    this.postcodeMapper = postcodeMapper;
  }

  public UkPostcodeDto getByPostcode(String postcode) {
    return this.ukPostcodeRepo.findByPostcode(postcode)
      .map(this.postcodeMapper::toDto)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity Not Found"));
  }

  public UkPostcodeDto updateCoordinatesByPostcode(String postcode, BigDecimal latitude,
    BigDecimal longitude) {

    if (latitude.compareTo(new BigDecimal("49.9")) < 0
      || latitude.compareTo(new BigDecimal("60.9")) > 0
      || longitude.compareTo(new BigDecimal("-8.2")) < 0
      || longitude.compareTo(new BigDecimal("1.8")) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Latitude or longitude is out of valid UK bounds. Expected lat between 49.9 and 60.9, lon between -8.2 and 1.8.");
    }

    var dto = this.getByPostcode(postcode);
    dto.setLatitude(latitude);
    dto.setLongitude(longitude);

    this.ukPostcodeRepo.save(this.postcodeMapper.toEntity(dto));
    return dto;
  }

  public GetDistanceApiRs constructPostcodesDistance(String postcode1, String postcode2) {

    var dto1 = this.getByPostcode(postcode1);
    var dto2 = this.getByPostcode(postcode2);

    var distance = this.calculateDistanceInKm(
      dto1.getLatitude().doubleValue(),
      dto1.getLongitude().doubleValue(),
      dto2.getLatitude().doubleValue(),
      dto2.getLongitude().doubleValue()
    );

    return GetDistanceApiRs.builder()
      .postcode1(dto1)
      .postcode2(dto2)
      .distance(
        DistanceApiRs.builder()
          .unit("km")
          .value(BigDecimal.valueOf(distance))
          .build()
      )
      .build();
  }

  private double calculateDistanceInKm(double latitude, double longitude, double latitude2, double
    longitude2) {
    // Using Haversine formula! See Wikipedia;
    double lon1Radians = Math.toRadians(longitude);
    double lon2Radians = Math.toRadians(longitude2);
    double lat1Radians = Math.toRadians(latitude);
    double lat2Radians = Math.toRadians(latitude2);
    double a = haversine(lat1Radians, lat2Radians)
      + Math.cos(lat1Radians) * Math.cos(lat2Radians) * haversine(lon1Radians, lon2Radians);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return (EARTH_RADIUS * c);
  }

  private double haversine(double deg1, double deg2) {
    return square(Math.sin((deg1 - deg2) / 2.0));
  }

  private double square(double x) {
    return x * x;
  }

}
