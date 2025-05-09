package com.assessment.postcode.model;

import com.assessment.postcode.dto.UkPostcodeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDistanceApiRs {

  private UkPostcodeDto postcode1;

  private UkPostcodeDto postcode2;

  private DistanceApiRs distance;
}
