package util.Exceptions;

import util.Enums.ErrorStatus;

/**
 * Thrown for trying to use a taken data
 * e.g. register with existent username
 *
 * @invariant ErrorStatus.EXISTENT_DATA_ERROR_STATUSES.contains(this.getStatus())
 */
public class DataExistentError extends MarketException{

    public DataExistentError(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.EXISTENT_DATA_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("DataExistentError - Braking invariant.");
    }
}
