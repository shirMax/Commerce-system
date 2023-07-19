/**
 * The interface for managing users in the data layer.
 */
package DataLayer.User;

import Domain.User.Member;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Records.UserRecords.UserRecord;

import java.util.Set;

public interface IUserRepo {

  /**
   * Retrieves a member by username.
   *
   * @param userName The username of the member to retrieve
   * @return The member object associated with the given username
   * @throws NonExistentData if the member with the given username doesn't exist
   */
  Member getMember(String userName) throws NonExistentData;

  /**
   * Adds a new member.
   *
   * @throws DataExistentError if a member with the same username already exists
   */
  void addMember(String sessionID, UserRecord userData, String password) throws DataExistentError;

  /**
   * Checks if a member with the given username exists.
   *
   * @param userName The username to check
   * @return true if a member with the given username exists, false otherwise
   */
  boolean isMemberExists(String userName);

  /**
   * @return all members usernames
   */
  Set<String> getAllMembers();

  /**
   * A SystemManager Action, remove member from the system if he doesn't hold any rule in a store.
   * @param userName the username to be deleted
   */
  void removeMember(String userName);

}