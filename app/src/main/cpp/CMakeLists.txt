cmake_minimum_required(VERSION 3.22.1)

project(engread_native C)

set(LIBMOBI_DIR ${CMAKE_CURRENT_LIST_DIR}/../../../../third_party/libmobi/src)

add_library(mobi_static STATIC
    ${LIBMOBI_DIR}/buffer.c
    ${LIBMOBI_DIR}/compression.c
    ${LIBMOBI_DIR}/debug.c
    ${LIBMOBI_DIR}/index.c
    ${LIBMOBI_DIR}/memory.c
    ${LIBMOBI_DIR}/meta.c
    ${LIBMOBI_DIR}/miniz.c
    ${LIBMOBI_DIR}/opf.c
    ${LIBMOBI_DIR}/parse_rawml.c
    ${LIBMOBI_DIR}/read.c
    ${LIBMOBI_DIR}/structure.c
    ${LIBMOBI_DIR}/util.c
    ${LIBMOBI_DIR}/write.c
    ${LIBMOBI_DIR}/xmlwriter.c
)

target_include_directories(mobi_static PUBLIC ${LIBMOBI_DIR})
target_compile_definitions(mobi_static PRIVATE
    USE_MINIZ
    USE_XMLWRITER
    PACKAGE_VERSION="0.12"
    MOBI_INLINE=inline
)
target_compile_options(mobi_static PRIVATE -Wno-unused-parameter -Wno-sign-compare)

add_library(mobi_parser SHARED mobi_parser_jni.c)
target_include_directories(mobi_parser PRIVATE ${LIBMOBI_DIR})
target_link_libraries(mobi_parser PRIVATE mobi_static log)
