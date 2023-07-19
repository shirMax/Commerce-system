package Domain.Store.Purchase;

import DataLayer.Store.ORM.DataPurchaseRule;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Conditions.*;
import util.Exceptions.PurchaseLimitation;
import Domain.User.IStoreBasket;

import java.util.Map;

public abstract class PurchaseRule {
    public enum PurchaseType {
        IfThen,
        Or,
        And
    }
    private static final Map<Class<? extends PurchaseRule>, PurchaseType> RULE_TO_TYPE =
            Map.of(
                    AndPurchaseRule.class, PurchaseType.And,
                    OrPurchaseRule.class, PurchaseType.Or,
                    IfThenPurchaseRule.class, PurchaseType.IfThen
            );

    protected DataPurchaseRule dataPurchaseRule;

    public PurchaseRule() {
    }

    public PurchaseRule(DataPurchaseRule dataPurchaseRule) {
        this.dataPurchaseRule = dataPurchaseRule;
    }

    /**
     * Checks if the given basket violates this purchase rule.
     *
     * @param basket The basket to be checked.
     * @throws PurchaseLimitation If the basket violates the purchase rule.
     */
    public abstract void checkCondition(IStoreBasket basket) throws PurchaseLimitation;

    public int getId() {
        return dataPurchaseRule.getId();
    }

    public PurchaseType getPurchaseType(){
        return RULE_TO_TYPE.get(getClass());
    }

    public void persist(DataStore dataStore) {
        if (dataPurchaseRule != null) return;

        dataPurchaseRule = new DataPurchaseRule(dataStore, getPurchaseType());
    }

    public void remove(){
        dataPurchaseRule.remove();
    }

    public static PurchaseRule recover(DataPurchaseRule dataPurchaseRule) {
        switch (dataPurchaseRule.getPurchaseType()){
            case Or -> {
                return new OrPurchaseRule(dataPurchaseRule);
            }
            case And -> {
                return new AndPurchaseRule(dataPurchaseRule);
            }
            case IfThen -> {
                return new IfThenPurchaseRule(dataPurchaseRule);
            }
        }
        throw new RuntimeException(String.format("PurchaseRule: couldn't recovery - type '%s' not found", dataPurchaseRule.getPurchaseType()));
    }
}
