package Domain.Store.Discount.DiscountTypes;


import DataLayer.Store.ORM.DataCondition;
import DataLayer.Store.ORM.DataStore;
import DataLayer.Store.ORM.Discount.DataCompositeDiscount;
import DataLayer.Store.ORM.Discount.DataDiscount;
import Domain.Store.Conditions.Condition;
import Domain.Store.Conditions.ConditionFactory;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class CompositeDiscount implements IDiscount {

    protected final Set<Condition> conditions;
    protected final Set<IDiscount> discounts;

    protected DataCompositeDiscount dataCompositeDiscount;

    public CompositeDiscount(DataCompositeDiscount dataCompositeDiscount, Set<Condition> conditions, Set<IDiscount> discounts) {
        this.dataCompositeDiscount = dataCompositeDiscount;
        this.conditions = conditions;
        this.discounts = discounts;
        for (IDiscount discount : this.discounts)
            discount.setFather(this);
    }

    public CompositeDiscount(DataCompositeDiscount dataCompositeDiscount) {
        this.dataCompositeDiscount = dataCompositeDiscount;
        conditions = new HashSet<>();
        discounts = new HashSet<>();
        for (DataCondition dataCondition : dataCompositeDiscount.getConditions())
            conditions.add(ConditionFactory.recover(dataCondition));
        for (DataDiscount dataDiscount : dataCompositeDiscount.getDiscounts().values())
            discounts.add(DiscountFactory.recover(dataDiscount));
    }

    @Override
    public int getDiscountId() {
        return dataCompositeDiscount.getId();
    }

    @Override
    public void setFather(CompositeDiscount father) {
        dataCompositeDiscount.setFather(father.dataCompositeDiscount);
    }

    @Override
    public void persist(DataStore dataStore) {
        dataCompositeDiscount.setStore(dataStore);
        dataCompositeDiscount = (DataCompositeDiscount) dataCompositeDiscount.persist();
        for (Condition condition : this.conditions)
            condition.persist(dataCompositeDiscount);
        for (IDiscount discount : this.discounts)
            discount.persist(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeDiscount that = (CompositeDiscount) o;
        return Objects.equals(conditions, that.conditions) &&
                Objects.equals(discounts, that.discounts) &&
                Objects.equals(getDiscountId(), that.getDiscountId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions, discounts, getDiscountId());
    }

    @Override
    public void remove() {
        dataCompositeDiscount.remove();
    }

    @Override
    public boolean isDependentOnProduct(int productID) {
        for (IDiscount discount : discounts)
            if (discount.isDependentOnProduct(productID))
                return true;
        return false;
    }

    @Override
    public Set<Integer> getDependentProducts() {
        Set<Integer> productIDs = new HashSet<>();
        for (IDiscount discount : discounts)
            productIDs.addAll(discount.getDependentProducts());
        return productIDs;
    }

    @Override
    public Set<Integer> getChildDiscountIds() {
        Set<Integer> ids = new HashSet<>();
        for (IDiscount discount : discounts) {
            ids.add(discount.getDiscountId());
            ids.addAll(discount.getChildDiscountIds());
        }
        return ids;
    }
}
