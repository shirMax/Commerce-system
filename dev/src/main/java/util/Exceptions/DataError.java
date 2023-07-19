package util.Exceptions;

import util.Enums.ErrorStatus;

/**
 * Thrown for trying to set faulty data
 * e.g. invalid username at register or negative price at setPrice
 *
 * @invariant ErrorStatus.DATA_ERROR_STATUSES.contains(this.getStatus())
 */
public class DataError extends MarketException{

    public DataError(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.DATA_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("DataError - Braking invariant.");
    }
}
