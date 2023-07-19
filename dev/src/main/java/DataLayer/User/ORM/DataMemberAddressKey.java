package DataLayer.User.ORM;

import DataLayer.DbConfig;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Embeddable
public class DataMemberAddressKey implements Serializable {
    private static final Map<String, Integer> MEMBERS_ID_COUNTERS = new ConcurrentHashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_username", nullable = false)
    private DataMember member;

    @Column(name = "id")
    private Integer id;

    public DataMemberAddressKey() {}

    public DataMemberAddressKey(DataMember member){
        this.member = member;
        String username = member.getUsername();
        synchronized (MEMBERS_ID_COUNTERS){
            if (!MEMBERS_ID_COUNTERS.containsKey(username)) {
                if (!DbConfig.shouldPersist())
                    MEMBERS_ID_COUNTERS.put(username, 1);
                else
                    try (Session session = DbConfig.getSessionFactory().openSession()) {
                        Query<Integer> query = session.createQuery(
                                "SELECT MAX(a.key.id) FROM DataMemberAddress a WHERE a.key.member.username = :username",
                                Integer.class
                        );
                        query.setParameter("username", username);
                        Integer maxID = query.uniqueResult();
                        MEMBERS_ID_COUNTERS.put(username, maxID == null ? 1 : maxID + 1);
                    }
            }
            this.id = MEMBERS_ID_COUNTERS.get(username);
            MEMBERS_ID_COUNTERS.put(username, id + 1);
        }
    }

    public Integer getId() {
        return id;
    }

    public DataMember getMember() {
        return member;
    }
}
