package DataLayer.User;

import Domain.User.Guest;
import Domain.User.ISystemManager;
import Domain.User.Member;
import util.Exceptions.SessionError;

import java.util.Set;

/**
 * The ISessionRepo interface represents a repository for managing user sessions in the system.
 * It provides methods to add, remove, and query sessions, as well as retrieve user information from sessions.
 */
public interface ISessionRepo {

  /**
   * Adds a member session to the repository.
   *
   * @param sessionId The ID of the session
   * @param member The member object associated with the session
   */
  void addSession(String sessionId, Member member);

  /**
   * Adds a system manager session to the repository.
   *
   * @param sessionId The ID of the session
   * @param systemManager The system manager object associated with the session
   */
  void addSession(String sessionId, ISystemManager systemManager);

  /**
   * Adds a guest session to the repository.
   *
   * @param sessionId The ID of the session
   * @param guest The guest object associated with the session
   */
  void addSession(String sessionId, Guest guest) throws SessionError;

  /**
   * Removes a session from the repository.
   *
   * @param sessionId The ID of the session to be removed
   */
  void removeSession(String sessionId);

  /**
   * Checks if a session with the given session ID exists.
   *
   * @param sessionId The ID of the session to be checked
   * @return true if the session exists, false otherwise
   */
  boolean isSessionExists(String sessionId);

  /**
   * Checks if a session with the given session ID is a guest session.
   *
   * @param sessionId The ID of the session to be checked
   * @return true if the session is a guest session, false otherwise
   */
  boolean isGuestSession(String sessionId);

  /**
   * Checks if a session with the given session ID is a member session.
   *
   * @param sessionId The ID of the session to be checked
   * @return true if the session is a member session, false otherwise
   */
  boolean isMemberSession(String sessionId);

  /**
   * Checks if a session with the given session ID is a system manager session.
   *
   * @param sessionId The ID of the session to be checked
   * @return true if the session is a system manager session, false otherwise
   */
  boolean isSystemManagerSession(String sessionId);

  /**
   * Retrieves the username associated with a member session.
   *
   * @param sessionId The ID of the member session
   * @return The username of the member associated with the session
   * @throws SessionError if the session is not a member session
   */
  String getMemberUserName(String sessionId) throws SessionError;

  /**
   * Retrieves the username associated with a system manager session.
   *
   * @param sessionId The ID of the system manager session
   * @return The username of the system manager associated with the session
   * @throws SessionError if the session is not a system manager session
   */
  String getSystemManagerUserName(String sessionId) throws SessionError;

  /**
   * Retrieves the guest object associated with a guest session.
   *
   * @param sessionId The ID of the guest session
   * @return The guest object associated with the session
   * @throws SessionError if the session is not a guest session
   */
  Guest getGuest(String sessionId) throws SessionError;

    Integer getAmountOfConnectedMembers();

  Integer getAmountOfConnectedGuests();

    Set<String> getAllLoggedMembers();
}