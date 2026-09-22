package android.content.pm;

import android.content.pm.PackageInfo;
import java.util.List;

import androidx.annotation.RequiresApi;

@RequiresApi(37)
public final class PackageInfoList extends ParceledListSlice<PackageInfo> {
    public static PackageInfoList emptyList() {
        throw new RuntimeException("Stub!");
    }

    public PackageInfoList(List<PackageInfo> list) {
        super(list);
        throw new RuntimeException("Stub!");
    }
}
