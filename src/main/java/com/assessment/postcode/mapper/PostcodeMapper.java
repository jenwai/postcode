package com.assessment.postcode.mapper;

import com.assessment.postcode.dto.UkPostcodeDto;
import com.assessment.postcode.entity.UkPostcode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostcodeMapper {

  UkPostcodeDto toDto(UkPostcode en);

  UkPostcode toEntity(UkPostcodeDto dto);
}
