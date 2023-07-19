package util.Records;

import Domain.User.IStoreBasket;

import java.time.LocalDateTime;
import java.util.Objects;

public record Transaction(int id, int storeId, String userName, IStoreBasket storeBasket, double price,
                          LocalDateTime timeStamp) {
    @Override
    public IStoreBasket storeBasket() {
        return storeBasket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id &&
                storeId == that.storeId &&
                Double.compare(that.price, price) == 0 &&
                Objects.equals(userName, that.userName) &&
                storeBasket.productsAreEqual(that.storeBasket) &&
                Objects.equals(timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storeId, userName, storeBasket, price, timeStamp);
    }
}
