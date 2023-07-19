package Domain.Services.NotificationService;

import util.Exceptions.NonExistentData;

import java.util.List;

public interface INotificationService {

  /**
   * Subscribes a given observer to receive notifications of the given user.
   *
   * @param recipient A member name that may receive notifications.
   * @param observer  An observer to activate once the member
   */
  void subscribe(String recipient, NotificationObserver observer);

  /**
   * Unsubscribes a user from receiving notifications.
   *
   * @param recipient        A member name that may receive notifications.
   * @param observerToRemove An already subscribed observer that should be removed from subscription.
   */
  void unsubscribe(String recipient, NotificationObserver observerToRemove);

  /**
   * Notifies a user with a specific message.
   *
   * @param msg the message to be sent
   * @param recipient the username of the recipient user
   * @param sender the username of the sender user
   * @return true if notification succeeded for at least one observer
   */
  boolean notify(String msg, String recipient, String sender);

  /**
   * Sends a message to a list of users.
   *
   * @param recipients the list of usernames to send the message to
   * @param message the content of the message
   * @param sender the username of the message sender
   */
  void broadcastMessage(List<String> recipients, String message, String sender);

  /**
   * return all notification that user hold or empty list.
   * @param userName
   * @return number of notifications left to send.
   */
  int sendAllMemberNotifications(String userName);

  public enum EmailAddress {
    Regular("eligaga@gmail.com", "passExample");

    private final String address;
    private final String password;

    EmailAddress(String address, String password) {
      this.address = address;
      this.password = password;
    }

    public String getAddress() { return address; }

    public String getPassword() { return password; }

    @Override
    public String toString() {
      return "EmailAddress{"
          + "address='" + address + '\'' + ", password='" + password + '\'' +
          '}';
    }
  }
}
