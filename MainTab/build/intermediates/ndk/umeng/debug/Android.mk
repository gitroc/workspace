LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := MainTab
LOCAL_SRC_FILES := \
	D:\project\saiscapp\Android\MainTab\src\main\jni\com_maintab_JniString.cpp \

LOCAL_C_INCLUDES += D:\project\saiscapp\Android\MainTab\src\main\jni
LOCAL_C_INCLUDES += D:\project\saiscapp\Android\MainTab\src\umeng\jni
LOCAL_C_INCLUDES += D:\project\saiscapp\Android\MainTab\build-types\debug\jni
LOCAL_C_INCLUDES += D:\project\saiscapp\Android\MainTab\src\umengDebug\jni

include $(BUILD_SHARED_LIBRARY)
