package util;

import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;

public interface  AppointOwnerCallBack {
    void onCallback() throws DataExistentError;
}
