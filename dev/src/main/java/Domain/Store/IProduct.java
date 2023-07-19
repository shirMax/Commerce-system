package Domain.Store;

import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

public interface IProduct {
  int getStoreId();

  int getProductId();

  String getProductName();

  void setProductName(String name);

  double getProductPrice();

  void setProductPrice(double price);

  Category getProductCategory();

  void setProductCategory(Category newCategory);

  int getProductQuantity();

  void reduceProductQuantity(int quantity) throws DataError;

  void addingProductQuantity(int quantity) throws DataError;

  double getProductRating();

  void setProductRating(double newRating);

  void update(ProductRecord updated);

  void remove();
}
