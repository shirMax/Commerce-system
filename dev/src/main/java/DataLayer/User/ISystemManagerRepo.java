/**
 * The interface for managing system managers in the data layer.
 */
package DataLayer.User;

import Domain.User.ISystemManager;
import Domain.User.SystemManager;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;

public interface ISystemManagerRepo {

  /**
   * Retrieves a system manager by username.
   *
   * @param userName The username of the system manager to retrieve
   * @return The system manager object associated with the given username
   * @throws NonExistentData if the system manager with the given username doesn't exist
   */
  ISystemManager getSystemManager(String userName) throws NonExistentData;

  /**
   * Checks if a system manager with the given username exists.
   *
   * @param userName The username to check
   * @return true if a system manager with the given username exists, false otherwise
   */
  boolean isSystemManagerExists(String userName);


  /**
   * Adds a new system manager.
   *
   * @param username The system manager object to add
   * @param password
   * @throws DataExistentError if a system manager with the same username already exists
   */
  void addSystemManager(String username, String password) throws DataExistentError;

  /**
   * Initializes the system manager with initial data.
   *
   * @param systemManager The system manager object to initialize
   */
  void initSystemManager(SystemManager systemManager);
}