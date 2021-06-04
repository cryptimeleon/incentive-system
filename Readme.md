# Cryptimemeleon Rewards App

This repository contains the android application for the cryptimeleon rewards system.

## Project Setup

We use (currently due to problems with Gradle on Java 16) Java 11 as the JDK (File->Project Structure->SDK Location).
Before building the app, you need to checkout, build, and publishToMavenLocal [cryptimeleon/math](https://github.com/cryptimeleon/math), [cryptimeleon/craco](https://github.com/cryptimeleon/craco) and the subproject cryptoprotocols of [cryptimeleon/incentive-system](https://github.com/cryptimeleon/incentive-system).

## MCL on android

We use the C++ library herumi/mcl as a fast implementation of the BarretoNaehrig group used in the incentive-system.
This needs to be compiled using NDK and added to the project as a jniLib. 

 1. Install [NDK](https://developer.android.com/studio/projects/install-ndk)
 2. Checkout [MCL](https://github.com/herumi/mcl) with the *correct version* (currently v1.28, which is the first release with native android support)
 3. Build mcl (might not be necessary) by following the instructions
 4. Build mcl for android: Go to `mcl/ffi/java/android/jni` and call `ndk-build` (either add ndk-build to your path, or use an absolute path, e.g. `$<path-to-mcl>/ffi/java/android/jni$ ~/Android/Sdk/ndk/22.1.7171670/ndk-build`)
 5. Copy the directories in `mcl/ffi/java/android/libs` (which contain the `libmcljava.so` file) to `app/src/main/jniLibs`
 6. Load mcl in the app prior usage with `System.loadLibrary("mcljava")`, e.g. in `MainActivity.onCreate`.