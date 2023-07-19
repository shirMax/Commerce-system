package DataLayer.User.ORM;

import DataLayer.DbConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.Session;

import java.time.LocalDate;

@Entity
@Table(name = "Sys_Manager")
public class DataSystemManager {
    @Id
    private String username;
    private String password;

    public DataSystemManager() {
    }

    public DataSystemManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Object getId(){
        return username;
    }
    public DataSystemManager persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataSystemManager updated = session.get(DataSystemManager.class, getId());
            if (updated == null) updated = this;
            updated.setPassword(getPassword());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataSystemManager toRemove = session.get(DataSystemManager.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
