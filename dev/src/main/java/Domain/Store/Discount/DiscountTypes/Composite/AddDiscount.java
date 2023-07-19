package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataAddDiscount;
import Domain.Store.Category;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.CategoryDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.StoreDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;
import util.Records.StoreRecords.ProductRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddDiscount extends CompositeDiscount {

    private final Map<Integer, ProductDiscount> productDiscounts;
    private final Map<Category, CategoryDiscount> categoryDiscounts;
    private final Set<StoreDiscount> storeDiscounts;

    public AddDiscount(Set<StoreDiscount> storeDiscounts, Set<CategoryDiscount> categoryDiscounts, Set<ProductDiscount> productDiscounts){
        super(
                new DataAddDiscount(null),
                Set.of(),
                Stream.concat(Stream.concat(storeDiscounts.stream(), categoryDiscounts.stream()), productDiscounts.stream()).collect(Collectors.toSet())
        );
        this.storeDiscounts = storeDiscounts;
        this.categoryDiscounts =
                categoryDiscounts.stream()
                        .collect(Collectors.toMap(CategoryDiscount::getCategory, Function.identity()));
        this.productDiscounts =
                productDiscounts.stream()
                        .collect(Collectors.toMap(ProductDiscount::getDiscountId, Function.identity()));
    }

    public AddDiscount(DataAddDiscount dataDiscount) {
        super(dataDiscount);
        productDiscounts = new HashMap<>();
        categoryDiscounts = new HashMap<>();
        storeDiscounts = new HashSet<>();
        for (IDiscount discount : discounts) {
            if (discount instanceof  ProductDiscount)
                productDiscounts.put(((ProductDiscount) discount).getProductId(), (ProductDiscount) discount);
            if (discount instanceof  CategoryDiscount)
                categoryDiscounts.put(((CategoryDiscount) discount).getCategory(), (CategoryDiscount) discount);
            if (discount instanceof  StoreDiscount)
                storeDiscounts.add((StoreDiscount) discount);
        }
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        Double baseDiscount = 1.;
        for (StoreDiscount disc : storeDiscounts)
            baseDiscount -= (1 - disc.getPercentage());
        for (ProductRecord product : basket.getProductsAsRecords().values()){
            Double discount = baseDiscount;
            if (productDiscounts.containsKey(product.productId()))
                discount -= (1 - productDiscounts.get(product.productId()).getPercentage());
            if (categoryDiscounts.containsKey(product.productCategory()))
                discount -= (1 - categoryDiscounts.get(product.productCategory()).getPercentage());
            double discountedPrice = discount * product.priceAfterDiscount();
            ProductRecord newProduct = product.updatePriceAfterDiscount(discountedPrice);
            basket.updateProductRecord(newProduct);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(getDiscountId()).append(", Add discount rules\n");

        sb.append("Store Discounts:\n");
        for (StoreDiscount discount : storeDiscounts) {
            sb.append("- ").append(discount.getPercentage()).append("%\n");
        }

        sb.append("Category Discounts:\n");
        for (Map.Entry<Category, CategoryDiscount> entry : categoryDiscounts.entrySet()) {
            sb.append("- Category: ").append(entry.getKey()).append(", Discount: ").append(entry.getValue().getPercentage()).append("%\n");
        }

        sb.append("Product Discounts:\n");
        for (Map.Entry<Integer, ProductDiscount> entry : productDiscounts.entrySet()) {
            sb.append("- Product ID: ").append(entry.getKey()).append(", Discount: ").append(entry.getValue().getPercentage()).append("%\n");
        }

        return sb.toString();
    }
}
