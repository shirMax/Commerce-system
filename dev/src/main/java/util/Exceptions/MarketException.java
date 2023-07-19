package util.Exceptions;

import util.Enums.ErrorStatus;

public abstract class MarketException extends Exception{

    private final ErrorStatus status;

    public MarketException(String msg, ErrorStatus status){
        super(msg);
        this.status = status;
    }

    public ErrorStatus getStatus() {
        return status;
    }

}
