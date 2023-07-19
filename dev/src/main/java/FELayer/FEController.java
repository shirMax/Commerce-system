package FELayer;

import Domain.Services.NotificationService.NotificationObserver;
import Domain.Store.Conditions.ConditionFactory;
import Domain.Store.Discount.DiscountTypes.Composite.*;
import Domain.Store.Discount.DiscountTypes.Simple.CategoryDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.StoreDiscount;
import Domain.Store.Offer;
import Domain.Store.OwnerAppointmentContract;
import Domain.Store.Purchase.*;
import util.Exceptions.DataError;
import util.Records.Transaction;
import Domain.Store.Category;
import Domain.Store.Conditions.Condition;
import Domain.Store.Discount.IDiscount;
import Service.Result;
import Service.SystemFacade;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import util.Enums.PermissionType;
import util.Records.AddressRecord;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class FEController {
    @Autowired
    private SystemFacade apiService;
    private static final Map<String, NotificationObserver> observers = new ConcurrentHashMap<>(); // <session_id, observer for that session>

    private void initNavigationBar(Model model, String sessionId) {
        List<NavigationLink> navigationLinks = new ArrayList<>();

        // Add the navigation links to the list
        Result<Boolean> guestSessionResult = apiService.isGuestSession(sessionId);
        if (guestSessionResult.isOk()) {
            if (guestSessionResult.getValue()) {
                navigationLinks.add(new NavigationLink("Cart", "/cart"));
                navigationLinks.add(new NavigationLink("Login", "/login"));
                navigationLinks.add(new NavigationLink("SignUp", "/register"));
            } else {
                Result<Boolean> systemManagerSessionResult = apiService.isSystemManagerSession(sessionId);
                if(systemManagerSessionResult.isOk()) {
                    if(systemManagerSessionResult.getValue()) {
                        navigationLinks.add(new NavigationLink("Logout", "/logout"));
                    }
                    else {
                        Result<Boolean> memberSessionResult = apiService.isMemberSession(sessionId);
                        if(memberSessionResult.isOk() && memberSessionResult.getValue()) {
                            navigationLinks.add(new NavigationLink("Cart", "/cart"));
                            navigationLinks.add(new NavigationLink("My Stores", "/stores"));
                            navigationLinks.add(new NavigationLink("My Profile", "/profile"));
                            navigationLinks.add(new NavigationLink("Logout", "/logout"));
                        }
                    }
                }

            }
        }
        model.addAttribute("navigationLinks", navigationLinks);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response, Model model, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        apiService.unsubscribeFromNotifications(sessionId, observers.get(sessionId));
        apiService.logout(sessionId);
        return showHomePage(model, session, response, request);
    }

    @GetMapping("/marketManager")
    public String showMarketManagerPage(HttpSession session, HttpServletResponse response, Model model, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildMarketManagerPage(sessionId, model);
    }

    private String getSessionId(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = "";
        if (session.isNew()) {
            sessionId = session.getId();
            Cookie cookie = new Cookie("sessionId", sessionId);
            cookie.setMaxAge(24 * 60 * 60); // 24 hours
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            apiService.connect(sessionId);
            return sessionId;
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("sessionId")) {
                        sessionId = cookie.getValue();
                        return sessionId;
                    }
                }
            }
        }
        return ""; // todo: Block the user from accessing our website because he edited his cookies!
    }

    @GetMapping("/")
    public String showHomePage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(isManagerSession(sessionId))
            return showMarketManagerPage(session, response, model, request);
        Result<Set<StoreRecord>> storesResult = apiService.getStores();
        if(storesResult.errorOccured()) {
            return showForbiddenPage();
        }
        model.addAttribute("stores", storesResult.getValue());
        ProductFilterAttributes productFilterAttributes = new ProductFilterAttributes(null, null, null, null, null, null, null, null, null, null);
        Result<List<ProductRecord>> recommendedProducts = apiService.getProductsBy(sessionId, productFilterAttributes);
        if(recommendedProducts.isOk()) {
            model.addAttribute("recommendedProducts", recommendedProducts.getValue());
        }
        initNavigationBar(model, sessionId);
        return "homepage";
    }

    private boolean isManagerSession(String sessionId) {
        Result<Boolean> isManagerSessionResult = apiService.isSystemManagerSession(sessionId);
        return isManagerSessionResult.isOk() && isManagerSessionResult.getValue();
    }

    @GetMapping("/discounts")
    public String showDiscountsPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @GetMapping("/purchaseRules")
    public String showPurchaseRulesPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildPurchaseRulesPage(sessionId, storeId, model);
    }

    @GetMapping("/storageManagement")
    public String showStorageManagementPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildStorageManagementPage(sessionId, storeId, model);
    }

    @GetMapping("/storeManagers")
    public String showStoreManagersPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildStoreManagersPage(sessionId, storeId, model);
    }

    @GetMapping("/storeOwners")
    public String showStoreOwnersPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        return buildStoreOwnersPage(sessionId, storeId, model);
    }

    private String buildStoreOwnersPage(String sessionId, Integer storeId, Model model) {
        Result<Integer> memberPermissions = apiService.getMemberPermissions(sessionId, storeId);
        if(memberPermissions.errorOccured()) {
            return showForbiddenPage();
        }
        initNavigationBar(model, sessionId);
        buildPermissions(model, memberPermissions.getValue());

        Result<List<String>> ownersResult = apiService.getStoreOwners(sessionId, storeId);
        if(ownersResult.errorOccured()) {
            model.addAttribute("errorMessage", ownersResult.getErrorMessage());
            return "storeOwners";
        }
        List<String> owners = ownersResult.getValue();

        model.addAttribute("owners", owners);
        model.addAttribute("storeId", storeId);
        Result<StoreRecord> storeInfoResult = apiService.getStoreInfo(sessionId, storeId);
        if(storeInfoResult.isOk())
            model.addAttribute("storeName", storeInfoResult.getValue().storeName());
        return "storeOwners";
    }

    @GetMapping("/PurchaseHistory")
    public String showSystemManagerPurchaseHistoryPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(!isManagerSession(sessionId))
            return showForbiddenPage();
        Result<List<Transaction>> transactionHistoryResult =  apiService.getTransactionHistory(sessionId);
        if(transactionHistoryResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("transactionsHistory", transactionHistoryResult.getValue());
        initNavigationBar(model, sessionId);
        return "PurchaseHistory";
    }

    @GetMapping("/MemberPurchaseHistory")
    public String showMemberPurchaseHistoryPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result<List<Transaction>> transactionHistoryResult =  apiService.getMyTransactionHistory(sessionId, LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.of(2099, 1, 1, 0, 0, 0));
        if(transactionHistoryResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("transactionsHistory", transactionHistoryResult.getValue());
        initNavigationBar(model, sessionId);
        return "PurchaseHistory";
    }


    @GetMapping("/StorePurchaseHistory")
    public String showStorePurchaseHistoryPage(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result<List<Transaction>> transactionHistoryResult =  apiService.getStoreTransactionHistory(sessionId, storeId,
                LocalDateTime.of(2000, 1, 1, 0, 0, 0), LocalDateTime.of(2099, 1, 1, 0, 0, 0));
        if(transactionHistoryResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("transactionsHistory", transactionHistoryResult.getValue());
        initNavigationBar(model, sessionId);
        return "PurchaseHistory";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, HttpServletResponse response, Model model, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result<Boolean> isMemberSessionResult =  apiService.isMemberSession(sessionId);
        Result<UserRecord> memberDetailsResult = apiService.getMemberDetails(sessionId);
        if(isMemberSessionResult.errorOccured() || memberDetailsResult.errorOccured() || !isMemberSessionResult.getValue())
            return showForbiddenPage();
        model.addAttribute("user", memberDetailsResult.getValue());
        initNavigationBar(model, sessionId);
        return "Profile";
    }

    @GetMapping("/forbidden")
    public String showForbiddenPage() {
        return "forbidden";
    }

    @GetMapping("/StoreActions")
    public String showStoreActionsPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request, @RequestParam(required=true) Integer storeId) {
        String sessionId = getSessionId(session, response, request);
        return buildStoreActionsPage(model, sessionId, storeId);
    }

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        return "register";
    }

    @GetMapping("/contactus")
    public String showContactUsPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(isManagerSession(sessionId))
            return showMarketManagerPage(session, response, model, request);
        initNavigationBar(model, sessionId);
        return "contactus";
    }
    @GetMapping("/about")
    public String showAboutPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(isManagerSession(sessionId))
            return showMarketManagerPage(session, response, model, request);
        initNavigationBar(model, sessionId);
        return "about";
    }
    @GetMapping("/search")
    public String showSearchPage(
            Model model,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request,
            @RequestParam(name = "searchKey", required = false) String searchKey,
            @RequestParam(name = "storeName", required = false) String storeName,
            @RequestParam(name = "category", required = false) List<String> productCategories,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "minStoreRate", required = false) Double minStoreRate,
            @RequestParam(name = "maxStoreRate", required = false) Double maxStoreRate,
            @RequestParam(name = "minProductRate", required = false) Double minProductRate,
            @RequestParam(name = "maxProductRate", required = false) Double maxProductRate
    ) {
        String sessionId = getSessionId(session, response, request);
        if(isManagerSession(sessionId))
            return showMarketManagerPage(session, response, model, request);
        initNavigationBar(model, sessionId);
        List<Category> categories = new ArrayList<>();
        if(productCategories!=null)
            for(String productCategoryStr : productCategories) {
                categories.add(Category.valueOf(productCategoryStr));
            }
        ProductFilterAttributes productFilterAttributes = new ProductFilterAttributes(null, storeName, searchKey, minStoreRate, maxStoreRate, categories, minProductRate, maxProductRate, minPrice, maxPrice);
        Result<List<ProductRecord>> filteredProducts = apiService.getProductsBy(sessionId, productFilterAttributes);
        if(filteredProducts.isOk()) {
            model.addAttribute("products", filteredProducts.getValue());
        }
        return "search";
    }
    @GetMapping("/cart")
    public String showCartPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        buildCartPage(model, sessionId);
        return "cart";
    }

    @GetMapping("/storeOffers")
    public String showStoreOffers(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        Result<Map<Integer, Offer>> storeOffersResult = apiService.getOffers(sessionId, storeId);
        if(storeOffersResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeOffers", new ArrayList<>(storeOffersResult.getValue().values()));
        model.addAttribute("storeId", storeId);
        return "storeOffers";
    }

    @GetMapping("/storeContracts")
    public String showStoreContracts(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        Result<Map<Integer, OwnerAppointmentContract>> storeContractsResult = apiService.getContracts(sessionId, storeId);
        if(storeContractsResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeContracts", new ArrayList<>(storeContractsResult.getValue().values()));
        model.addAttribute("storeId", storeId);
        return "storeContracts";
    }

    @GetMapping("/myOffers")
    public String showMyOffers(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        Result<Map<Integer, Offer>> memberOffersResult = apiService.getMemberOffers(sessionId);
        if(memberOffersResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeOffers", new ArrayList<>(memberOffersResult.getValue().values()));
        return "myOffers";
    }

    @GetMapping("/product")
    public String showProductPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request, @RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId) {
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        Result<ProductRecord> productRecordResult = apiService.getProductInfo(sessionId, productId, storeId);
        Result<Boolean> memberResult = apiService.isMemberSession(sessionId);
        if(productRecordResult.errorOccured() || memberResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("product", productRecordResult.getValue());
        model.addAttribute("isMember", memberResult.getValue());
        return "product";
    }

    @GetMapping("/stores")
    public String showStoresPage(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        buildMyStores(model, sessionId);
        return "stores";
    }

    //removeMember
    @PostMapping("/removeMember")
    public String removeMember(@RequestParam("username") String username, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result removeMemberResult = apiService.removeMember(sessionId, username);
        if(removeMemberResult.errorOccured()) {
            model.addAttribute("errorMessage", removeMemberResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "member removed successfully!");
        return buildMarketManagerPage(sessionId, model);
    }

    @PostMapping("/addSystemManager")
    public String addSystemManager(@RequestParam("username") String username, @RequestParam("password") String password, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result addSystemManagerResult = apiService.createSystemManager(sessionId, username, password);
        if(addSystemManagerResult.errorOccured()) {
            model.addAttribute("errorMessage", addSystemManagerResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "system manager added successfully!");
        return showMarketManagerPage(session, response, model, request);
    }

    @PostMapping("/makeOffer")
    public String makeOffer(@RequestParam("offerPrice") Double offerPrice, @RequestParam("quantity") Integer quantity, @RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result publishOfferResult = apiService.memberPublishOffer(sessionId, storeId, productId, offerPrice, quantity);
        if(publishOfferResult.errorOccured()) {
            model.addAttribute("errorMessage", publishOfferResult.getErrorMessage());
            return showProductPage(model, session, response, request, storeId, productId);
        }
        model.addAttribute("successMessage", "offer made successfully!");
        return showHomePage(model, session, response, request);
    }

    @PostMapping("/acceptContract")
    public String acceptContract(@RequestParam("storeId") Integer storeId, @RequestParam("contractId") Integer contractId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result consentContractResult = apiService.consentContract(sessionId, storeId, contractId);
        if(consentContractResult.errorOccured()) {
            model.addAttribute("errorMessage", consentContractResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "contract accepted successfully!");
        return showStoreContracts(storeId, model, session, response, request);
    }

    @PostMapping("/rejectContract")
    public String rejectContract(@RequestParam("storeId") Integer storeId, @RequestParam("contractId") Integer contractId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result rejectContractResult = apiService.removeContract(sessionId, storeId, contractId);
        if(rejectContractResult.errorOccured()) {
            model.addAttribute("errorMessage", rejectContractResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "contract rejected successfully!");
        return showStoreContracts(storeId, model, session, response, request);
    }

    @PostMapping("/acceptOffer")
    public String acceptOffer(@RequestParam("storeId") Integer storeId, @RequestParam("offerId") Integer offerId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result consentOfferResult = apiService.consentOffer(sessionId, offerId, storeId);
        if(consentOfferResult.errorOccured()) {
            model.addAttribute("errorMessage", consentOfferResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "offer accepted successfully!");
        return showStoreOffers(storeId, model, session, response, request);
    }

    @PostMapping("/rejectOffer")
    public String rejectOffer(@RequestParam("storeId") Integer storeId, @RequestParam("offerId") Integer offerId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result rejectOfferResult = apiService.storeRejectOffer(sessionId, offerId, storeId);
        if(rejectOfferResult.errorOccured()) {
            model.addAttribute("errorMessage", rejectOfferResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "offer rejected successfully!");
        return showStoreOffers(storeId, model, session, response, request);
    }

    @PostMapping("/memberRejectOffer")
    public String memberRejectOffer(@RequestParam("offerId") Integer offerId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result rejectOfferResult = apiService.memberRejectOffer(sessionId, offerId);
        if(rejectOfferResult.errorOccured()) {
            model.addAttribute("errorMessage", rejectOfferResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "offer rejected successfully!");
        return showMyOffers(model, session, response, request);
    }

    @PostMapping("/purchaseOffer")
    public String purchaseOffer(@RequestParam("offerId") Integer offerId, @RequestParam("storeId") Integer storeId, @RequestParam("fullName") String fullName, @RequestParam("street") String street, @RequestParam("city") String city, @RequestParam("country") String country, @RequestParam("zip") String zip, @RequestParam("phoneNumber") String phoneNumber, @RequestParam("card_owner") String card_owner, @RequestParam("card_number") String card_number, @RequestParam("expiry_date") String expiry_date, @RequestParam("cvv") String cvv,  Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        YearMonth ym = YearMonth.parse(expiry_date, fmt);
        LocalDate expirationDate = ym.atDay(1); // choose whatever day you want
        Result purchaseBidResult = apiService.purchaseBid(sessionId, new PaymentDetails(card_owner, card_number, expirationDate, cvv),  new AddressRecord(fullName, street, city, country, zip, phoneNumber), offerId, storeId);
        if(purchaseBidResult.errorOccured()) {
            model.addAttribute("errorMessage", purchaseBidResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "offer purchased successfully!");
        return showMyOffers(model, session, response, request);
    }

    @PostMapping("/counteroffer")
    public String counteroffer(@RequestParam("storeId") Integer storeId, @RequestParam("offerId") Integer offerId, @RequestParam("quantity") Integer quantity, @RequestParam("price") Double price, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result counterOfferResult = apiService.counterOffer(sessionId, storeId, offerId, quantity, price);
        if(counterOfferResult.errorOccured()) {
            model.addAttribute("errorMessage", counterOfferResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "offer countered successfully!");
        return showStoreOffers(storeId, model, session, response, request);
    }

    //Login
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result loginResult = apiService.login(sessionId, username, password);
        if(loginResult.isOk()) {
            // Login successful, redirect to home page
            observers.put(sessionId, n -> WebSocketHandler.sendMessageToClient(sessionId, n));
            Result subResult = apiService.subscribeToNotifications(sessionId, observers.get(sessionId));
            if (subResult.errorOccured()){
                // TODO: This is not expected and highly unlikely. So I have no idea what to do here.
            }
            return showHomePage(model, session, response, request);
        } else {
            // Login failed, display error message
            model.addAttribute("errorMessage", loginResult.getErrorMessage());
            initNavigationBar(model, sessionId);
            return "Login";
        }
    }

    @PostMapping("/getNotifications")
    public void getNotifications(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        apiService.getAllPendingMessages(sessionId);
    }

    @PostMapping("/editCartProduct")
    public String editCartProduct(@RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, @RequestParam("quantity") Integer quantity, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = null;
        if(quantity > 0) {
            result = apiService.addProductToCart(sessionId, productId, storeId, quantity);
        }
        else {
            result = apiService.removeProductFromCart(sessionId, productId, storeId, Math.abs(quantity));
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        buildCartPage(model, sessionId);
        return "cart";
    }

    private Condition createCondition(String conditionName, List<String> parameters, int[] conditionDataIndex) {
        switch (conditionName) {
            case "basketNotContainsMoreThenByProduct":
                conditionDataIndex[0]++;
                int productId = Integer.parseInt(parameters.get(0));
                int totalQuantity = Integer.parseInt(parameters.get(1));
                return ConditionFactory.limitQuantity(totalQuantity, productId);
            case "basketNotContainsMoreThenByCategory":
                conditionDataIndex[0]++;
                String categoryName = parameters.get(0);
                totalQuantity = Integer.parseInt(parameters.get(1));
                return ConditionFactory.limitQuantity(totalQuantity, Category.valueOf(categoryName));
            case "basketNotContainsMoreThenByAllBasket":
                conditionDataIndex[0]++;
                totalQuantity = Integer.parseInt(parameters.get(0));
                return ConditionFactory.limitQuantity(totalQuantity);
            case "basketNotContainsLessThenByAllBasket":
                conditionDataIndex[0]++;
                totalQuantity = Integer.parseInt(parameters.get(0));
                return ConditionFactory.atLeastQuantity(totalQuantity);
            case "basketNotContainsLessThenByCategory":
                conditionDataIndex[0]++;
                categoryName = parameters.get(0);
                totalQuantity = Integer.parseInt(parameters.get(1));
                return ConditionFactory.atLeastQuantity(totalQuantity, Category.valueOf(categoryName));
            case "basketNotContainsLessThenByProduct":
                conditionDataIndex[0]++;
                productId = Integer.parseInt(parameters.get(0));
                totalQuantity = Integer.parseInt(parameters.get(1));
                return ConditionFactory.atLeastQuantity(totalQuantity, productId);
            case "notAllowedToBuyAlcoholUnder18":
                return ConditionFactory.alcoholAge();
            case "notAllowedToBuyAlcohol23To06":
                return ConditionFactory.alcoholTime();
            // Handle other conditions similarly
            default:
                throw new IllegalArgumentException("Invalid condition name: " + conditionName);
        }
    }

    @PostMapping("/addIfThenPurchaseRule")
    public String addIfThenPurchaseRule(@RequestParam("storeId") Integer storeId,
                                        @RequestParam("conditions") List<String> conditionNames,
                                        @RequestParam(value = "conditionData", required = false) List<List<String>> conditionData,
                                        Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        List<Condition> conditions = new ArrayList<>();
        int[] conditionDataIndex = new int[1];
        for (int i = 0; i < conditionNames.size(); i++) {
            String conditionName = conditionNames.get(i);
            List<String> parameters = null;
            if(conditionData!= null)
                parameters = conditionData.get(conditionDataIndex[0]);
            Condition condition = createCondition(conditionName, parameters, conditionDataIndex);
            conditions.add(condition);
        }

        IfThenPurchaseRule rule = new IfThenPurchaseRule(conditions.get(0), conditions.get(1));
        Result result = apiService.addPurchaseRule(sessionId, storeId, rule);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "if then purchase rule added successfully!");
        return buildPurchaseRulesPage(sessionId, storeId, model);
    }

    @PostMapping("/addOrPurchaseRule")
    public String addOrPurchaseRule(@RequestParam("storeId") Integer storeId,
                                        @RequestParam("conditions") List<String> conditionNames,
                                        @RequestParam(value = "conditionData", required = false) List<List<String>> conditionData,
                                        Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Set<Condition> conditions = new HashSet<>();
        int[] conditionDataIndex = new int[1];
        for (int i = 0; i < conditionNames.size(); i++) {
            String conditionName = conditionNames.get(i);
            List<String> parameters = null;
            if(conditionData!= null)
                parameters = conditionData.get(conditionDataIndex[0]);
            Condition condition = createCondition(conditionName, parameters, conditionDataIndex);
            conditions.add(condition);
        }

        OrPurchaseRule rule = new OrPurchaseRule(conditions);
        Result result = apiService.addPurchaseRule(sessionId, storeId, rule);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "or purchase rule added successfully!");
        return buildPurchaseRulesPage(sessionId, storeId, model);
    }

    @PostMapping("/addAndPurchaseRule")
    public String addAndPurchaseRule(@RequestParam("storeId") Integer storeId,
                                        @RequestParam("conditions") List<String> conditionNames,
                                        @RequestParam(value = "conditionData", required = false) List<List<String>> conditionData,
                                        Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Set<Condition> conditions = new HashSet<>();
        int[] conditionDataIndex = new int[1];
        for (int i = 0; i < conditionNames.size(); i++) {
            String conditionName = conditionNames.get(i);
            List<String> parameters = null;
            if(conditionData!= null)
                parameters = conditionData.get(conditionDataIndex[0]);
            Condition condition = createCondition(conditionName, parameters, conditionDataIndex);
            conditions.add(condition);
        }

        AndPurchaseRule rule = new AndPurchaseRule(conditions);
        Result result = apiService.addPurchaseRule(sessionId, storeId, rule);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "and purchase rule added successfully!");
        return buildPurchaseRulesPage(sessionId, storeId, model);
    }

    @PostMapping("/addProductDiscount")
    public String addProductDiscount(@RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, @RequestParam("discountPercentage") Double discountPercentage, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new ProductDiscount(discountPercentage, productId));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "product discount added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addCategoryDiscount")
    public String addCategoryDiscount(@RequestParam("storeId") Integer storeId, @RequestParam("productCategory") String category, @RequestParam("discountPercentage") Double discountPercentage, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new CategoryDiscount(discountPercentage, Category.valueOf(category)));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "category discount added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addStoreDiscount")
    public String addStoreDiscount(@RequestParam("storeId") Integer storeId, @RequestParam("discountPercentage") Double discountPercentage, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new StoreDiscount(discountPercentage));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store discount added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addXorDiscountRule")
    public String addXorDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountId1") Integer discountId1,
                                     @RequestParam("discountId2") Integer discountId2, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result<IDiscount> discount1Result = apiService.getStoreDiscount(sessionId, storeId, discountId1);
        Result<IDiscount> discount2Result = apiService.getStoreDiscount(sessionId, storeId, discountId2);
        if(discount1Result.errorOccured()) {
            model.addAttribute("errorMessage", discount1Result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(discount2Result.errorOccured()) {
            model.addAttribute("errorMessage", discount2Result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        Result result = apiService.addDiscount(sessionId, storeId, new XorDiscount(discount1Result.getValue(), discount2Result.getValue()));
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "xor discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addOrDiscountRule")
    public String addOrDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountId") Integer discountId,
                                    @RequestParam(value = "MinimumBasketPrice", required = false) Integer minimumBasketPrice,
                                    @RequestParam(value = "MinimumProductQuantity", required = false) Integer minimumProductQuantity,
                                    @RequestParam(value = "MinimumProductId", required = false) Integer minimumProductId,
                                    @RequestParam(value = "MinimumCategoryQuantity", required = false) Integer minimumCategoryQuantity,
                                    @RequestParam(value = "category", required = false) String category,
                                    Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(minimumBasketPrice == null && minimumProductQuantity == null && minimumProductId == null && minimumCategoryQuantity == null && category==null) {
            model.addAttribute("errorMessage", "can't create or discount without conditions!");
            return buildDiscountsPage(sessionId, storeId, model);
        }
        Set<Condition> conditionList = new HashSet<>();
        Result<IDiscount> discountResult = apiService.getStoreDiscount(sessionId, storeId, discountId);
        if(discountResult.errorOccured()) {
            model.addAttribute("errorMessage", discountResult.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(minimumBasketPrice != null) {
            conditionList.add(ConditionFactory.minBasketPrice(minimumBasketPrice));
        }
        if(minimumCategoryQuantity != null && category != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumCategoryQuantity, Category.valueOf(category)));
        }
        if(minimumProductId != null && minimumProductQuantity != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumProductQuantity, minimumProductId));
        }
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new OrDiscount(conditionList, discountResult.getValue()));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "or discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addAndDiscountRule")
    public String addAndDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountId") Integer discountId,
                                     @RequestParam(value = "MinimumBasketPrice", required = false) Integer minimumBasketPrice,
                                     @RequestParam(value = "MinimumProductQuantity", required = false) Integer minimumProductQuantity,
                                     @RequestParam(value = "MinimumProductId", required = false) Integer minimumProductId,
                                     @RequestParam(value = "MinimumCategoryQuantity", required = false) Integer minimumCategoryQuantity,
                                     @RequestParam(value = "category", required = false) String category,
                                     Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(minimumBasketPrice == null && minimumProductQuantity == null && minimumProductId == null && minimumCategoryQuantity == null && category==null) {
            model.addAttribute("errorMessage", "can't create and discount without conditions!");
            return buildDiscountsPage(sessionId, storeId, model);
        }
        Set<Condition> conditionList = new HashSet<>();
        Result<IDiscount> discountResult = apiService.getStoreDiscount(sessionId, storeId, discountId);
        if(discountResult.errorOccured()) {
            model.addAttribute("errorMessage", discountResult.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(minimumBasketPrice != null) {
            conditionList.add(ConditionFactory.minBasketPrice(minimumBasketPrice));
        }
        if(minimumCategoryQuantity != null && category != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumCategoryQuantity, Category.valueOf(category)));
        }
        if(minimumProductId != null && minimumProductQuantity != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumProductQuantity, minimumProductId));
        }
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new AndDiscount(conditionList, discountResult.getValue()));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "and discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addIfThenDiscountRule")
    public String addIfThenDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountId") Integer discountId,
                                        @RequestParam(value = "MinimumBasketPrice", required = false) Integer minimumBasketPrice,
                                        @RequestParam(value = "MinimumProductQuantity", required = false) Integer minimumProductQuantity,
                                        @RequestParam(value = "MinimumProductId", required = false) Integer minimumProductId,
                                        @RequestParam(value = "MinimumCategoryQuantity", required = false) Integer minimumCategoryQuantity,
                                        @RequestParam(value = "category", required = false) String category,
                                        Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        if(minimumBasketPrice == null && minimumProductQuantity == null && minimumProductId == null && minimumCategoryQuantity == null && category==null) {
            model.addAttribute("errorMessage", "can't create if then discount without condition!");
            return buildDiscountsPage(sessionId, storeId, model);
        }
        List<Condition> conditionList = new ArrayList<>();
        Result<IDiscount> discountResult = apiService.getStoreDiscount(sessionId, storeId, discountId);
        if(discountResult.errorOccured()) {
            model.addAttribute("errorMessage", discountResult.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        if(minimumBasketPrice != null) {
            conditionList.add(ConditionFactory.minBasketPrice(minimumBasketPrice));
        }
        if(minimumCategoryQuantity != null && category != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumCategoryQuantity, Category.valueOf(category)));
        }
        if(minimumProductId != null && minimumProductQuantity != null) {
            conditionList.add(ConditionFactory.atLeastQuantity(minimumProductQuantity, minimumProductId));
        }
        if(conditionList.size() != 1) {
            model.addAttribute("errorMessage", "If then discount must have exactly one condition only!");
            return buildDiscountsPage(sessionId, storeId, model);
        }
        Result result = apiService.addDiscount(sessionId, storeId, new IfThenDiscount(conditionList.get(0), discountResult.getValue()));
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "if then discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addMaxDiscountRule")
    public String addMaxDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountIds") List<Integer> discountIds, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Set<IDiscount> discounts = new HashSet<>();
        for(int discountId : discountIds) {
            Result<IDiscount> discountResult = apiService.getStoreDiscount(sessionId, storeId, discountId);
            if(discountResult.errorOccured()) {
                model.addAttribute("errorMessage", discountResult.getErrorMessage());
                return buildDiscountsPage(sessionId, storeId, model);
            }
            discounts.add(discountResult.getValue());
        }
        Result result = null;
        try {
            result = apiService.addDiscount(sessionId, storeId, new MaxDiscount(discounts));
        } catch (DataError e) {
            model.addAttribute("errorMessage", e.getMessage());
            return buildDiscountsPage(sessionId, storeId, model);        }
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "max discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/addAddDiscountRule")
    public String addAddDiscountRule(@RequestParam("storeId") Integer storeId, @RequestParam("discountIds") List<Integer> discountIds, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Set<ProductDiscount> productDiscounts = new HashSet<>();
        Set<CategoryDiscount> categoryDiscounts = new HashSet<>();
        Set<StoreDiscount> storeDiscounts = new HashSet<>();
        for(int discountId : discountIds) {
            Result<IDiscount> discountResult = apiService.getStoreDiscount(sessionId, storeId, discountId);
            if(discountResult.errorOccured()) {
                model.addAttribute("errorMessage", discountResult.getErrorMessage());
                return buildDiscountsPage(sessionId, storeId, model);
            }
            IDiscount discount = discountResult.getValue();
            if(discount instanceof ProductDiscount)
                productDiscounts.add((ProductDiscount)discount);
            else if(discount instanceof CategoryDiscount)
                categoryDiscounts.add((CategoryDiscount)discount);
            else if(discount instanceof StoreDiscount)
                storeDiscounts.add((StoreDiscount)discount);
            else {
                model.addAttribute("errorMessage", "Only product/category/store discounts are allowed for add discount rule!");
                return buildDiscountsPage(sessionId, storeId, model);
            }
        }
        Result result = apiService.addDiscount(sessionId, storeId, new AddDiscount(storeDiscounts, categoryDiscounts, productDiscounts));
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            return buildDiscountsPage(sessionId, storeId, model);
        }
        model.addAttribute("successMessage", "add discount rule added successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/removeDiscount")
    public String removeDiscount(@RequestParam("storeId") Integer storeId, @RequestParam("discountId") int discountId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.removeDiscount(sessionId, storeId, discountId);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        model.addAttribute("successMessage", "discount removed successfully!");
        return buildDiscountsPage(sessionId, storeId, model);
    }

    @PostMapping("/removePurchaseRule")
    public String removePurchaseRule(@RequestParam("storeId") Integer storeId, @RequestParam("purchaseRuleId") int purchaseRuleId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.removePurchaseRule(sessionId, storeId, purchaseRuleId);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "purchase rule removed successfully!");
        return buildPurchaseRulesPage(sessionId, storeId, model);
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam("fullName") String fullName, @RequestParam("street") String street, @RequestParam("city") String city, @RequestParam("country") String country, @RequestParam("zip") String zip, @RequestParam("phoneNumber") String phoneNumber, @RequestParam("card_owner") String card_owner, @RequestParam("card_number") String card_number, @RequestParam("expiry_date") String expiry_date, @RequestParam("cvv") String cvv, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        YearMonth ym = YearMonth.parse(expiry_date, fmt);
        LocalDate expirationDate = ym.atDay(1); // choose whatever day you want
        Result result = apiService.pay(sessionId, new PaymentDetails(card_owner, card_number, expirationDate, cvv),  new AddressRecord(fullName, street, city, country, zip, phoneNumber));
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
            buildCartPage(model, sessionId);
            return "cart";
        }
        model.addAttribute("successMessage", "checkout done successfully!");
        return showHomePage(model, session, response, request);
    }

    @PostMapping("/removeCartProduct")
    public String removeCartProduct(@RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.removeProductFromCart(sessionId, storeId, productId, 1); // todo = Oran, need to change the quantity!!!!!!!!!!
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else {
            model.addAttribute("successMessage", "product removed from cart successfully!");
        }
        buildCartPage(model, sessionId);
        return "cart";
    }


    //Register
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("confirmpassword") String confirmpassword,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("birthday") String birthdayStr,
                           Model model, HttpSession session,
                           HttpServletResponse response, HttpServletRequest request) {
        // perform registration logic with the received form data
        String sessionId = getSessionId(session, response, request);
        initNavigationBar(model, sessionId);
        LocalDate birthday = LocalDate.parse(birthdayStr);
        if(!password.equals(confirmpassword)) {
            model.addAttribute("errorMessage", "Passwords don't match");
            return "register";
        }

        UserRecord userRecord = new UserRecord(username, email, phone, birthday);
        Result registerResult = apiService.register(sessionId, userRecord, password);

        if (registerResult.isOk()) {
            model.addAttribute("successMessage", "registration done successfully!");
            return "homepage";
        } else {
            model.addAttribute("errorMessage", registerResult.getErrorMessage());
            return "register";
        }
    }

    //Stores
    @PostMapping("/openStore")
    public String openStore(@RequestParam("name") String name,
                            @RequestParam("description") String description,
                            Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result<Boolean> isMemberSession = apiService.isMemberSession(sessionId);
        //todo if its not a member session, block the user from our website!
        StoreRecord storeRecord = new StoreRecord(name, description);
        Result registerResult = apiService.openStore(sessionId, storeRecord);

        if(registerResult.errorOccured()) {
            model.addAttribute("errorMessage", registerResult.getErrorMessage());
        }
        return showStoresPage(model, session, response, request);
    }

    //Store Actions
    @PostMapping("/reopenStore")
    public String reopenStore(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.reopenStore(sessionId, storeId);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        return buildStoreActionsPage(model, sessionId, storeId);
    }

    @PostMapping("/inactiveStore")
    public String closeStore(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.closeStore(sessionId, storeId);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        return buildStoreActionsPage(model, sessionId, storeId);
    }

    @PostMapping("/addNewProduct")
    public String addNewProduct(@RequestParam("storeId") Integer storeId, @RequestParam("productName") String productName, @RequestParam("productPrice") Double productPrice,  @RequestParam("productCategory") String productCategoryStr, @RequestParam("productQuantity") int productQuantity, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Category productCategory = Category.valueOf(productCategoryStr);
        ProductRecord productRecord = new ProductRecord(storeId, productName, productPrice, productCategory, productQuantity);
        Result addProductResult = apiService.addNewProduct(sessionId, productRecord);
        if(addProductResult.errorOccured()) {
            model.addAttribute("errorMessage", addProductResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "product added successfully!");
        return buildStorageManagementPage(sessionId, storeId, model);
    }

    @PostMapping("/editProduct")
    public String editProduct(@RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, @RequestParam("productName") String productName,  @RequestParam("productPrice") Double productPrice,  @RequestParam("productCategory") Category productCategory, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        ProductRecord productRecord = new ProductRecord(storeId, productId, productName, productPrice, productCategory);
        Result editProductResult = apiService.updateProduct(sessionId, productRecord);
        if(editProductResult.errorOccured()) {
            model.addAttribute("errorMessage", editProductResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "product edited successfully!");
        return buildStorageManagementPage(sessionId, storeId, model);
    }

    @PostMapping("/removeProduct")
    public String removeProduct(@RequestParam("storeId") Integer storeId, @RequestParam("productsId") List<Integer> productsId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        boolean success = true;
        for(int productId: productsId) {
            Result removeProductResult = apiService.removeProduct(sessionId, storeId, productId);
            if (removeProductResult.errorOccured()) {
                model.addAttribute("errorMessage", removeProductResult.getErrorMessage());
                success = false;
            }
        }
        if(success)
            model.addAttribute("successMessage", "product removed successfully!");
        return buildStorageManagementPage(sessionId, storeId, model);
    }

    @PostMapping("/editStoreManagerPermissions")
    public String editStoreManagerPermissions(@RequestParam("storeId") Integer storeId, @RequestParam("username") String username,
                                              @RequestParam(value = "permissions", required = false) String[] permissions,
                                              Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request ) {
        String sessionId = getSessionId(session, response, request);
        List<PermissionType> permissionTypeList = new ArrayList<>();
        if(permissions != null)
            for(String permission: permissions) {
                permissionTypeList.add(PermissionType.valueOf(permission));
            }
        Integer newPerms = PermissionType.collectionToBitmap(permissionTypeList);
        Result editPermissionsResult = apiService.modifyPermissionsFor(sessionId, username, storeId, newPerms);
        if(editPermissionsResult.errorOccured()) {
            model.addAttribute("errorMessage", editPermissionsResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store manager permission edited successfully!");
        return buildStoreManagersPage(sessionId, storeId, model);
    }

    @PostMapping("/removeStoreManager")
    public String removeStoreManager(@RequestParam("username") String username, @RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result removeStoreManagerResult = apiService.removeStoreManager(sessionId, username, storeId);
        if(removeStoreManagerResult.errorOccured()) {
            model.addAttribute("errorMessage", removeStoreManagerResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store manager removed successfully!");
        return buildStoreActionsPage(model, sessionId, storeId);
    }
    @PostMapping("/removeStoreOwner")
    public String removeStoreOwner(@RequestParam("username") String username, @RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result removeStoreOwnerResult = apiService.removeStoreOwner(sessionId, username, storeId);
        if(removeStoreOwnerResult.errorOccured()) {
            model.addAttribute("errorMessage", removeStoreOwnerResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store owner removed successfully!");
        return buildStoreOwnersPage(sessionId, storeId, model);
    }

    @PostMapping("/appointStoreOwner")
    public String appointStoreOwner(@RequestParam("username") String username, @RequestParam("contract") String contract, @RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result appointOwnerResult = apiService.publishMemberContract(sessionId, storeId, username, contract);
        if(appointOwnerResult.errorOccured()) {
            model.addAttribute("errorMessage", appointOwnerResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "contract made successfully!");
        return buildStoreOwnersPage(sessionId, storeId, model);
    }

    @PostMapping("/appointStoreManager")
    public String appointStoreManager(@RequestParam("username") String username, @RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result appointManagerResult = apiService.appointManager(sessionId, username, storeId);
        if(appointManagerResult.errorOccured()) {
            model.addAttribute("errorMessage", appointManagerResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store manager appointed successfully!");
        return buildStoreManagersPage(sessionId, storeId, model);
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("storeId") Integer storeId, @RequestParam("productId") Integer productId, @RequestParam("quantity") Integer quantity, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result addProductToCart = apiService.addProductToCart(sessionId, productId, storeId, quantity);
        if(addProductToCart.errorOccured()) {
            model.addAttribute("errorMessage", addProductToCart.getErrorMessage());
            return showProductPage(model, session, response, request, storeId, productId);
        }
        else
            model.addAttribute("successMessage", "product added to the cart successfully!");
        return showHomePage(model, session, response, request);
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result changePasswordResult = apiService.changePassword(sessionId, oldPassword, newPassword);
        if(changePasswordResult.errorOccured()) {
            model.addAttribute("errorMessage", changePasswordResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "Password changed successfully!");
        return showProfilePage(session, response, model, request);
    }


    @PostMapping("/closeStorePermanently")
    public String closeStorePermanently(@RequestParam("storeId") Integer storeId, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        Result result = apiService.closeStorePermanently(sessionId, storeId);
        if(result.errorOccured()) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "store closed successfully!");
        return showStoresPage(model, session, response, request);
    }

    @PostMapping("/addMemberAddress")
    public String addMemberAddress(@RequestParam("fullName") String fullName, @RequestParam("street") String street, @RequestParam("city") String city, @RequestParam("country") String country, @RequestParam("zip") String zip, @RequestParam("phoneNumber") String phoneNumber, Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String sessionId = getSessionId(session, response, request);
        AddressRecord addressData = new AddressRecord(fullName, street, city, country, zip, phoneNumber);
        Result<Integer> addMemberaddressResult = apiService.addMemberAddress(sessionId, addressData);
        if(addMemberaddressResult.errorOccured()) {
            model.addAttribute("errorMessage", addMemberaddressResult.getErrorMessage());
        }
        else
            model.addAttribute("successMessage", "Address added successfully!");
        // Retrieve the Referer header
        String referer = request.getHeader("Referer");

        // Check if the Referer is localhost:8080/profile
        if (referer != null && referer.contains("localhost:8080/profile")) {
            // Redirect the user to the profile page
            return showProfilePage(session, response, model, request);
        }

        // Check if the Referer is localhost:8080/cart
        if (referer != null && (referer.contains("localhost:8080/cart") || referer.contains("localhost:8080/addMemberAddress"))) {
            Result<Boolean> guestSessionResult = apiService.isGuestSession(sessionId);
            if(guestSessionResult.isOk() && guestSessionResult.getValue()) {
                List<AddressRecord> addressList = new ArrayList<>();
                addressList.add(new AddressRecord(fullName, street, city, country, zip, phoneNumber));
                model.addAttribute("addresses", addressList);
            }
            // Redirect the user to the cart page
            return showCartPage(model, session, response, request);
        }

        // Redirect the user to a default page if the Referer doesn't match any specific pages
        return showHomePage(model, session, response, request);
    }

    private String buildStoreActionsPage(Model model, String sessionId, Integer storeId) {
        Result<Integer> memberPermissions = apiService.getMemberPermissions(sessionId, storeId);
        Result<StoreRecord> storeRecordResult = apiService.getStoreInfo(sessionId, storeId);
        if(memberPermissions.errorOccured() || storeRecordResult.errorOccured()) {
            return showForbiddenPage();
        }
        initNavigationBar(model, sessionId);
        model.addAttribute("storeName", storeRecordResult.getValue().storeName());
        model.addAttribute("storeId", storeId);
        model.addAttribute("isActive", storeRecordResult.getValue().isActive());
        buildPermissions(model, memberPermissions.getValue());
        return "StoreActions";
    }

    private void buildPermissions(Model model, Integer memberPermission) {
        for(PermissionType permissionType : PermissionType.bitmapToSet(memberPermission)) {
            model.addAttribute(permissionType.name(), true);
        }
        
        for(PermissionType permissionType : PermissionType.values()) {
            if(!model.containsAttribute(permissionType.name()))
                model.addAttribute(permissionType.name(), false);
        }
    }
    private void buildMyStores(Model model, String sessionId) {
        initNavigationBar(model, sessionId);
        Result<List<StoreRecord>> myStores = apiService.getMyStores(sessionId);
        if(myStores.isOk()) {
            List<StoreRecord> storeRecords = myStores.getValue();
            model.addAttribute("stores", storeRecords);
        }
    }

    private void buildCartPage(Model model, String sessionId) {
        initNavigationBar(model, sessionId);
        List<ProductRecord> productRecordList = new ArrayList<>();
        Result<Map<Integer, Map<Integer, ProductRecord>>> cartProductsResult = apiService.getCartContent(sessionId);
        Result<Double> cartPriceResult = apiService.getCartPrice(sessionId);
        if(cartPriceResult.isOk()) {
            model.addAttribute("cartPrice", cartPriceResult.getValue());
        }
        if(cartProductsResult.isOk()) {
            Map<Integer, Map<Integer, ProductRecord>> cartProducts = cartProductsResult.getValue();
            for(int storeId : cartProducts.keySet()) {
                Map<Integer, ProductRecord> products = cartProducts.get(storeId);
                for(int productId : products.keySet()) {
                    ProductRecord product = products.get(productId);
                    productRecordList.add(product);
                }
            }
        }
        Result<UserRecord> memberAddressesResult = apiService.getMemberDetails(sessionId);
        if(memberAddressesResult.isOk()) {
            UserRecord userData = memberAddressesResult.getValue();
            List<AddressRecord> addresses = userData.addresses();
            model.addAttribute("addresses", addresses);
        }
        model.addAttribute("products", productRecordList);
        model.addAttribute("productsAmount", productRecordList.size());
    }

    private String buildMarketManagerPage(String sessionId, Model model) {
        initNavigationBar(model, sessionId);
        Result<Set<StoreRecord>> storesResult = apiService.getStores();
        Result<Integer> conntectedMembersResult = apiService.getAmountOfConnectedMembers(sessionId);
        Result<Integer> conntectedGuestsResult = apiService.getAmountOfConnectedGuests(sessionId);
        if(storesResult.errorOccured() || conntectedMembersResult.errorOccured() || conntectedGuestsResult.errorOccured()) {
            return showForbiddenPage();
        }
        model.addAttribute("storeRecords", storesResult.getValue());
        model.addAttribute("membersAmount", conntectedMembersResult.getValue());
        model.addAttribute("guestsAmount", conntectedGuestsResult.getValue());
        return "marketManager";
    }

    private String buildDiscountsPage(String sessionId, Integer storeId, Model model) {
        initNavigationBar(model, sessionId);
        Result<List<IDiscount>> storeDiscountsResult = apiService.getStoreDiscounts(sessionId, storeId);
        Result<StoreRecord> storeInfoResult = apiService.getStoreInfo(sessionId, storeId);
        if(storeInfoResult.errorOccured() || storeDiscountsResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeName", storeInfoResult.getValue().storeName());
        model.addAttribute("discounts", storeDiscountsResult.getValue());
        model.addAttribute("storeId", storeId);
        return "Discounts";
    }

    private String buildPurchaseRulesPage(String sessionId, Integer storeId, Model model) {
        initNavigationBar(model, sessionId);
        Result<List<PurchaseRule>> storePurchaseRulesResult = apiService.getStorePurchaseRules(sessionId, storeId);
        Result<StoreRecord> storeInfoResult = apiService.getStoreInfo(sessionId, storeId);
        if(storeInfoResult.errorOccured() || storePurchaseRulesResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeName", storeInfoResult.getValue().storeName());
        model.addAttribute("purchaseRules", storePurchaseRulesResult.getValue());
        model.addAttribute("storeId", storeId);
        return "purchaseRules";
    }

    private String buildStorageManagementPage(String sessionId, Integer storeId, Model model) {
        initNavigationBar(model, sessionId);
        Result<StoreRecord> storeInfoResult = apiService.getStoreInfo(sessionId, storeId);
        Result<Integer> memberPermissionResult = apiService.getMemberPermissions(sessionId, storeId);
        if(storeInfoResult.errorOccured() || memberPermissionResult.errorOccured())
            return showForbiddenPage();
        model.addAttribute("storeId", storeId);
        model.addAttribute("storeName", storeInfoResult.getValue().storeName());
        Result<List<ProductRecord>> productsResult = apiService.getStoreProducts(sessionId, storeId);
        if(productsResult.errorOccured()) {
            model.addAttribute("errorMessage", productsResult.getErrorMessage());
        } else {
            List<ProductRecord> products = productsResult.getValue();
            model.addAttribute("productRecords", products);
        }
        return "storageManagement";
    }

    private String buildStoreManagersPage(String sessionId, Integer storeId, Model model) {
        Result<Integer> memberPermissions = apiService.getMemberPermissions(sessionId, storeId);
        if(memberPermissions.errorOccured()) {
            return showForbiddenPage();
        }
        initNavigationBar(model, sessionId);
        buildPermissions(model, memberPermissions.getValue());

        Result<Map<String,Integer>> managersResult = apiService.getManagersPermissions(sessionId, storeId);
        if(managersResult.errorOccured()) {
            model.addAttribute("errorMessage", managersResult.getErrorMessage());
        } else {
            Map<String, Integer> managers = managersResult.getValue();
            Map<String, Set<PermissionType>> managersPermission = new HashMap<>();
            for(String userName : managers.keySet()) {
                managersPermission.put(userName, PermissionType.bitmapToSet(managers.get(userName)));
            }
            model.addAttribute("managers", managersPermission);
        }
        model.addAttribute("storeId", storeId);
        Result<StoreRecord> storeInfoResult = apiService.getStoreInfo(sessionId, storeId);
        if(storeInfoResult.isOk())
            model.addAttribute("storeName", storeInfoResult.getValue().storeName());
        return "storeManagers";
    }
}