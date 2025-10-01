package android.os;

import android.content.pm.UserInfo;

import java.util.List;

public interface IUserManager extends IInterface {

    List<UserInfo> getUsers(boolean excludeDying) throws RemoteException;

    List<UserInfo> getUsers(boolean excludePartial, boolean excludeDying) throws RemoteException;

    List<UserInfo> getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated) throws RemoteException;

    UserInfo getUserInfo(int userId);

    int[] getProfileIds(int userId, boolean enabledOnly);

    List<UserInfo> getProfiles(int userId, boolean enabledOnly);

    abstract class Stub extends Binder implements IUserManager {

        public static IUserManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}