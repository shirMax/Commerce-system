package DataLayer.Services.NotificationService.ORM;

import DataLayer.DbConfig;
import DataLayer.ORM.DataPermission;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import util.Records.NotificationRecord;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
public class DataNotification implements Comparable<DataNotification>{
    @EmbeddedId
    private DataNotificationKey key;
    private String sender;
    private String recipient;
    @Column(columnDefinition = "timestamp(9)")
    private LocalDateTime sending_time;
    private String msg;

    public DataNotification() {
    }

    public DataNotification(NotificationRecord notification) {
        key = new DataNotificationKey(notification.recipient());
        sender = notification.sender();
        recipient = notification.recipient();
        sending_time = notification.sendingTime();
        msg = notification.message();
        if (DbConfig.shouldPersist()) {
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(this);
                session.getTransaction().commit();
            }
        }
    }

    public DataNotificationKey getKey() {
        return key;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public LocalDateTime getSending_time() {
        return sending_time;
    }

    public String getMsg() {
        return msg;
    }

    public Object getId(){
        return key;
    }

    public NotificationRecord getAsRecord() {
        return new NotificationRecord(sender, recipient, sending_time, msg);
    }

    public NotificationRecord remove() {
        if (!DbConfig.shouldPersist()) return getAsRecord();

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            session.beginTransaction();
            DataNotification toRemove = session.get(DataNotification.class, getId());
            if (toRemove == null) return getAsRecord();
            session.remove(toRemove);
            session.getTransaction().commit();
        }
        return getAsRecord();
    }

    @Override
    public int compareTo(DataNotification o) {
        return getSending_time().compareTo(o.getSending_time());
    }
}
