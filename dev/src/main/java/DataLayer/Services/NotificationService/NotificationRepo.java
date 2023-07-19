package DataLayer.Services.NotificationService;

import DataLayer.DbConfig;
import DataLayer.Services.NotificationService.ORM.DataNotification;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Records.NotificationRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NotificationRepo implements INotificationRepo {
    private final Map<String, Queue<DataNotification>> notifications; // <username, waitingNotifications>
    private final Map<String, ReadWriteLock> locks; // <username, lock>

    public NotificationRepo() {
        notifications = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();
    }

    @Override
    public void add(NotificationRecord notification) {
        pullDataIfAbsent(notification.recipient());
        notifications.get(notification.recipient()).add(new DataNotification(notification));
    }

    @Override
    public void add(Queue<NotificationRecord> notifications) {
        while (!notifications.isEmpty())
            add(notifications.remove());
    }

    @Override
    public Queue<NotificationRecord> getNotificationsFor(String userName) {
        pullDataIfAbsent(userName);
        Queue<NotificationRecord> toReturn = new PriorityQueue<>();
        Queue<DataNotification> toGet = notifications.get(userName);
        while (!toGet.isEmpty())
            toReturn.add(toGet.remove().remove()); //removing from queue and DB
        return toReturn;
    }

    @Override
    public void lockFor(String userName) {
        locks.putIfAbsent(userName, new ReentrantReadWriteLock());

        locks.get(userName).writeLock().lock();
    }

    @Override
    public void unlockFor(String userName) {
        ReadWriteLock lock =
                !notifications.containsKey(userName) || notifications.get(userName).isEmpty() ?
                        locks.remove(userName) :
                        locks.get(userName);

        lock.writeLock().unlock();
    }

    private void pullDataIfAbsent(String username) {
        if (!DbConfig.shouldPersist())
            notifications.putIfAbsent(username, new PriorityQueue<>());

        if (notifications.containsKey(username)) {
            return;
        }
        try (Session session = DbConfig.getSessionFactory().openSession()) {
            Query<DataNotification> query = session.createQuery("From DataNotification n WHERE n.key.member.username = :username", DataNotification.class);
            query.setParameter("username", username);
            notifications.put(
                    username,
                    new PriorityQueue<>(
                            query.list().stream()
                                    .sorted(Comparator.comparing(DataNotification::getSending_time))
                                    .toList()
                    )
            );
        }
    }
}
