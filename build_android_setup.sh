#!/bin/bash

if [ -z "$ANDROID_HOME" ]; then
  echo "\$ANDROID_HOME is not defined"
  exit 1
fi

echo "Using android sdk: $ANDROID_HOME"

if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "\$ANDROID_NDK_HOME is not defined"
    exit 1
fi

TOOLCHAIN_PREFIX="$1"
if [ -z "$TOOLCHAIN_PREFIX" ]; then
  TOOLCHAIN_PREFIX="/tmp/android-toolchain"
fi


###### ARM ####

if [ -d "$TOOLCHAIN_PREFIX/arm" ]; then
  rm -rf "$TOOLCHAIN_PREFIX/arm"
fi
$ANDROID_NDK_HOME/build/tools/make-standalone-toolchain.sh --arch=arm --toolchain=arm-linux-androideabi-4.9 --platform=android-19 --install-dir="$TOOLCHAIN_PREFIX/arm"

cat << EOF > android-arm_cross.txt
name = 'android'
c = '$TOOLCHAIN_PREFIX/arm/bin/arm-linux-androideabi-gcc'
cpp = '$TOOLCHAIN_PREFIX/arm/bin/arm-linux-androideabi-g++'
ar = '$TOOLCHAIN_PREFIX/arm/bin/arm-linux-androideabi-ar'
ld = '$TOOLCHAIN_PREFIX/arm/bin/arm-linux-androideabi-ld'
strip = '$TOOLCHAIN_PREFIX/arm/bin/arm-linux-androideabi-strip'

root = '$TOOLCHAIN_PREFIX/arm'
EOF

if [ -d "build-android-arm" ]; then
  rm -rf "build-android-arm"
fi
mkdir "build-android-arm"
meson build-android-arm --cross-file android-arm_cross.txt -Dbuild_jni=true

pushd build-android-arm
ninja
cp java/jni/libmicrodb-jni.so ../java/binding/android/src/main/jniLibs/armeabi/
popd

####### x86 #######

if [ -d "$TOOLCHAIN_PREFIX/x86" ]; then
  rm -rf "$TOOLCHAIN_PREFIX/x86"
fi
$ANDROID_NDK_HOME/build/tools/make-standalone-toolchain.sh --arch=x86 --toolchain=x86-4.9 --platform=android-19 --install-dir="$TOOLCHAIN_PREFIX/x86"


cat << EOF > android-x86_cross.txt
name = 'android'
c = '$TOOLCHAIN_PREFIX/x86/bin/i686-linux-android-gcc'
cpp = '$TOOLCHAIN_PREFIX/x86/bin/i686-linux-android-g++'
ar = '$TOOLCHAIN_PREFIX/x86/bin/i686-linux-android-ar'
ld = '$TOOLCHAIN_PREFIX/x86/bin/i686-linux-android-ld'
strip = '$TOOLCHAIN_PREFIX/x86/bin/i686-linux-android-strip'

root = '$TOOLCHAIN_PREFIX/x86'
EOF

if [ -d "build-android-x86" ]; then
  rm -rf "build-android-x86"
fi
mkdir "build-android-x86"
meson build-android-x86 --cross-file android-x86_cross.txt -Dbuild_jni=true

pushd build-android-x86
ninja
cp java/jni/libmicrodb-jni.so ../java/binding/android/src/main/jniLibs/x86/
popd