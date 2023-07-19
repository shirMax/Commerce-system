package DataLayer.ORM;

import jakarta.persistence.*;

import java.io.Serializable;

@Embeddable
public class DataTransactedProductKey implements Serializable {
    @Column(name = "product_id")
    private int productID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private DataTransaction transaction;

    public DataTransactedProductKey() {
    }

    public DataTransactedProductKey(int productID, DataTransaction transaction) {
        this.productID = productID;
        this.transaction = transaction;
    }

    public int getProductID() {
        return productID;
    }

    public DataTransaction getTransaction() {
        return transaction;
    }
}
