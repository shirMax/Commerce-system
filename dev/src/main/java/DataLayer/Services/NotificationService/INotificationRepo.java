package DataLayer.Services.NotificationService;

import util.Records.NotificationRecord;

import java.util.Queue;

public interface INotificationRepo {
    /**
     * Adds notification to be saved for when the recipient logs in.
     *
     * @param notification Notification to be saved.
     */
    void add(NotificationRecord notification);

    /**
     * Adds multiple notifications at once.
     * @param notifications notifications to add.
     */
    void add(Queue<NotificationRecord> notifications);

    /**
     * @param userName member to get their waiting notifications
     * @return queue of notifications waiting to be sent.
     */
    Queue<NotificationRecord> getNotificationsFor(String userName);

    /**
     * Locks editing of notification queue of given username
     * @param userName member who's notification queue needs to be locked for editing.
     */
    void lockFor(String userName);


    /**
     * Allows editing of notification queue of given username
     * @param userName member who's notification queue needs to be released for editing.
     */
    void unlockFor(String userName);
}
