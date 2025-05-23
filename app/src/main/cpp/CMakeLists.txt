cmake_minimum_required(VERSION 3.4.1)

# Specify the library name and source files
add_library(native_lib SHARED
            native_bin.cpp)  # Adjust the path as necessary

# Find the log library
find_library(log-lib log)

# Link the log library
target_link_libraries(native_lib ${log-lib})
