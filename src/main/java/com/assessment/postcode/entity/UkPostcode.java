package com.assessment.postcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "UK_POSTCODE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UkPostcode {

  @Id
  @Column(name = "ID", unique = true, nullable = false)
  private long id;

  @Column(name = "POSTCODE", unique = true, nullable = false)
  private String postcode;

  @Column(name = "LATITUDE")
  private BigDecimal latitude;

  @Column(name = "LONGITUDE")
  private BigDecimal longitude;
}
