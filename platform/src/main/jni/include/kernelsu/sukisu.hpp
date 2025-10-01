#ifndef MMRL_SUKISU_HPP
#define MMRL_SUKISU_HPP

#include <__stddef_size_t.h>

#define CMD_ENABLE_KPM 100
#define CMD_HOOK_TYPE 101

bool is_KPM_enable();

bool get_suki_hook_type(char* hook_type, size_t size);

#endif //MMRL_SUKISU_HPP
