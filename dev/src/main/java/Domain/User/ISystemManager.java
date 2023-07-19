/**
 * Interface for system manager operations, including login, logout, password management, and retrieving user name.
 */
package Domain.User;

import util.Exceptions.DataError;
import util.Exceptions.SessionError;

public interface ISystemManager {

  /**
   * Logs in the system manager with the provided password.
   *
   * @param password The password to be used for login.
   * @throws SessionError If the system manager is already logged in.
   * @throws SessionError If the provided password is incorrect.
   */
  void login(String password) throws SessionError;

  /**
   * Logs out the system manager.
   *
   * @throws SessionError If the system manager is already disconnected.
   */
  void logout() throws SessionError;

  /**
   * Changes the password of the system manager.
   *
   * @param oldPassword The old password to be replaced.
   * @param newPassword The new password to be set.
   * @throws DataError If the provided old password is incorrect.
   */
  void changePassword(String oldPassword, String newPassword) throws DataError;

  /**
   * Retrieves the user name of the system manager.
   *
   * @return The user name of the system manager.
   */
  String getUserName();

  void remove();
}