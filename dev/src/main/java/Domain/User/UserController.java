package Domain.User;

import DataLayer.User.*;
import Domain.IMarket;
import Domain.Services.NotificationService.NotificationObserver;
import FELayer.SystemManagerWebSocketHandler;
import util.Records.Transaction;
import Domain.MarketImpl;
import Domain.MarketLogger;
import Domain.Services.NotificationService.INotificationService;
import Domain.Store.IStoreController;
import Domain.Store.Offer;
import util.Enums.ErrorStatus;
import util.Exceptions.*;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.UserRecords.UserRecord;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserController implements IUserController {
  private final ISystemManagerRepo systemManagerRepo;
  private final IUserRepo userRepo;
  private final ISessionRepo sessionRepo;
  private final IRegistrationValidator registrationValidator;
  private final IMarket market;

  private INotificationService notificationService;

    public UserController(ISystemManagerRepo systemManagerRepo, IUserRepo userRepo, ISessionRepo sessionRepo, IMarket market) {
        this.systemManagerRepo = systemManagerRepo;
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.registrationValidator = new RegistrationValidator();
        this.systemManagerRepo.initSystemManager(new SystemManager("admin", "admin"));
        this.market = market;
    }

  @Override
  public void updateNotificationService (INotificationService notificationService){
    this.notificationService = notificationService;
  }

  @Override
  public void openSession(String sessionId) throws SessionError {
    sessionRepo.addSession(sessionId, new Guest(sessionId));
    SystemManagerWebSocketHandler.refreshSystemManagerWebsite();
  }

  @Override
  public void closeSession(String sessionId) throws SessionError {
    validateSessionExists(sessionId);
    if (!sessionRepo.isGuestSession(sessionId)) {
      logout(sessionId);
    }
    sessionRepo.removeSession(sessionId);
    SystemManagerWebSocketHandler.refreshSystemManagerWebsite();
  }

  @Override
  public IUser getUser(String sessionId) throws SessionError {
    if (sessionRepo.isGuestSession(sessionId)) {
      return sessionRepo.getGuest(sessionId);
    }
    if (sessionRepo.isMemberSession(sessionId)) {
      String userName = sessionRepo.getMemberUserName(sessionId);
      try {
        return userRepo.getMember(userName);
      } catch (NonExistentData e) {
        MarketLogger.logError("UserController", "getUser", "Got member name from session ID but member doesn't exist. This should not happen!", String.valueOf(sessionId));
      }
    }
    throw new SessionError("Session is not existing!", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
  }

  @Override
  public Member getMember(String userName) throws NonExistentData {
      return userRepo.getMember(userName);
  }

  @Override
  public void login(String sessionId, String userName, String password) throws SessionError {
    Guest guest = sessionRepo.getGuest(sessionId);
    if (userRepo.isMemberExists(userName)) {
      try {
        Member member = userRepo.getMember(userName);
        member.login(sessionId, password);
        guest.removeUserCart();
        sessionRepo.removeSession(guest.getSessionId());
        sessionRepo.addSession(sessionId, member);
      } catch (NonExistentData | NoSuchAlgorithmException e) {
        MarketLogger.logError("UserController", "Login", "userRepo returned that member is exists but couldn't return him. This should not happen!", String.valueOf(sessionId), userName);// don't! add password to log
      }
    } else if (systemManagerRepo.isSystemManagerExists(userName)) {
      try {
        ISystemManager systemManager = systemManagerRepo.getSystemManager(userName);
        systemManager.login(password);
        guest.removeUserCart();
        sessionRepo.removeSession(guest.getSessionId());
        sessionRepo.addSession(sessionId, systemManager);
      } catch (NonExistentData e) {
        MarketLogger.logError("UserController", "Login", "systemManagerRepo returned that system manager is exists but couldn't return him. This should not happen!", String.valueOf(sessionId), userName);
      }

    } else {
      throw new SessionError("user name doesn't exists!", ErrorStatus.USERNAME_PASSWORD_MISMATCH);
    }
    SystemManagerWebSocketHandler.refreshSystemManagerWebsite();
  }

  @Override
  public void subscribeToNotifications(String sessionID, NotificationObserver observer) throws SessionError {
    String username = sessionRepo.getMemberUserName(sessionID);
    notificationService.subscribe(username, observer);
  }

  @Override
  public void unsubscribeFromNotifications(String sessionID, NotificationObserver observerToRemove) throws SessionError {
    String username = sessionRepo.getMemberUserName(sessionID);
    notificationService.unsubscribe(username, observerToRemove);
  }

  @Override
  public void logout(String sessionId) throws SessionError {
    validateSessionExists(sessionId);
    if (sessionRepo.isMemberSession(sessionId)) {
      String userName = sessionRepo.getMemberUserName(sessionId);
      try {
        Member member = userRepo.getMember(userName);
        member.logout();
      } catch (NonExistentData e) {
        MarketLogger.logError("UserController", "logout", "Got member name from session ID but member doesn't exist. This should not happen!", String.valueOf(sessionId));
        return;
      }

    } else if (sessionRepo.isSystemManagerSession(sessionId)) {
      String userName = sessionRepo.getSystemManagerUserName(sessionId);
      try {
        ISystemManager systemManager = systemManagerRepo.getSystemManager(userName);
        systemManager.logout();
      } catch (NonExistentData e) {
        MarketLogger.logError("UserController", "logout", "Got system manager username from session ID but system manager doesn't exist. This should not happen!", String.valueOf(sessionId));
      }
    } else {
      throw new SessionError("guest can't logout!", ErrorStatus.GUEST_SESSION);
    }
    sessionRepo.removeSession(sessionId);
    sessionRepo.addSession(sessionId, new Guest(sessionId));
    SystemManagerWebSocketHandler.refreshSystemManagerWebsite();
  }

  @Override
  public boolean isMemberIsSystemManager(String sessionId){
    return sessionRepo.isSystemManagerSession(sessionId);
  }

  @Override
  public void register(String sessionId, UserRecord userData, String password) throws DataError, SessionError, DataExistentError {
    String username = userData.username();
    validateSessionExists(sessionId);
    registrationValidator.validateUserName(username);
    registrationValidator.validatePassword(password);
    registrationValidator.validateEmail(userData.email());
    registrationValidator.validatePhoneNumber(userData.phoneNumber());
    if(systemManagerRepo.isSystemManagerExists(username) || userRepo.isMemberExists(username)) {
      MarketLogger.logError("UserController", "register", "username is already exists!", sessionId, userData, password);
      throw new DataExistentError("username is already exists!", ErrorStatus.USERNAME_EXISTS);
    }
    userRepo.addMember("", userData, password);
  }
  private void validateSessionExists(String sessionId) throws SessionError {
    if(!sessionRepo.isSessionExists(sessionId)) {
      MarketLogger.logError("UserController", "validateSessionExist", sessionId);
      throw new SessionError("Session is not exists", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
    }
  }
  @Override
  public void changePassword(String sessionId, String oldPassword,
                             String newPassword) throws DataError, SessionError {
    if (sessionRepo.isMemberSession(sessionId)) {
      String userName = sessionRepo.getMemberUserName(sessionId);
      try {
        Member member = userRepo.getMember(userName);
        member.changePassword(oldPassword, newPassword);
      } catch (NonExistentData | NoSuchAlgorithmException e) {
        MarketLogger.logError("UserController", "changePassword", "Got member username from session ID but member doesn't exist. This should not happen!", String.valueOf(sessionId)); // don't! add password to log
      }
    }
    else if (sessionRepo.isSystemManagerSession(sessionId)) {
      String userName = sessionRepo.getSystemManagerUserName(sessionId);
      try {
        ISystemManager systemManager = systemManagerRepo.getSystemManager(userName);
        systemManager.changePassword(oldPassword, newPassword);
      } catch (NonExistentData e) {
        MarketLogger.logError("UserController", "changePassword", "Got system manager username from session ID but member doesn't exist. This should not happen!", String.valueOf(sessionId)); // don't! add password to log
      }
    }
  }

  @Override
  public void sendMessage(String msg, String receiverUsername, String sessionIdSender) throws SessionError, NonExistentData {
    String senderUsername = sessionRepo.getMemberUserName(sessionIdSender);
    Member sender = getMember(senderUsername);
    Member receiver = userRepo.getMember(receiverUsername);
    if(sessionRepo.isMemberSession(sessionIdSender)) {
      notificationService.notify(msg, receiverUsername,  senderUsername);
      return;
    }
    MarketLogger.logError("UserContoller", "sendMessage", "Guest users cant send messages", msg, receiverUsername, sessionIdSender);
    throw new IllegalArgumentException("Guest users cant send messages");
  }

  @Override
  public IUserCart getUserCart(String sessionId) throws SessionError {
    IUser user = getUser(sessionId);
    IStoreController storeController = market.getStoreController();
    IUserCart cart = user.getUserCart();
    storeController.syncCart(cart);
    return cart;
  }

  @Override
  public void
  addProductsToStoreBasket(String sessionId, int storeId,
                           Map<Integer, ProductRecord> products) throws SessionError, NonExistentData, DataError {
    MarketImpl.getInstance().checkProductsExist(storeId, products);
    IUser user = getUser(sessionId);
    user.addProductsToStoreBasket(storeId, products.values().stream().toList());
  }

  @Override
  public void removeProductFromStoreBasket(String sessionId, int storeId,
                                           int productId, int quantity) throws SessionError, NonExistentData {
    IUser user = getUser(sessionId);
    user.removeProductFromStoreBasket(storeId, productId, quantity);
  }

  @Override
  public void updateProductQuantityInStoreBasket(String sessionId, int storeId,
                                                 int productId, int quantity) throws SessionError, NonExistentData {
    IUser user = getUser(sessionId);
    user.updateProductQuantityInStoreBasket(storeId, productId, quantity);
  }

  @Override
  public boolean isUserIsSystemManager(String sessionId) {
    return sessionRepo.isSystemManagerSession(sessionId);
  }

  @Override
  public boolean isUserIsMember(String sessionId) {
    return sessionRepo.isMemberSession(sessionId);
  }

  @Override
  public String getMemberUserName(String sessionId) throws SessionError {
    return sessionRepo.getMemberUserName(sessionId);
  }

  @Override
  public boolean isSessionExists(String sessionId) {
    return sessionRepo.isSessionExists(sessionId);
  }

  @Override
  public void addRoleToMember(String userName, int storeId) throws DataExistentError, NonExistentData {
    Member member = getMember(userName);
    member.addRole(storeId);
  }

  @Override
  public List<Integer> getMemberStores(String sessionId) throws SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
      Member member = null;
      try {
          member = getMember(userName);
      } catch (NonExistentData ignored) {
      }
      assert member != null;
      return member.getMemberStores();
  }

  @Override
  public void removeRoleFromMember(String userName, int storeId) throws NonExistentData {
      Member member = getMember(userName);
      member.removeRole(storeId);
  }

  @Override
  public void createSystemManager(String sessionId, String userName,
                                  String password) throws DataError, DataExistentError {
    registrationValidator.validateUserName(userName);
    registrationValidator.validatePassword(password);
    if(userRepo.isMemberExists(userName)) {
      throw new DataExistentError("username is already exists!", ErrorStatus.USERNAME_EXISTS);
    }
    systemManagerRepo.addSystemManager(userName, password);
  }

  @Override
  public void changeSystemManagerPassword(String sessionId, String oldPassword,
                                          String newPassword) throws DataError, SessionError {
    String userName = sessionRepo.getSystemManagerUserName(sessionId);
    try {
      ISystemManager systemManager = systemManagerRepo.getSystemManager(userName);
      systemManager.changePassword(oldPassword, newPassword);
    } catch (NonExistentData e) {
      MarketLogger.logError("UserController", "changeSystemManagerPassword", "Got system manager username from session ID but system manager doesn't exist. This should not happen!", String.valueOf(sessionId));// don't! add password to log
    }
  }

  @Override
  public MemberAddress getMemberPrimaryAddress(String sessionId) throws NonExistentData, SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
    Member member = getMember(userName);
    return member.getPrimaryAddress();
  }

  @Override
  public void setPrimaryAddress(String sessionId, int addressId) throws NonExistentData, SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
    Member member = getMember(userName);
    member.setPrimaryAddress(addressId);
  }

  @Override
  public int addAddress(String sessionId, AddressRecord addressData) throws SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
    Member member = null;
    try {
      member = getMember(userName);
    } catch (NonExistentData ignored) {
    }
    assert member != null;
    return member.addAddress(addressData);
  }

  @Override
  public void removeAddress(String sessionId, int addressId) throws SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
    Member member = null;
    try {
    member = getMember(userName);
  } catch (NonExistentData ignored) {
  }
    assert member != null;
    member.removeAddress(addressId);
  }

  @Override
  public void updateAddress(String sessionId, int addressId, AddressRecord addressData) throws NonExistentData, SessionError {
    String userName = sessionRepo.getMemberUserName(sessionId);
    Member member = getMember(userName);
    MemberAddress address = member.getAddress(addressId);
    address.update(addressData);
  }

  @Override
  public void removeUserCart(String sessionId) throws SessionError {
    IUser user = getUser(sessionId);
    user.removeUserCart();
  }

  private void validateSessionManager(String managerSessionId) throws SessionError {
    if(!sessionRepo.isSystemManagerSession(managerSessionId)){
      throw new SessionError("Session Manager is not exists", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
    }
  }

  @Override
  public void removeMember(String managerSessionId, String userName) throws SessionError, PermissionError, NonExistentData {
      validateSessionManager(managerSessionId);
      if (!userRepo.isMemberExists(userName)) {
          MarketLogger.logError("UserController", "removeMember",
                  "user name doesnt exist in the system", managerSessionId, userName);
          throw new NonExistentData("user name doesnt exist in the system", ErrorStatus.USERNAME_EXISTS);
      }
      Member member = userRepo.getMember(userName);
      if (!member.getMemberStores().isEmpty())
          throw new PermissionError(
                  "user name have a role and cant be removed",
                  ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
          );

      if (member.isLoggedIn())
          member.logout();
      userRepo.removeMember(userName);
  }

  @Override
  public boolean isGuestSession(String sessionId) {
    return sessionRepo.isGuestSession(sessionId);
  }

  @Override
  public boolean isMemberSession(String sessionId) {
    return sessionRepo.isMemberSession(sessionId);
  }

  @Override
  public boolean isSystemManagerSession(String sessionId) {
    return sessionRepo.isSystemManagerSession(sessionId);
  }

  @Override
  public boolean isMemberExists(String username) {
      try {
        getMember(username);
        return true;
      } catch (NonExistentData e) {
        return false;
      }
  }

  @Override
  public Integer getAmountOfConnectedMembers(String sessionId) throws SessionError {
    validateSessionManager(sessionId);
    return sessionRepo.getAmountOfConnectedMembers();
  }

  @Override
  public Integer getAmountOfConnectedGuests(String sessionId) throws SessionError {
    validateSessionManager(sessionId);
    return sessionRepo.getAmountOfConnectedGuests();
  }

  @Override
  public UserRecord getMemberDetails(String sessionId) throws NonExistentData, SessionError {
    return new UserRecord(userRepo.getMember(sessionRepo.getMemberUserName(sessionId)));
  }


  @Override
  public List<Transaction> getUserTransactions(String sessionId, LocalDateTime optionalStart, LocalDateTime optionalEnd) throws SessionError {
    String userName = getMemberUserName(sessionId);
    return MarketImpl.getInstance().getUserTransactions(userName, optionalStart, optionalEnd);
  }

  @Override
  public Map<Integer, Transaction> getAllTransactions(String sessionId) throws SessionError {
    sessionRepo.getSystemManagerUserName(sessionId);
    return MarketImpl.getInstance().getTransactions();
  }

  @Override
  public Map<Integer, Offer> getMemberOffers(String sessionID) throws NonExistentData, SessionError {
      String username = getMemberUserName(sessionID);
    return getMember(username).getOffers();
  }

  @Override
  public void getAllPendingMessage(String sessionId) throws SessionError {
    String userName = getMemberUserName(sessionId);
    notificationService.sendAllMemberNotifications(userName);
  }

  @Override
  public Set<String> getUsers(String session) throws PermissionError {
    if (!isSystemManagerSession(session))
    {
      throw new PermissionError("session isn't a system manager", ErrorStatus.NO_MANAGER_PERMISSION);
    }
    else {
      return userRepo.getAllMembers();
    }
  }

    @Override
    public Set<String> getLoggedUsers(String session) throws PermissionError {
        if (!isSystemManagerSession(session))
            throw new PermissionError("session isn't a system manager", ErrorStatus.NO_MANAGER_PERMISSION);
        return sessionRepo.getAllLoggedMembers();
    }

    @Override
    public Set<String> getDisconnectedUsers(String session) throws PermissionError {
        if (!isSystemManagerSession(session))
            throw new PermissionError("session isn't a system manager", ErrorStatus.NO_MANAGER_PERMISSION);
        Set<String> loggedUsers = sessionRepo.getAllLoggedMembers();
        return userRepo.getAllMembers()
                .stream()
                .filter(u -> !loggedUsers.contains(u))
                .collect(Collectors.toSet());
    }

}
