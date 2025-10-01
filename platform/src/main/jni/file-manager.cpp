#include <jni.h>
#include <sys/stat.h>
#include <jni.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <dirent.h>
#include <cstring>
#include <stack>
#include <cstdio>
#include <cerrno>
#include <string>
#include <fstream>
#include <logging.hpp>
#include <dlfcn.h>

#define MMRL_UNUSED(x) x __attribute__((__unused__))

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeSetOwner(JNIEnv *env,
                                                                  jobject MMRL_UNUSED(thiz),
                                                                  jstring path, jint owner,
                                                                  jint group) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    bool success = (chown(cpath, owner, group) == 0);
    env->ReleaseStringUTFChars(path, cpath);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeSetPermissions(JNIEnv *env,
                                                                        jobject MMRL_UNUSED(thiz),
                                                                        jstring path,
                                                                        jint mode) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    bool success = (chmod(cpath, static_cast<mode_t>(mode)) == 0);
    env->ReleaseStringUTFChars(path, cpath);
    return success ? JNI_TRUE : JNI_FALSE;
}

static JavaVM* gJvm = nullptr;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}

static JNIEnv* getJNIEnv() {
    if (!gJvm) return nullptr;

    JNIEnv* env = nullptr;
    if (gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        if (gJvm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return nullptr;
        }
    }
    return env;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dergoogler_mmrl_platform_file_FileManager_nativeLoadSharedObjects(JNIEnv* env,
                                                                           jobject /* this */,
                                                                           jobjectArray jSoPaths) {
    jsize count = env->GetArrayLength(jSoPaths);
    if (count == 0) return JNI_FALSE;

    JNIEnv* appEnv = getJNIEnv();
    if (!appEnv) return JNI_FALSE;

    for (jsize i = 0; i < count; ++i) {
        auto jSoPath = (jstring)env->GetObjectArrayElement(jSoPaths, i);
        const char* soPath = env->GetStringUTFChars(jSoPath, nullptr);
        if (!soPath) {
            env->ReleaseStringUTFChars(jSoPath, soPath);
            return JNI_FALSE;
        }

        void* handle = dlopen(soPath, RTLD_NOW);
        env->ReleaseStringUTFChars(jSoPath, soPath);
        env->DeleteLocalRef(jSoPath);

        if (!handle) {
            // Failed to load one .so - consider returning false or continue depending on policy
            return JNI_FALSE;
        }

        using RegisterNativesFunc = void (*)(JNIEnv*);
        auto regFunc = (RegisterNativesFunc)dlsym(handle, "registerNatives");
        if (!regFunc) {
            dlclose(handle);
            return JNI_FALSE;
        }

        regFunc(appEnv);
    }

    return JNI_TRUE;
}