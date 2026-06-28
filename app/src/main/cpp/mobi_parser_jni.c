#include <jni.h>
#include <stdlib.h>
#include <string.h>

#include "mobi.h"
#include "util.h"

typedef struct {
    char *data;
    size_t size;
    size_t capacity;
} TextBuffer;

static int buffer_reserve(TextBuffer *buffer, size_t extra) {
    if (extra > SIZE_MAX - buffer->size - 1) {
        return 0;
    }
    size_t required = buffer->size + extra + 1;
    if (required <= buffer->capacity) {
        return 1;
    }
    size_t next_capacity = buffer->capacity > 0 ? buffer->capacity : 4096;
    while (next_capacity < required) {
        if (next_capacity > SIZE_MAX / 2) {
            next_capacity = required;
            break;
        }
        next_capacity *= 2;
    }
    char *next = (char *) realloc(buffer->data, next_capacity);
    if (next == NULL) {
        return 0;
    }
    buffer->data = next;
    buffer->capacity = next_capacity;
    return 1;
}

static int buffer_append(TextBuffer *buffer, const unsigned char *data, size_t size) {
    if (size == 0) {
        return 1;
    }
    if (!buffer_reserve(buffer, size)) {
        return 0;
    }
    memcpy(buffer->data + buffer->size, data, size);
    buffer->size += size;
    buffer->data[buffer->size] = '\0';
    return 1;
}

static int buffer_append_cstr(TextBuffer *buffer, const char *text) {
    return buffer_append(buffer, (const unsigned char *) text, strlen(text));
}

static void throw_parse_error(JNIEnv *env, const char *message) {
    jclass exception_class = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception_class != NULL) {
        (*env)->ThrowNew(env, exception_class, message);
    }
}

static int append_markup_parts(TextBuffer *buffer, MOBIRawml *rawml) {
    MOBIPart *part = rawml->markup;
    while (part != NULL) {
        if (part->data != NULL && part->size > 0 && part->type == T_HTML) {
            if (!buffer_append(buffer, part->data, part->size)) {
                return 0;
            }
            if (!buffer_append_cstr(buffer, "\n\n")) {
                return 0;
            }
        }
        part = part->next;
    }
    return 1;
}

static int append_rawml_fallback(TextBuffer *buffer, MOBIData *mobi) {
    size_t length = mobi_get_text_maxsize(mobi);
    if (length == 0) {
        length = 1024 * 1024;
    }
    char *text = (char *) calloc(length + 1, sizeof(char));
    if (text == NULL) {
        return 0;
    }
    MOBI_RET ret = mobi_get_rawml(mobi, text, &length);
    if (ret != MOBI_SUCCESS) {
        free(text);
        return 0;
    }
    int ok = 0;
    if (mobi_is_cp1252(mobi)) {
        size_t utf8_length = length * 3 + 1;
        char *utf8 = (char *) calloc(utf8_length, sizeof(char));
        if (utf8 != NULL) {
            MOBI_RET convert_ret = mobi_cp1252_to_utf8(utf8, text, &utf8_length, length);
            if (convert_ret == MOBI_SUCCESS) {
                ok = buffer_append(buffer, (const unsigned char *) utf8, utf8_length);
            }
            free(utf8);
        }
    }
    if (!ok) {
        ok = buffer_append(buffer, (const unsigned char *) text, length);
    }
    free(text);
    return ok;
}

JNIEXPORT jstring JNICALL
Java_com_engread_app_parser_NativeMobiParser_parseFile(JNIEnv *env, jobject thiz, jstring path) {
    (void) thiz;
    const char *native_path = (*env)->GetStringUTFChars(env, path, NULL);
    if (native_path == NULL) {
        return NULL;
    }

    MOBIData *mobi = mobi_init();
    if (mobi == NULL) {
        (*env)->ReleaseStringUTFChars(env, path, native_path);
        throw_parse_error(env, "libmobi 初始化失败");
        return NULL;
    }

    MOBI_RET ret = mobi_load_filename(mobi, native_path);
    (*env)->ReleaseStringUTFChars(env, path, native_path);
    if (ret != MOBI_SUCCESS) {
        mobi_free(mobi);
        throw_parse_error(env, "libmobi 无法读取 MOBI 文件");
        return NULL;
    }

    TextBuffer buffer = {0};
    MOBIRawml *rawml = mobi_init_rawml(mobi);
    if (rawml != NULL) {
        ret = mobi_parse_rawml(rawml, mobi);
        if (ret == MOBI_SUCCESS) {
            if (!append_markup_parts(&buffer, rawml)) {
                mobi_free_rawml(rawml);
                mobi_free(mobi);
                free(buffer.data);
                throw_parse_error(env, "libmobi 输出正文时内存不足");
                return NULL;
            }
        }
        mobi_free_rawml(rawml);
    }

    if (buffer.size == 0 && !append_rawml_fallback(&buffer, mobi)) {
        mobi_free(mobi);
        free(buffer.data);
        throw_parse_error(env, "libmobi 没有解析到可阅读正文");
        return NULL;
    }

    mobi_free(mobi);
    jstring result = (*env)->NewStringUTF(env, buffer.data != NULL ? buffer.data : "");
    free(buffer.data);
    return result;
}
