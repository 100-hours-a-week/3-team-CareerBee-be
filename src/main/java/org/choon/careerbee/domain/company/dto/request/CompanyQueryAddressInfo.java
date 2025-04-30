package org.choon.careerbee.domain.company.dto.request;

import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;

public record CompanyQueryAddressInfo(
    double latitude,
    double longitude
) {
  public CompanyQueryAddressInfo{
    if (latitude < 34 || latitude > 44) {
      throw new CustomException(CustomResponseStatus.INVALID_LATITUDE_ERROR);

    }

    if (longitude < 124 || longitude > 134) {
      throw new CustomException(CustomResponseStatus.INVALID_LONGITUDE_ERROR);
    }
  }

  public String toWKTPoint() {
    return String.format("POINT (%f %f)", this.latitude, this.longitude);
  }
}
