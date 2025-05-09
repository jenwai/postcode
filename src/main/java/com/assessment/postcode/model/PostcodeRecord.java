package com.assessment.postcode.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PostcodeRecord(long id, String postcode, BigDecimal latitude, BigDecimal longitude) {
}
