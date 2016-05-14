LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := hello.c
## add PIE support default
# http://stackoverflow.com/questions/24818902/running-a-native-library-on-android-l-error-only-position-independent-executab
##
LOCAL_CFLAGS := -fPIE
LOCAL_LDFLAGS := -fPIE -pie -llog


LOCAL_MODULE := hello

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := hello.c
LOCAL_LDFLAGS := -llog


LOCAL_MODULE := hello_nopie

include $(BUILD_EXECUTABLE)
