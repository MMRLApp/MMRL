package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.content.LocalModule;
import com.dergoogler.mmrl.platform.content.ModuleCompatibility;
import com.dergoogler.mmrl.platform.content.BulkModule;
import com.dergoogler.mmrl.platform.content.NullableBoolean;
import com.dergoogler.mmrl.platform.model.ModId;
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback;

interface IModuleManager {
    String getManagerName();
    String getVersion();
    String getBinary();
    int getVersionCode();
    List<LocalModule> getModules();
    ModuleCompatibility getModuleCompatibility();
    LocalModule getModuleById(in ModId id);
    LocalModule getModuleInfo(String zipPath);
    oneway void reboot(String reason);
    oneway void enable(in ModId id, boolean useShell, IModuleOpsCallback callback);
    oneway void disable(in ModId id, boolean useShell, IModuleOpsCallback callback);
    oneway void remove(in ModId id, boolean useShell, IModuleOpsCallback callback);
    String getInstallCommand(String path);
    String getActionCommand(in ModId id);
    List<String> getActionEnvironment();

    // General
    int getSuperUserCount();

    // KernelSU (Next) related
    NullableBoolean isLkmMode();
    boolean isSafeMode();
    boolean setSuEnabled(boolean enabled);
    boolean isSuEnabled();
    boolean uidShouldUmount(int uid);
}