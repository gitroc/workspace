LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := JniSample
LOCAL_SRC_FILES := \
	D:\project\workspace\MainTab\src\main\jni\com_maintab_JniString.cpp \
	D:\project\workspace\MainTab\src\main\jni\util.c \

LOCAL_C_INCLUDES += D:\project\workspace\MainTab\src\main\jni
LOCAL_C_INCLUDES += D:\project\workspace\MainTab\build-types\debug\jni

include $(BUILD_SHARED_LIBRARY)
