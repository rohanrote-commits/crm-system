package com.example.crm_system_backend.utils;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class GeneralUtils {

    public static long maskOnId(long userId) {
        long maskId = 0;
        if (userId != 0) {
            maskId = userId << 2;
            Random r = new Random();
            int low = 1000;
            int high = 9999;
            int result = r.nextInt(high - low) + low;
            maskId = Long.parseLong(maskId + "" + result);
            maskId = maskId << 2;
        }

        return maskId;
    }

    public static long unmaskOnId(long userId) throws UserException {
        long unMaskId;
        int beginIndex = 4;
        if (userId != 0 && ("" + userId).length() > 4) {
            userId = userId >> 2;
            String inputAsString = "" + userId;
            long randomRemove = Long.parseLong((inputAsString.substring(0, inputAsString.length() - beginIndex)));
            unMaskId = randomRemove >> 2;
        } else {
            throw new UserException(ErrorCode.INVALID_USER_ID);
        }
        return unMaskId;
    }

}
