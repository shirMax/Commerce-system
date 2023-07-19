package Domain.User;

import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataBasket;
import DataLayer.User.ORM.DataCart;
import DataLayer.User.ORM.DataMember;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCart implements IUserCart {
  private final Map<Integer, IStoreBasket> baskets;
  private DataCart dataCart;

  // CTOR for guest cart
  public UserCart(){
    this.baskets = new ConcurrentHashMap<>();
    dataCart = null;
  }

  // CTOR for member creation
  public UserCart(DataMember dataMember) {
    this.baskets = new ConcurrentHashMap<>();
    dataCart = new DataCart(dataMember);
    dataCart = dataCart.persist();
  }

  // CTOR for data pulled from DB
  public UserCart(DataCart dataCart){
    this.dataCart = dataCart;
    this.baskets = new ConcurrentHashMap<>();

    for (Map.Entry<Integer, DataBasket> entry : dataCart.getBaskets().entrySet()){
      this.baskets.put(entry.getKey(), new StoreBasket(entry.getValue()));
    }
  }

  @Override
  public List<IStoreBasket> getStoreBaskets() {
    return baskets.values().stream().toList();
  }

  @Override
  public IStoreBasket getStoreBasket(int storeID) {
    if (!baskets.containsKey(storeID))
      baskets.put(
              storeID,
              dataCart == null ? new DummyStoreBasket(storeID) : new StoreBasket(dataCart, storeID)
      );
    return baskets.get(storeID);
  }

  @Override
  public void addProducts(int storeId, List<ProductRecord> products) {
    IStoreBasket storeBasket = getStoreBasket(storeId);

    for (ProductRecord record : products) {
      storeBasket.addProduct(record);
    }
  }

  @Override
  public void removeProduct(int storeId, int productId, int quantity) throws NonExistentData {
    IStoreBasket storeBasket = getStoreBasket(storeId);
    ProductRecord record = storeBasket.getProductRecord(productId);
    if(record.quantity() - quantity <= 0 )
      storeBasket.removeProduct(productId);
    else{
      updateProductQuantity(storeId, productId, record.quantity() - quantity);
    }
    if (storeBasket.isEmpty()){
      storeBasket.remove();
      baskets.remove(storeId);
    }
  }

  @Override
  public void updateProductQuantity(int storeId, int productId, int quantity) throws NonExistentData {
    IStoreBasket storeBasket;
    storeBasket = getStoreBasket(storeId);

    storeBasket.updateProductQuantity(productId, quantity);
  }
}
