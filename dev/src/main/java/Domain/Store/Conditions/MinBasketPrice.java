package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.User.IStoreBasket;

public class MinBasketPrice extends Condition{
    private final double price;

    // CTOR for data creation
    public MinBasketPrice(double price) {
        this.price = price;
    }

    // CTOR for recovery
    public MinBasketPrice(DataCondition dataCondition) {
        super(dataCondition);
        price = dataCondition.getPrice();
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        return basket.getBasketPriceAfterDiscount() >= price;
    }

    @Override
    public String toString(){
        return String.format("MinBasketPrice(%f)", getPrice());
    }
}
