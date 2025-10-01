#include <kernelsu/ksun.hpp>
#include <kernelsu/ksu.hpp>

const char* get_ksun_hook_mode() {
    static char mode[16];
    ksuctl(CMD_KSUN_HOOK_MODE, mode, nullptr);
    return mode;
}