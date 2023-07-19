package DataLayer.User;

import Domain.MarketLogger;
import Domain.User.Guest;
import Domain.User.ISystemManager;
import Domain.User.Member;
import util.Enums.ErrorStatus;
import util.Exceptions.SessionError;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SessionRepo implements ISessionRepo {
  private final Map<String, String> sessionToMember;
  private final Map<String, String> sessionToSystemManager;
  private final Map<String, Guest> sessionToGuest;

  public SessionRepo() {
    sessionToMember = new ConcurrentHashMap<>();
    sessionToSystemManager = new ConcurrentHashMap<>();
    sessionToGuest = new ConcurrentHashMap<>();
  }

  @Override
  public void addSession(String sessionId, Member member) {
    sessionToMember.put(sessionId, member.getUserName());
  }

  @Override
  public void addSession(String sessionId, ISystemManager systemManager) {
    sessionToSystemManager.put(sessionId, systemManager.getUserName());
  }

  @Override
  public void addSession(String sessionId, Guest guest) throws SessionError {
    if(sessionToGuest.containsKey(sessionId)){
      MarketLogger.logError("SessionRepo", "addSession", "session already exist",sessionId, guest);
      throw new SessionError("session already exist", ErrorStatus.GUEST_SESSION);
    }
    sessionToGuest.put(sessionId, guest);
  }

  @Override
  public void removeSession(String sessionId) {
    sessionToMember.remove(sessionId);
    sessionToSystemManager.remove(sessionId);
    sessionToGuest.remove(sessionId);
    // when database is implemented, remove session from database
  }

  @Override
  public boolean isSessionExists(String sessionId) {
    return sessionToMember.containsKey(sessionId) ||
        sessionToGuest.containsKey(sessionId) ||
        sessionToSystemManager.containsKey(sessionId);
  }

  @Override
  public boolean isGuestSession(String sessionId) {
    return sessionToGuest.containsKey(sessionId);
  }

  @Override
  public boolean isMemberSession(String sessionId) {
    return sessionToMember.containsKey(sessionId);
  }

  @Override
  public boolean isSystemManagerSession(String sessionId) {
    return sessionToSystemManager.containsKey(sessionId);
  }

  @Override
  public String getMemberUserName(String sessionId) throws SessionError {
    if (!sessionToMember.containsKey(sessionId))
      throw new SessionError(
          "the session given is not a member session", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
    return sessionToMember.get(sessionId);
  }

  @Override
  public String getSystemManagerUserName(String sessionId) throws SessionError {
    if (!sessionToSystemManager.containsKey(sessionId))
      throw new SessionError(
          "the session given is not a system manager session", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
    return sessionToSystemManager.get(sessionId);
  }

  @Override
  public Guest getGuest(String sessionId) throws SessionError {
    if (!sessionToGuest.containsKey(sessionId))
      throw new SessionError(
          "the session given is not a guest session!", ErrorStatus.SESSION_ID_DOES_NOT_EXIST);
    return sessionToGuest.get(sessionId);
  }

  @Override
  public Integer getAmountOfConnectedMembers() {
    return sessionToMember.size();
  }

  @Override
  public Integer getAmountOfConnectedGuests() {
    return sessionToGuest.size();
  }

  @Override
  public Set<String> getAllLoggedMembers() {
    synchronized (sessionToMember) {
      return new HashSet<>(sessionToMember.values());
    }
  }
}
