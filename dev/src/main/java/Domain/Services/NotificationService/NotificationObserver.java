package Domain.Services.NotificationService;

import util.Records.NotificationRecord;

public interface NotificationObserver {
    /**
     * notify the observer with the given notification.
     * @param notification a notification.
     * @return true iff the notification succeeded (if failed then notification should be stored).
     */
    boolean notify(NotificationRecord notification);
}
