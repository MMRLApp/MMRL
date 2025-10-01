package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.stub.IFileManager;
import com.dergoogler.mmrl.platform.stub.IModuleManager;
import com.dergoogler.mmrl.platform.content.Service;

interface IServiceManager {
    int getUid() = 0;
    int getPid() = 1;
    String getSELinuxContext() = 2;
    String currentPlatform() = 3;
    IModuleManager getModuleManager() = 4;
    IFileManager getFileManager() = 5;
    IBinder addService(in Service service) = 6;
    IBinder getService(String name) = 7;
    boolean isSELinuxEnabled() = 8;
    boolean isSELinuxEnforced() = 9;
    void addServiceBinder(String name, IBinder binder) = 10;
}