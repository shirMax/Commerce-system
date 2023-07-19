package Domain.Services;

import org.checkerframework.checker.nullness.qual.NonNull;
public class Response<T> {
  private String message;
  private T returnObject = null;
  private Boolean errorOccurred;
  private int status;

  public Response(String message, Boolean errorOccurred, int status) {
    this.message = message;
    this.errorOccurred = errorOccurred;
    this.status = status;
  }

  public String getMessage() { return message; }

  public Boolean isErrorOccurred() { return errorOccurred; }

  public void setMessage(@NonNull String message) { this.message = message; }

  public void setErrorOccurred(Boolean errorOccurred) {
    this.errorOccurred = errorOccurred;
  }

  public Object getReturnObject() { return returnObject; }

  public void setReturnObject(@NonNull T returnObject) {
    this.returnObject = returnObject;
  }

  public static Response createSuccessResponse() {
    return new Response("", false, 200);
  }

  public static Response createErrorResponse(@NonNull String message,
                                             int status) {
    return new Response(message, true, status);
  }

  public int getStatus() { return status; }

  public void setStatus(int status) { this.status = status; }
}
