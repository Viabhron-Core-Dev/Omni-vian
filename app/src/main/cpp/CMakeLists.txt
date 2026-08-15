cmake_minimum_required(VERSION 3.22.1)

project("llama_bridge")

# Performance flags for ARM NEON and vectorization
if(ANDROID_ABI STREQUAL "arm64-v8a")
    add_compile_options(-O3 -fvectorize -funroll-loops)
endif()

# Toggle this to ON in GitHub Actions CI to fetch and compile the real llama.cpp
option(USE_REAL_LLAMA "Fetch and compile real llama.cpp" OFF)

if(USE_REAL_LLAMA)
    add_compile_definitions(USE_REAL_LLAMA)
    
    include(FetchContent)
    FetchContent_Declare(
        llama
        GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
        GIT_TAG master
    )
    FetchContent_MakeAvailable(llama)
    
    add_library(llama_bridge SHARED llama_bridge.cpp)
    target_link_libraries(llama_bridge llama log)
else()
    add_library(llama_bridge SHARED llama_bridge.cpp)
    target_link_libraries(llama_bridge log)
endif()

