package util.Records.StoreRecords;

import Domain.Store.Category;

import java.util.List;

/**
 * Collection of attributes that distinguish products from one another.
 * Used to apply filter functions and get only the products which fulfill the given attributes
 *
 * @param storeId
 * @param storeName
 * @param productName
 * @param lowStoreRating
 * @param highStoreRating
 * @param productCategories
 * @param lowProductRating
 * @param highProductRating
 * @param lowPrice
 * @param highPrice
 * @apiNote Attribute types are non-primitive for the option of skipping certain attributes.
 * <br>null => do not filter by this attribute.
 */
public record ProductFilterAttributes(Integer storeId, String storeName,  String productName, Double lowStoreRating, Double highStoreRating, List<Category> productCategories,
                                      Double lowProductRating, Double highProductRating, Double lowPrice, Double highPrice) {
}

