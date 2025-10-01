#ifndef KERNELSU_KSU_HPP
#define KERNELSU_KSU_HPP

#include <linux/capability.h>
#include <cstdint>

#define KERNEL_SU_OPTION 0xDEADBEEF

#define CMD_GRANT_ROOT 0

#define CMD_BECOME_MANAGER 1
#define CMD_GET_VERSION 2
#define CMD_ALLOW_SU 3
#define CMD_DENY_SU 4
#define CMD_GET_SU_LIST 5
#define CMD_GET_DENY_LIST 6
#define CMD_SET_SEPOLICY 8
#define CMD_CHECK_SAFEMODE 9

#define CMD_GET_APP_PROFILE 10
#define CMD_SET_APP_PROFILE 11

#define CMD_IS_UID_GRANTED_ROOT 12
#define CMD_IS_UID_SHOULD_UMOUNT 13
#define CMD_IS_SU_ENABLED 14
#define CMD_ENABLE_SU 15

bool ksuctl(int cmd, void* arg1, void* arg2);

bool grant_root();

bool become_manager(const char *);

static bool is_lkm = false;

int get_version();

bool get_allow_list(int *uids, int *size);

bool uid_should_umount(int uid);

bool is_safe_mode();

bool is_lkm_mode();

#define KSU_APP_PROFILE_VER 2
#define KSU_MAX_PACKAGE_NAME 256
// NGROUPS_MAX for Linux is 65535 generally, but we only supports 32 groups.
#define KSU_MAX_GROUPS 32
#define KSU_SELINUX_DOMAIN 64

using p_key_t = char[KSU_MAX_PACKAGE_NAME];

struct root_profile {
    int32_t uid;
    int32_t gid;

    int32_t groups_count;
    int32_t groups[KSU_MAX_GROUPS];

    // kernel_cap_t is u32[2] for capabilities v3
    struct {
        uint64_t effective;
        uint64_t permitted;
        uint64_t inheritable;
    } capabilities;

    char selinux_domain[KSU_SELINUX_DOMAIN];

    int32_t namespaces;
};

struct non_root_profile {
    bool umount_modules;
};

struct app_profile {
    // It may be utilized for backward compatibility, although we have never explicitly made any promises regarding this.
    uint32_t version;

    // this is usually the package of the app, but can be other value for special apps
    char key[KSU_MAX_PACKAGE_NAME];
    int32_t current_uid;
    bool allow_su;

    union {
        struct {
            bool use_default;
            char template_name[KSU_MAX_PACKAGE_NAME];

            struct root_profile profile;
        } rp_config;

        struct {
            bool use_default;

            struct non_root_profile profile;
        } nrp_config;
    };
};

bool set_app_profile(const app_profile *profile);

bool get_app_profile(p_key_t key, app_profile *profile);

bool set_su_enabled(bool enabled);

bool is_su_enabled();

struct FfiPolicy {
		uint32_t cmd;
		uint32_t subcmd;
		const char* sepol1;
		const char* sepol2;
		const char* sepol3;
		const char* sepol4;
		const char* sepol5;
		const char* sepol6;
		const char* sepol7;
};

bool ksu_set_policy(const FfiPolicy* policy);

#endif //KERNELSU_KSU_HPP