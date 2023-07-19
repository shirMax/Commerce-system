package DataLayer.User;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataSystemManager;
import Domain.User.ISystemManager;
import Domain.User.SystemManager;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Enums.ErrorStatus;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemManagerRepo implements ISystemManagerRepo {
  private final Map<String, ISystemManager> managers;

  public SystemManagerRepo() {
    this.managers = new ConcurrentHashMap<>();
  }

  @Override
  public ISystemManager getSystemManager(String userName) throws NonExistentData {
    pullDataIfAbsent(userName);
    if (!managers.containsKey(userName))
      throw new NonExistentData("the user name is not exists", ErrorStatus.USERNAME_DOES_NOT_EXIST);
    return managers.get(userName);
  }

  @Override
  public boolean isSystemManagerExists(String userName) {
    try {
      getSystemManager(userName);
      return true;
    } catch (NonExistentData e) {
      return false;
    }
  }

  @Override
  public void addSystemManager(String username, String password) throws DataExistentError {
    if (isSystemManagerExists(username))
      throw new DataExistentError("system manager already exists!", ErrorStatus.USERNAME_EXISTS);
    managers.put(username, new SystemManager(username, password));
  }

  @Override
  public void initSystemManager(SystemManager systemManager) {
    managers.put(systemManager.getUserName(), systemManager);
  }

  private void pullDataIfAbsent(String username){
    if (!DbConfig.shouldPersist() || managers.containsKey(username)) return;
    synchronized (managers) {
      if (managers.containsKey(username))
        return;
      DataSystemManager dataSystemManager;
      ISystemManager systemManager;
      try (Session session = DbConfig.getSessionFactory().openSession()) {
        Query<DataSystemManager> query = session.createQuery("From DataSystemManager m WHERE m.username = :username", DataSystemManager.class);
        query.setParameter("username", username);
        dataSystemManager = query.getSingleResult();
        systemManager = new SystemManager(dataSystemManager);
        managers.put(username, systemManager);
      } catch (NoResultException ignored) {
      }
    }
  }
}
