FROM ubuntu:20.04

LABEL maintainer="XFY9326@xfy9326.top"

# Environment
ENV DEBIAN_FRONTEND noninteractive

# For users in china, use tuna mirrors instead
RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list

RUN apt-get clean
RUN apt-get update
RUN apt-get install git wget unzip openjdk-8-jdk -y --fix-missing

# JAVA
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV JRE_HOME ${JAVA_HOME}/jre
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib:${JAVA_HOME}/lib/dt.jar:${JAVA_HOME}/lib/tools.jar
ENV PATH ${JAVA_HOME}/bin:$PATH

# Gradle
ENV GRADLE_DISTS ~/.gradle/wrapper/dists
ENV GRADLE_VERSION 6.7.1
ENV GRADLE_TYPE all
ENV GRADLE_DIGEST 2moa8rlfac5eqlcfgk98k0deb

# For users in china, use tencent mirrors instead
# ENV GRADLE_DISTRIBUTIONS_URL https://services.gradle.org/distributions
ENV GRADLE_DISTRIBUTIONS_URL https://mirrors.cloud.tencent.com/gradle

RUN mkdir -p ${GRADLE_DISTS}/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}/${GRADLE_DIGEST}/
RUN wget --no-check-certificate -O ${GRADLE_DISTS}/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}/${GRADLE_DIGEST}/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}.zip "${GRADLE_DISTRIBUTIONS_URL}/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}.zip"

# Android SDK
WORKDIR /Android

ENV ANDROID_SDK /Android/sdk
ENV ANDROID_HOME $ANDROID_SDK

RUN mkdir -p ${ANDROID_HOME}
RUN mkdir ~/.android
RUN touch ~/.android/repositories.cfg

ENV ANDROID_COMMAND_LINE_TOOLS_VERSION 7302050

RUN wget --no-check-certificate -O ${ANDROID_HOME}/tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_COMMAND_LINE_TOOLS_VERSION}_latest.zip"
RUN unzip -d ${ANDROID_HOME} ${ANDROID_HOME}/tools.zip > /dev/null
RUN rm -rf ${ANDROID_HOME}/tools.zip
ENV PATH ${ANDROID_HOME}/cmdline-tools/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:$PATH

ENV ANDROID_PLATFORM_VERSION 30
ENV ANDROID_BUILD_TOOLS_VERSION 30.0.3

RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "tools" "platform-tools"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "platforms;android-${ANDROID_PLATFORM_VERSION}"
ENV PATH ${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/${ANDROID_BUILD_TOOLS_VERSION}:$PATH