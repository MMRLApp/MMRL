cmake_minimum_required(VERSION 3.22.1)

project(MMRL)

set(CMAKE_C_FLAGS_RELEASE "-O3 -Wall -Werror")
set(CMAKE_CXX_FLAGS_RELEASE ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_C_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELWITHDEBINFO})
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Define 16 KB page size
set(MMRL_PAGE_SIZE 16384)
add_link_options("-Wl,--build-id=none,-z,max-page-size=${MMRL_PAGE_SIZE}")

include_directories(include)

find_library(log-lib log)

add_library(mmrl-file-manager SHARED
        file-manager.cpp
        include/logging.hpp
)
target_link_libraries(mmrl-file-manager ${log-lib})
target_link_options(mmrl-file-manager PRIVATE "-Wl,-z,max-page-size=${MMRL_PAGE_SIZE}")

add_library(mmrl-kernelsu SHARED
        kernelsu/ksu.cpp
        kernelsu/ksun.cpp
        kernelsu/sukisu.cpp
        kernelsu/jni.cpp
        include/kernelsu/ksu.hpp
        include/logging.hpp
)
target_link_libraries(mmrl-kernelsu ${log-lib})
target_link_options(mmrl-kernelsu PRIVATE "-Wl,-z,max-page-size=${MMRL_PAGE_SIZE}")
