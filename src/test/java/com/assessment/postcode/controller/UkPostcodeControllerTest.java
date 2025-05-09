package com.assessment.postcode.controller;

import com.assessment.postcode.dto.UkPostcodeDto;
import com.assessment.postcode.model.DistanceApiRs;
import com.assessment.postcode.model.GetDistanceApiRs;
import com.assessment.postcode.model.UpdateCoordApiRq;
import com.assessment.postcode.service.PostcodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UkPostcodeController.class)
class UkPostcodeControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private PostcodeService postcodeService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String POSTCODE_1 = "SW1A 1AA";

  @Test
  @WithMockUser(username = "user", roles = {"ADMIN"})
  void testGetPostcodesDistance_Success() throws Exception {
    var distanceRs = new GetDistanceApiRs();
    var distance = new DistanceApiRs("km", new BigDecimal("5.0"));
    distanceRs.setDistance(distance);

    when(postcodeService.constructPostcodesDistance(POSTCODE_1, POSTCODE_1))
      .thenReturn(distanceRs);

    mockMvc.perform(get("/api/v1/postcodes/uk/distance")
        .param("postcode_1", POSTCODE_1)
        .param("postcode_2", POSTCODE_1)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.distance.unit").value("km"))
      .andExpect(jsonPath("$.distance.value").value(5.0));
  }

  @Test
  @WithMockUser(username = "user", roles = {"ADMIN"})
  void testGetPostcodesDistance_BadRequest() throws Exception {
    when(postcodeService.constructPostcodesDistance(any(), any()))
      .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postcodes"));

    mockMvc.perform(get("/api/v1/postcodes/uk/distance")
        .param("postcode_1", "XXX")
        .param("postcode_2", "YYY"))
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Invalid postcodes"));
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void testQueryUkPostcode_Success() throws Exception {
    var dto = new UkPostcodeDto();
    dto.setPostcode(POSTCODE_1);

    when(postcodeService.getByPostcode(POSTCODE_1)).thenReturn(dto);

    mockMvc.perform(get("/api/v1/postcodes/uk/SW1A 1AA"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.postcode").value(POSTCODE_1));
  }

  @Test
  void testQueryUkPostcode_NotFound() throws Exception {
    when(postcodeService.getByPostcode("XXX"))
      .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

    mockMvc.perform(get("/api/v1/postcodes/uk/XXX")
        .with(user("admin").roles("ADMIN")))
      .andExpect(status().isNotFound())
      .andExpect(content().string("Not found"));
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void testUpdateTrxDesc_Success() throws Exception {
    var dto = new UkPostcodeDto();
    dto.setPostcode(POSTCODE_1);

    var rq = new UpdateCoordApiRq();
    rq.setLatitude(new BigDecimal("51.5074"));
    rq.setLongitude(new BigDecimal("-0.1278"));

    when(postcodeService.updateCoordinatesByPostcode(POSTCODE_1, new BigDecimal("51.5074"),
      new BigDecimal("-0.1278")))
      .thenReturn(dto);

    mockMvc.perform(patch("/api/v1/postcodes/uk/SW1A 1AA/coordinates")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(rq))
        .with(csrf())
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.postcode").value(POSTCODE_1));
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void testUpdateTrxDesc_BadRequest() throws Exception {
    var rq = new UpdateCoordApiRq();
    rq.setLatitude(new BigDecimal("100.0"));
    rq.setLongitude(new BigDecimal("-0.1278"));

    when(postcodeService.updateCoordinatesByPostcode(any(), any(), any()))
      .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates"));

    mockMvc.perform(patch("/api/v1/postcodes/uk/SW1A 1AA/coordinates")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(rq))
        .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Invalid coordinates"));
  }
}
