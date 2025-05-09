package com.assessment.postcode.service;

import com.assessment.postcode.dto.UkPostcodeDto;
import com.assessment.postcode.entity.UkPostcode;
import com.assessment.postcode.mapper.PostcodeMapper;
import com.assessment.postcode.model.GetDistanceApiRs;
import com.assessment.postcode.repository.UkPostcodeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostcodeServiceTest {

  @Mock
  private UkPostcodeRepo ukPostcodeRepo;
  @Mock
  private PostcodeMapper postcodeMapper;
  @InjectMocks
  private PostcodeService postcodeService;
  private UkPostcodeDto postcodeDto;
  private UkPostcode postcodeEntity;
  private static final String POSTCODE_1 = "SW1A 1AA";

  @BeforeEach
  void setUp() {
    postcodeDto =
      new UkPostcodeDto(1, POSTCODE_1, BigDecimal.valueOf(51.5074), BigDecimal.valueOf(-0.1278));
    postcodeEntity =
      new UkPostcode(1, POSTCODE_1, BigDecimal.valueOf(51.5074), BigDecimal.valueOf(-0.1278));
  }

  @Test
  void testGetByPostcode_Success() {
    when(ukPostcodeRepo.findByPostcode(POSTCODE_1))
      .thenReturn(Optional.of(postcodeEntity));
    when(postcodeMapper.toDto(postcodeEntity)).thenReturn(postcodeDto);
    UkPostcodeDto result = postcodeService.getByPostcode(POSTCODE_1);

    assertNotNull(result);
    assertEquals(POSTCODE_1, result.getPostcode());
    assertEquals(BigDecimal.valueOf(51.5074), result.getLatitude());
    assertEquals(BigDecimal.valueOf(-0.1278), result.getLongitude());
  }

  @Test
  void testGetByPostcode_NotFound() {
    when(ukPostcodeRepo.findByPostcode("INVALID")).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      postcodeService.getByPostcode("INVALID");
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Entity Not Found", exception.getReason());
  }

  @Test
  void testUpdateCoordinatesByPostcode_Success() {
    when(ukPostcodeRepo.findByPostcode(POSTCODE_1))
      .thenReturn(Optional.of(postcodeEntity));
    when(postcodeMapper.toDto(postcodeEntity)).thenReturn(postcodeDto);
    UkPostcodeDto updatedDto =
      postcodeService.updateCoordinatesByPostcode(POSTCODE_1, new BigDecimal("51.5085"),
        new BigDecimal("-0.1279"));

    assertNotNull(updatedDto);
    assertEquals(new BigDecimal("51.5085"), updatedDto.getLatitude());
    assertEquals(new BigDecimal("-0.1279"), updatedDto.getLongitude());
  }

  @Test
  void testUpdateCoordinatesByPostcode_InvalidLatitude() {
    assertInvalidCoordinates(new BigDecimal("61.0"), new BigDecimal("-0.1279"));
  }

  @Test
  void testUpdateCoordinatesByPostcode_InvalidLongitude() {
    assertInvalidCoordinates(new BigDecimal("51.0"), new BigDecimal("2.0"));
  }

  private void assertInvalidCoordinates(BigDecimal latitude, BigDecimal longitude) {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
      () -> postcodeService.updateCoordinatesByPostcode(PostcodeServiceTest.POSTCODE_1, latitude,
        longitude));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(
      "Latitude or longitude is out of valid UK bounds. Expected lat between 49.9 and 60.9, lon between -8.2 and 1.8.",
      exception.getReason());
  }

  @Test
  void testConstructPostcodesDistance() {
    when(ukPostcodeRepo.findByPostcode(POSTCODE_1))
      .thenReturn(Optional.of(postcodeEntity));
    when(postcodeMapper.toDto(postcodeEntity)).thenReturn(postcodeDto);
    UkPostcodeDto otherDto =
      new UkPostcodeDto(2, "SW1A 2AA", BigDecimal.valueOf(40.5074), BigDecimal.valueOf(-1.1278));
    UkPostcode otherEntity =
      new UkPostcode(2, "SW1A 2AA", BigDecimal.valueOf(40.5074), BigDecimal.valueOf(-1.1278));
    when(ukPostcodeRepo.findByPostcode("SW1A 2AA")).thenReturn(Optional.of(otherEntity));
    when(postcodeMapper.toDto(otherEntity)).thenReturn(otherDto);
    GetDistanceApiRs result = postcodeService.constructPostcodesDistance(POSTCODE_1, "SW1A 2AA");

    assertNotNull(result);
    assertEquals(POSTCODE_1, result.getPostcode1().getPostcode());
    assertEquals("SW1A 2AA", result.getPostcode2().getPostcode());
    assertEquals("km", result.getDistance().getUnit());
    assertEquals(1225.5484327780487, result.getDistance().getValue().doubleValue());
  }
}
