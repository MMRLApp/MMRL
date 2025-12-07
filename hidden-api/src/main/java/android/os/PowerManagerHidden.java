package android.os;

import androidx.annotation.RequiresApi;

public class PowerManagerHidden {
    @RequiresApi(30)
    public static boolean isRebootingUserspaceSupportedImpl() {
        throw new RuntimeException("Stub!");
    }
}
