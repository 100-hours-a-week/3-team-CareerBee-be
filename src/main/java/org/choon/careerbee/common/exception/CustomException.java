package org.choon.careerbee.common.exception;

import lombok.Getter;
import org.choon.careerbee.common.enums.CustomResponseStatus;

@Getter
public class CustomException extends RuntimeException{
    private final CustomResponseStatus customResponseStatus;

    public CustomException(CustomResponseStatus customResponseStatus) {
        super(customResponseStatus.getMessage());
        this.customResponseStatus = customResponseStatus;
    }
}
