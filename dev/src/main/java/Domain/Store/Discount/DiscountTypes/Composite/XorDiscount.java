package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataXorDiscount;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class XorDiscount extends CompositeDiscount {

    private final IDiscount discount1;
    private final IDiscount discount2;

    public XorDiscount(IDiscount discount1, IDiscount discount2) {
        super(new DataXorDiscount(null), Set.of(), Set.of(discount1, discount2));
        this.discount1 = discount1;
        this.discount2 = discount2;
    }

    public XorDiscount(DataXorDiscount dataDiscount) {
        super(dataDiscount);
        List<IDiscount> discountList = discounts.stream().toList();
        if (discountList.size() != 2)
            throw new RuntimeException("XorDiscount - failed at recovery, discounts.size()=" + discountList.size());
        discount1 = discountList.get(0);
        discount2 = discountList.get(1);
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        IStoreBasket basketByDiscount1 = basket.clone();
        IStoreBasket basketByDiscount2 = basket.clone();
        discount1.applyDiscountOnBasket(basketByDiscount1);
        discount2.applyDiscountOnBasket(basketByDiscount2);
        if(basketByDiscount1.getBasketPriceAfterDiscount() <= basketByDiscount2.getBasketPriceAfterDiscount())
            discount1.applyDiscountOnBasket(basket);
        else
            discount2.applyDiscountOnBasket(basket);
    }


    @Override
    public String toString(){
        StringJoiner stringJoiner = new StringJoiner(" xor ");
        stringJoiner.add(discount1.toString());
        stringJoiner.add(discount2.toString());
        return "ID: "+getDiscountId()+", " +stringJoiner.toString();
    }
}
