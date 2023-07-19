package Domain.Store.Purchase;

import DataLayer.Store.ORM.DataCondition;
import DataLayer.Store.ORM.DataConditionRule;
import DataLayer.Store.ORM.DataPurchaseRule;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Conditions.Condition;
import Domain.Store.Conditions.ConditionFactory;
import util.Enums.ErrorStatus;
import util.Exceptions.PurchaseLimitation;
import Domain.User.IStoreBasket;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class IfThenPurchaseRule extends PurchaseRule {
    private final Condition ifTest;
    private final Condition thenTest;

    // CTOR for data creation
    public IfThenPurchaseRule(Condition ifTest, Condition thenTest) {
        this.ifTest = ifTest;
        this.thenTest = thenTest;
    }

    // CTOR for recovery
    public IfThenPurchaseRule(DataPurchaseRule dataPurchaseRule) {
        super(dataPurchaseRule);

        List<DataConditionRule> dataConditions =
                dataPurchaseRule.getConditions()
                        .stream()
                        .sorted(Comparator.comparingInt(DataCondition::getId)).toList();
        if (dataConditions.size() != 2) {
            throw new RuntimeException(String.format("IfThenPurchaseRule: Failed at recovery - dataConditions.size() == %d", dataConditions.size()));
        }
        ifTest = ConditionFactory.recover(dataConditions.get(0));
        thenTest = ConditionFactory.recover(dataConditions.get(1));
    }

    @Override
    public void checkCondition(@NonNull IStoreBasket basket) throws PurchaseLimitation {
        if (ifTest.checkCondition(basket)) {
            if (thenTest.checkCondition(basket))
                return;
            throw new PurchaseLimitation(
                    "Cant purchase, condition is not satisfy: " + thenTest,
                    ErrorStatus.PURCHASE_LIMITATION
            );
        }
    }

    @Override
    public void persist(DataStore dataStore) {
        super.persist(dataStore);

        ifTest.persist(dataPurchaseRule);
        thenTest.persist(dataPurchaseRule);
    }

    @Override
    public String toString() {
        return String.format("IfThen(ID=%d, %s, %s)", getId(), ifTest, thenTest);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfThenPurchaseRule that = (IfThenPurchaseRule) o;
        return getId() == that.getId() &&
                Objects.equals(ifTest, that.ifTest) &&
                Objects.equals(thenTest, that.thenTest);
    }
}
