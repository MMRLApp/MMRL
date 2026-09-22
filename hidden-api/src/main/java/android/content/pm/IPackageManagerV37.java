package android.content.pm;

import android.os.IInterface;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import dev.rikka.tools.refine.RefineAs;

@RequiresApi(37)
@RefineAs(IPackageManager.class)
public interface IPackageManagerV37 extends IInterface {
    PackageInfoList getInstalledPackages(long flags, int userId) throws RemoteException;
}
