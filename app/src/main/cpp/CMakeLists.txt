
cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/inc)

add_library( # Sets the name of the library.
        native-lib
        SHARED
        native-lib.cpp)
#设置第三方so库的路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries( # Specifies the target library.
        native-lib
        mobvoidsp
        log)