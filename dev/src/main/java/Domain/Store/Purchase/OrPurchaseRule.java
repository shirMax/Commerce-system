package Domain.Store.Purchase;

import DataLayer.Store.ORM.DataPurchaseRule;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Conditions.Condition;
import Domain.Store.Conditions.ConditionFactory;
import util.Enums.ErrorStatus;
import util.Exceptions.PurchaseLimitation;
import Domain.User.IStoreBasket;

import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class OrPurchaseRule extends PurchaseRule {

    Set<Condition> conditions;

    // CTOR for data creation
    public OrPurchaseRule(Set<Condition> conditions) {
        this.conditions = conditions;
    }

    // CTOR for recovery
    public OrPurchaseRule(DataPurchaseRule dataPurchaseRule) {
        super(dataPurchaseRule);

        this.conditions =
                dataPurchaseRule.getConditions()
                        .stream()
                        .map(ConditionFactory::recover)
                        .collect(Collectors.toSet());
    }

    @Override
    public void checkCondition(IStoreBasket basket) throws PurchaseLimitation {
        for (Condition condition : conditions) {
            if (condition.checkCondition(basket))
                return;
        }
        throw new PurchaseLimitation(
                "Cant purchase, condition is not satisfy: " + conditions.toString(),
                ErrorStatus.PURCHASE_LIMITATION
        );

    }

    @Override
    public void persist(DataStore dataStore) {
        super.persist(dataStore);

        conditions.forEach(c -> c.persist(dataPurchaseRule));
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(" or ");
        for (Condition condition : conditions) {
            stringJoiner.add(condition.toString());
        }
        return String.format("Or(ID=%d, %s)", getId(), stringJoiner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrPurchaseRule that = (OrPurchaseRule) o;
        return getId() == that.getId() &&
                Objects.equals(conditions, that.conditions);
    }
}
