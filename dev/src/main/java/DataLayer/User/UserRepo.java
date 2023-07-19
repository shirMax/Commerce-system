package DataLayer.User;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataMember;
import Domain.User.Member;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Enums.ErrorStatus;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Records.UserRecords.UserRecord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserRepo implements IUserRepo {
    private final Map<String, Member> members;

    public UserRepo() {
        this.members = new ConcurrentHashMap<>();
    }

    @Override
    public Member getMember(String userName) throws NonExistentData {
        pullDataIfAbsent(userName);
        if (members.containsKey(userName)) {
            return members.get(userName);
        }
        throw new NonExistentData("user doesn't exists", ErrorStatus.USERNAME_DOES_NOT_EXIST);
    }

    @Override
    public void addMember(String sessionID, UserRecord userData, String password) throws DataExistentError {
        if (isMemberExists(userData.username()))
            throw new DataExistentError("user name is already exists!", ErrorStatus.USERNAME_EXISTS);
        if(members.put(userData.username(), new Member(sessionID, userData, password)) != null){
            throw new DataExistentError("user name is already exists!", ErrorStatus.USERNAME_EXISTS);
        }
    }

    @Override
    public boolean isMemberExists(String userName) {
        try {
            getMember(userName);
        } catch (NonExistentData e) {
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getAllMembers() {
        if (!DbConfig.shouldPersist())
            return members.values().stream()
                    .map(Member::getUserName)
                    .collect(Collectors.toSet());

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            Query<DataMember> query = session.createQuery("FROM DataMember", DataMember.class);
            for (DataMember dataMember : query.list())
                if (!members.containsKey(dataMember.getUsername()))
                    members.put(dataMember.getUsername(), new Member(dataMember));
        }
        return members.values().stream()
                .map(Member::getUserName)
                .collect(Collectors.toSet());
    }

    @Override
    public void removeMember(String userName) {
        pullDataIfAbsent(userName);
        Member toRemove = members.remove(userName);
        if (toRemove != null) toRemove.remove();
    }

    private void pullDataIfAbsent(String userName) {
        if (!DbConfig.shouldPersist() || members.containsKey(userName)) return;

        synchronized (members) {
            if (members.containsKey(userName))
                return;
            DataMember dataMember;
            Member member;
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                Query<DataMember> query = session.createQuery("From DataMember m WHERE m.username = :username", DataMember.class);
                query.setParameter("username", userName);
                dataMember = query.getSingleResult();
                member = new Member(dataMember);
                members.put(userName, member);
            } catch (NoResultException ignored) {
                return;
            }
        }
    }
}
