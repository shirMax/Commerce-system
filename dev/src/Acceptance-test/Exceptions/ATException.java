package Exceptions;

import util.Enums.ErrorStatus;

public class ATException extends RuntimeException {
    public final ErrorStatus status;
    public ATException(String msg, ErrorStatus status) {
        super(msg);
        this.status = status;
    }
}
