package com.assessment.postcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UkPostcodeDto {

  private long id;

  private String postcode;

  private BigDecimal latitude;

  private BigDecimal longitude;
}
