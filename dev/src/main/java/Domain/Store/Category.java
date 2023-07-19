package Domain.Store;

public enum Category {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing"),
    BOOKS("Books"),
    BEAUTY("Beauty"),
    HOME("Home"),
    SPORTS("Sports"),
    TOYS("Toys"),
    FURNITURE("Furniture"),
    JEWELRY("Jewelry"),
    FOOD("Food"),
    AUTOMOTIVE("Automotive"),
    HEALTH("Health"),
    MUSIC("Music"),
    GARDEN("Garden"),
    PETS("Pets"),
    ART("Art"),
    OFFICE("Office"),
    CRAFTS("Crafts"),
    PARTY("Party"),
    BABY("Baby"),
    SHOES("Shoes"),
    WEDDING("Wedding"),
    FITNESS("Fitness"),
    TRAVEL("Travel"),
    HOBBIES("Hobbies"),
    ALCOHOL("Alcohol");

    private String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
