package util.Exceptions;

import util.Enums.ErrorStatus;

/**
 * Thrown for trying to do something illegal relate to the session
 * e.g. guest trying to logout
 *
 * @invariant ErrorStatus.SESSION_ERROR_STATUSES.contains(this.getStatus())
 */
public class SessionError extends MarketException{

    public SessionError(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.SESSION_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("SessionError - Braking invariant.");
    }
}
