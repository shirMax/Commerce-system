package util.Exceptions;

import util.Enums.ErrorStatus;

/**
 * Thrown for failures at any purchase process
 * e.g. couldn't remove the products from the store
 *
 * @invariant ErrorStatus.PURCHASE_ERROR_STATUSES.contains(this.getStatus())
 */
public class PurchaseError extends MarketException{

    public PurchaseError(String msg, ErrorStatus status){
        super(msg, status);
        if (!ErrorStatus.PURCHASE_ERROR_STATUSES.contains(status))
            throw new IllegalArgumentException("PurchaseError - Braking invariant.");
    }
}
