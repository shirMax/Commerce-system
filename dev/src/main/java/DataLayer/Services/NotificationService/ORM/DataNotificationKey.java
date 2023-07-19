package DataLayer.Services.NotificationService.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Embeddable
public class DataNotificationKey implements Serializable {

    private static final Map<String, Integer> MEMBER_ID_COUNTERS = new ConcurrentHashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private DataMember member;

    @Column(name = "notification_id")
    private Integer id;

    public DataNotificationKey() {}

    public DataNotificationKey(DataMember member){
        this.member = member;
        String username = member.getUsername();
        synchronized (MEMBER_ID_COUNTERS){
            if (!MEMBER_ID_COUNTERS.containsKey(username)) {
                if (!DbConfig.shouldPersist())
                    MEMBER_ID_COUNTERS.put(username, 1);
                else
                    try (Session session = DbConfig.getSessionFactory().openSession()) {
                        Query<Integer> query = session.createQuery(
                                "SELECT MAX(n.key.id) FROM DataNotification n WHERE n.key.member.username = :username",
                                Integer.class
                        );
                        query.setParameter("username", username);
                        Integer maxID = query.uniqueResult();
                        MEMBER_ID_COUNTERS.put(username, maxID == null ? 1 : maxID + 1);
                    }
            }
            this.id = MEMBER_ID_COUNTERS.get(username);
            MEMBER_ID_COUNTERS.put(username, id + 1);
        }
    }

    public DataNotificationKey(String username) {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            member = session.get(DataMember.class, username);
        }
        synchronized (MEMBER_ID_COUNTERS){
            if (!MEMBER_ID_COUNTERS.containsKey(username)) {
                if (!DbConfig.shouldPersist())
                    MEMBER_ID_COUNTERS.put(username, 1);
                else
                    try (Session session = DbConfig.getSessionFactory().openSession()) {
                        Query<Integer> query = session.createQuery(
                                "SELECT MAX(n.key.id) FROM DataNotification n WHERE n.key.member.username = :username",
                                Integer.class
                        );
                        query.setParameter("username", username);
                        Integer maxID = query.uniqueResult();
                        MEMBER_ID_COUNTERS.put(username, maxID == null ? 1 : maxID + 1);
                    }
            }
            this.id = MEMBER_ID_COUNTERS.get(username);
            MEMBER_ID_COUNTERS.put(username, id + 1);
        }
    }

    public DataMember getMember() {
        return member;
    }

    public Integer getId() {
        return id;
    }
}
