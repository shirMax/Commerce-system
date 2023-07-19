package DataLayer.Store.ORM.Discount;

import Domain.Store.Category;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import util.Exceptions.DataError;

@Entity
@Table(name = "Discount_Category")
public class DataCategoryDiscount extends DataSimpleDiscount {
    @Enumerated(EnumType.STRING)
    private Category category;

    public DataCategoryDiscount() {
    }

    public DataCategoryDiscount(double percentage, Category category) {
        super(percentage);
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
