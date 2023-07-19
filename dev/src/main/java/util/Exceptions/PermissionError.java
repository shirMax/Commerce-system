package util.Exceptions;

import util.Enums.ErrorStatus;

public class PermissionError extends MarketException{
    public PermissionError(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.PERMISSION_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("PermissionError - Braking invariant.");
    }
}
