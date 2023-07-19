package Domain.User;

public abstract class User implements IUser {
  private String sessionId;

  public User(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setSessionId(String sessionId) { this.sessionId = sessionId; }
  public String getSessionId() { return sessionId; }
}
