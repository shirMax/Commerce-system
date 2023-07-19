package Domain.Services.NotificationService;

import DataLayer.Services.NotificationService.INotificationRepo;
import DataLayer.Services.NotificationService.NotificationRepo;
import Domain.MarketLogger;
import Domain.User.IUserController;
import org.checkerframework.checker.nullness.qual.NonNull;
import util.Records.NotificationRecord;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NotificationService implements INotificationService{

  private IUserController userController;
  private final INotificationRepo notificationRepo;

  private final Map<String, List<NotificationObserver>> observers; // <recipient, observers>
  private final Map<String, ReadWriteLock> observersLocks;

  public NotificationService(IUserController userController){
    notificationRepo = new NotificationRepo();
    this.userController = userController;
    this.observers = new ConcurrentHashMap<>();
    observersLocks = new ConcurrentHashMap<>();
  }

  public void updateUserController (@NonNull IUserController userController){
    this.userController = userController;
  }

  public void subscribe(@NonNull String recipient,@NonNull NotificationObserver observer){
    // Synchronize
    observersLocks.putIfAbsent(recipient, new ReentrantReadWriteLock());
    ReadWriteLock lock = observersLocks.get(recipient);
    lock.writeLock().lock();

    // subscribe
    observers.putIfAbsent(recipient, new ArrayList<>());
    observers.get(recipient).add(observer);

    // Sync end
    lock.writeLock().unlock();
  }

  public void unsubscribe(String recipient, NotificationObserver observerToRemove){
    // Synchronize
    observersLocks.putIfAbsent(recipient, new ReentrantReadWriteLock());
    ReadWriteLock lock = observersLocks.get(recipient);
    lock.writeLock().lock();

    // unsubscribe
    if (observers.containsKey(recipient)) {
      List<NotificationObserver> observerList = observers.get(recipient);
      observerList.remove(observerToRemove);
      if (observers.get(recipient).isEmpty())
        observers.remove(recipient);
    }

    // Sync end
    lock.writeLock().unlock();
  }

  public boolean notify(String msg, String recipient, String sender){
    NotificationRecord notification = new NotificationRecord(sender, recipient, LocalDateTime.now(), msg);
    boolean success = false; // if notification sent successfully to at least one observer

    // Synchronize
    observersLocks.putIfAbsent(recipient, new ReentrantReadWriteLock());
    ReadWriteLock lock = observersLocks.get(recipient);
    lock.readLock().lock();

    // notify
    if (observers.containsKey(recipient))
      for (NotificationObserver observer : observers.get(recipient))
        success |= observer.notify(notification);

    // Sync end
    lock.readLock().unlock();

    // failed sending will store the notification for later
    if (!success) {
      notificationRepo.lockFor(recipient);
      notificationRepo.add(notification);
      notificationRepo.unlockFor(recipient);
    }
    return success;
  }


  public void broadcastMessage(List<String> recipients, String message, String sender){
    for (String recipient: recipients) {
      notify(message, recipient, sender);
    }
  }

  @Override
  public int sendAllMemberNotifications(String userName){
    if (!userController.isMemberExists(userName)) {
      MarketLogger.logError("NotificationService", "getAllMemberNotifications", "user not subscribe to service", userName);
      throw new RuntimeException("user not subscribe to service");
    }
    notificationRepo.lockFor(userName);
    Queue<NotificationRecord> notifications = notificationRepo.getNotificationsFor(userName);
    int notificationsSent = 0;
    while (!notifications.isEmpty()) {
      NotificationRecord notification = notifications.remove();
      try {
        notificationsSent = notify(notification.message(), userName, notification.sender()) ? notificationsSent : notificationsSent + 1;
      } catch (RuntimeException e) {
        notificationRepo.add(notifications); // saving all unsent notifications
        notificationRepo.unlockFor(userName);
        throw e;
      }
    }
    notificationRepo.unlockFor(userName);
    return notificationsSent;
  }

  public void sendEmail(String toEmailAddress, String fromEmailAddress,
                        String password, String subject, String bodyText)
      throws MessagingException {
    Session session = createSession(fromEmailAddress, password);
    MimeMessage email = createEmail(session, toEmailAddress, fromEmailAddress,
                                    subject, bodyText);
    Transport.send(email);
  }

  /**
   * Create a MimeMessage using the parameters provided.
   *
   * @param toEmailAddress   email address of the receiver
   * @param fromEmailAddress email address of the sender, the mailbox account
   * @param subject          subject of the email
   * @param bodyText         body text of the email
   * @return the MimeMessage to be used to send email
   * @throws MessagingException - if a wrongly formatted address is encountered.
   */
  private MimeMessage createEmail(Session session, String toEmailAddress,
                                  String fromEmailAddress, String subject,
                                  String bodyText) throws MessagingException {
    MimeMessage email = new MimeMessage(session);
    email.setFrom(fromEmailAddress);
    email.addRecipients(Message.RecipientType.TO, toEmailAddress);
    email.setSubject(subject);
    email.setText(bodyText);
    return email;
  }

  private Session createSession(String fromEmailAddress, String password) {
    String host = "smtp.gmail.com";
    Properties properties = System.getProperties();
    properties.put("mail.smtp.host", host);
    properties.put("mail.smtp.port", "465");
    properties.put("mail.smtp.ssl.enable", "true");
    properties.put("mail.smtp.auth", "true");
    Session session =
        Session.getInstance(properties, new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(fromEmailAddress, password);
          }
        });
    session.setDebug(false);
    return session;
  }
}
