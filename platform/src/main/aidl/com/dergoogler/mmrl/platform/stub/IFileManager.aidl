package com.dergoogler.mmrl.platform.stub;

import android.os.ParcelFileDescriptor;
import com.dergoogler.mmrl.platform.content.ParcelResult;

interface IFileManager {
    boolean deleteOnExit(String path);
    String[] list(String path);
    long stat(String path);
    long length(String path);
    boolean delete(String path);
    boolean exists(String path);
    boolean isDirectory(String path);
    boolean isFile(String path);
    boolean isBlock(String path);
    boolean isCharacter(String path);
    boolean isSymlink(String path);
    boolean isNamedPipe(String path);
    boolean isSocket(String path);
    boolean mkdir(String path);
    boolean mkdirs(String path);
    boolean createNewFile(String path);
    boolean renameTo(String target, String dest);
    void copyTo(String path, String target, boolean overwrite);
    boolean canExecute(String path);
    boolean canWrite(String path);
    boolean canRead(String path);
    boolean isHidden(String path);
    boolean setPermissions(String path, int mode);
    boolean setOwner(String path, int owner, int group);
    ParcelFileDescriptor parcelFile(String path);
    ParcelResult openReadStream(String path, in ParcelFileDescriptor fd);
    ParcelResult openWriteStream(String path, in ParcelFileDescriptor fd, boolean append);
    int getMode(String path);
    boolean loadSharedObjects(in String[] path);
}