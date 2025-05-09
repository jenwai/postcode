package com.assessment.postcode.repository;


import com.assessment.postcode.entity.UkPostcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UkPostcodeRepo extends JpaRepository<UkPostcode, String> {

  Optional<UkPostcode> findByPostcode(String postcode);
}
