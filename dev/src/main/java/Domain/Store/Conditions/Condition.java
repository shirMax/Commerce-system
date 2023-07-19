package Domain.Store.Conditions;

import DataLayer.Store.ORM.*;
import DataLayer.Store.ORM.Discount.DataDiscount;
import Domain.Store.Category;
import Domain.User.IStoreBasket;

import java.util.Map;
import java.util.Objects;

public abstract class Condition {
    public enum ConditionType {
        AtLeast,
        CategoryAtLeast,
        ProductAtLeast,
        Limit,
        CategoryLimit,
        ProductLimit,
        MinPrice,
        NoAlcoholAtNight,
        NoAlcoholUnder18
    }

    private static final Map<Class<? extends Condition>, ConditionType> CONDITION_TO_TYPE =
            Map.of(
                    AtLeast.class, ConditionType.AtLeast,
                    CategoryAtLeast.class, ConditionType.CategoryAtLeast,
                    ProductAtLeast.class, ConditionType.ProductAtLeast,
                    LimitQuantity.class, ConditionType.Limit,
                    CategoryLimit.class, ConditionType.CategoryLimit,
                    ProductLimit.class, ConditionType.ProductLimit,
                    MinBasketPrice.class, ConditionType.MinPrice,
                    NoAlcoholAtNight.class, ConditionType.NoAlcoholAtNight,
                    NoAlcoholUnder18.class, ConditionType.NoAlcoholUnder18
            );

    protected DataCondition dataCondition;

    public Condition() {
    }

    public Condition(DataCondition dataCondition) {
        this.dataCondition = dataCondition;
    }

    public abstract boolean checkCondition(IStoreBasket basket);

    public int getQuantity() {
        return 0;
    }

    public double getPrice() {
        return 0;
    }

    public int getProductID() {
        return 0;
    }

    public Category getCategory() {
        return null;
    }

    public ConditionType getConditionType() {
        return CONDITION_TO_TYPE.get(getClass());
    }

    public void persist(DataPurchaseRule dataPurchaseRule) {
        if (dataCondition != null) return;

        dataCondition = new DataConditionRule(dataPurchaseRule, getConditionType(), getQuantity(), getPrice(), getProductID(), getCategory());
    }

    public void persist(DataDiscount dataDiscount) {
        if (dataCondition != null) return;

        dataCondition = new DataConditionDiscount(dataDiscount, getConditionType(), getQuantity(), getPrice(), getProductID(), getCategory());
    }

    public void remove() {
        dataCondition.remove();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(dataCondition.getId(), condition.dataCondition.getId()) &&
                Objects.equals(getCategory(), condition.getCategory()) &&
                Objects.equals(getPrice(), condition.getPrice()) &&
                Objects.equals(getQuantity(), condition.getQuantity()) &&
                Objects.equals(getProductID(), condition.getProductID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategory(), getPrice(), getConditionType(), getQuantity(), getProductID());
    }
}
