package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.Store.Category;

public class ConditionFactory {

    public static Condition minBasketPrice(double price){
        return new MinBasketPrice(price);
    }

    public static Condition limitQuantity(int maxAmount, int productId){
        return new ProductLimit(productId, maxAmount);
    }

    public static Condition limitQuantity(int maxAmount, Category category){
        return new CategoryLimit(category, maxAmount);
    }

    public static Condition limitQuantity(int maxAmount){
        return new LimitQuantity(maxAmount);
    }

    public static Condition atLeastQuantity(int minAmount, int productId){
        return new ProductAtLeast(productId, minAmount);
    }

    public static Condition atLeastQuantity(int minAmount, Category category){
        return new CategoryAtLeast(category, minAmount);
    }

    public static Condition atLeastQuantity(int minAmount){
        return new AtLeast(minAmount);
    }

    public static Condition alcoholAge(){
        return new NoAlcoholUnder18();
    }

    public static Condition alcoholTime(){
        return new NoAlcoholAtNight();
    }

    public static Condition recover(DataCondition dataCondition) {
        switch (dataCondition.getConditionType()){
            case AtLeast -> {
                return new AtLeast(dataCondition);
            }
            case Limit -> {
                return new LimitQuantity(dataCondition);
            }
            case MinPrice -> {
                return new MinBasketPrice(dataCondition);
            }
            case ProductLimit -> {
                return new ProductLimit(dataCondition);
            }
            case CategoryLimit -> {
                return new CategoryLimit(dataCondition);
            }
            case ProductAtLeast -> {
                return new ProductAtLeast(dataCondition);
            }
            case CategoryAtLeast -> {
                return new CategoryAtLeast(dataCondition);
            }
            case NoAlcoholAtNight -> {
                return new NoAlcoholAtNight(dataCondition);
            }
            case NoAlcoholUnder18 -> {
                return new NoAlcoholUnder18(dataCondition);
            }
        }
        throw new RuntimeException(String.format("ConditionFactory: failed at recovery - type '%s' not found", dataCondition.getConditionType()));
    }
}
