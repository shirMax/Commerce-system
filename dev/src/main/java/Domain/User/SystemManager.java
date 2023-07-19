package Domain.User;

import DataLayer.User.ORM.DataSystemManager;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;
import util.Exceptions.SessionError;

public class SystemManager implements ISystemManager {
  private DataSystemManager dataSystemManager;

  private boolean isLogged;


  // CTOR for data creation
  public SystemManager(String userName, String password) {
    dataSystemManager = new DataSystemManager(userName, password);
    dataSystemManager = dataSystemManager.persist();
  }

  // CTOR for recovery from DB
  public SystemManager(DataSystemManager dataSystemManager){
    this.dataSystemManager = dataSystemManager;
  }

  @Override
  public void login(String password) throws SessionError {
    if (isLogged())
      throw new SessionError(
          "system manager is already logged in!", ErrorStatus.ALREADY_LOGGED_IN);
    if (!dataSystemManager.getPassword().equals(password))
      throw new SessionError("wrong password!", ErrorStatus.USERNAME_PASSWORD_MISMATCH);
    setLogged(true);
    dataSystemManager = dataSystemManager.persist();
  }

  @Override
  public void logout() throws SessionError {
    if (!isLogged())
      throw new SessionError(
          "cant logout system manager because he is not logged in!", ErrorStatus.NOT_LOGGED_IN);
    setLogged(false);
    dataSystemManager = dataSystemManager.persist();
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) throws DataError {
    if (!dataSystemManager.getPassword().equals(oldPassword))
      throw new DataError("Incorrect old password!", ErrorStatus.INVALID_PASSWORD);
    dataSystemManager.setPassword(newPassword);
    dataSystemManager = dataSystemManager.persist();
  }

  @Override
  public String getUserName() {
    return dataSystemManager.getUsername();
  }

  @Override
  public void remove(){
    dataSystemManager.remove();
  }

  public boolean isLogged() {
    return isLogged;
  }

  public void setLogged(boolean logged) {
    isLogged = logged;
  }
}
