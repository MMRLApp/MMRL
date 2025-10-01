#include <sys/prctl.h>
#include <cstdint>
#include <cstring>
#include <cstdio>
#include <unistd.h>
#include <kernelsu/ksu.hpp>
#include <logging.hpp>

bool ksuctl(int cmd, void* arg1, void* arg2) {
    int32_t result = 0;
    prctl(KERNEL_SU_OPTION, cmd, arg1, arg2, &result);
    return result == KERNEL_SU_OPTION;
}

bool grant_root() {
    return ksuctl(CMD_GRANT_ROOT, nullptr, nullptr);
}

bool become_manager(const char *pkg) {
    char param[128];
    uid_t uid = getuid();
    uint32_t userId = uid / 100000;
    if (userId == 0) {
        sprintf(param, "/data/data/%s", pkg);
    } else {
        snprintf(param, sizeof(param), "/data/user/%d/%s", userId, pkg);
    }

    return ksuctl(CMD_BECOME_MANAGER, param, nullptr);
}

int get_version() {
    int32_t version = -1;
    int32_t flags = 0;
    ksuctl(CMD_GET_VERSION, &version, &flags);
    if (!is_lkm && (flags & 0x1)) {
        is_lkm = true;
    }
    return version;
}

bool get_allow_list(int *uids, int *size) {
    return ksuctl(CMD_GET_SU_LIST, uids, size);
}

bool is_safe_mode() {
    return ksuctl(CMD_CHECK_SAFEMODE, nullptr, nullptr);
}

bool is_lkm_mode() {
    return is_lkm;
}

bool uid_should_umount(int uid) {
    bool should;
    return ksuctl(CMD_IS_UID_SHOULD_UMOUNT, reinterpret_cast<void *>(uid), &should) && should;
}

bool set_su_enabled(bool enabled) {
    return ksuctl(CMD_ENABLE_SU, (void *) enabled, nullptr);
}

bool is_su_enabled() {
    bool enabled = true;
    // if ksuctl failed, we assume su is enabled, and it cannot be disabled.
    ksuctl(CMD_IS_SU_ENABLED, &enabled, nullptr);
    return enabled;
}

bool set_app_profile(const app_profile *profile) {
	return ksuctl(CMD_SET_APP_PROFILE, (void*) profile, nullptr);
}

bool get_app_profile(p_key_t key, app_profile *profile) {
	return ksuctl(CMD_GET_APP_PROFILE, (void*) profile, nullptr);
}

bool ksu_set_policy(const FfiPolicy* policy) {
	if (!policy) return false;
	return ksuctl(CMD_SET_SEPOLICY, nullptr,(void*) policy);
}