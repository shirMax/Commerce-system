package util.Exceptions;

import util.Enums.ErrorStatus;

/**
 * Thrown for trying to get nonexistence data
 *
 * @invariant ErrorStatus.N0N_EXISTENT_DATA_ERROR_STATUSES.contains(this.getStatus())
 */
public class NonExistentData extends MarketException{

    public NonExistentData(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.NON_EXISTENT_DATA_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("NonDataExistent - Braking invariant.");
    }
}
