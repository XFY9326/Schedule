FROM ubuntu:latest

LABEL maintainer="XFY9326@xfy9326.github.io"

# WorkDir
WORKDIR /Android

# Environment
ENV DEBIAN_FRONTEND noninteractive

# Change apt source for users in China 
# RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list

# Init environment
RUN apt-get update
RUN apt-get install -y --fix-missing git curl wget unzip
# ----- Add apt package here -----

# Install JDK
ARG JDK_VERSION=11
RUN apt-get install -y --fix-missing openjdk-${JDK_VERSION}-jdk 

# Android SDK manager
ENV ANDROID_HOME /root/Android/Sdk
ENV ANDROID_SDK_ROOT ${ANDROID_HOME}
ENV PATH ${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin

RUN mkdir -p ${ANDROID_HOME}/.cache
RUN wget --no-check-certificate -O "${ANDROID_HOME}/.cache/commandlinetools.zip" "$(curl -s 'https://developer.android.com/studio' | grep -Eo 'https://dl.google.com/android/repository/commandlinetools-linux-.*_latest.zip')"
RUN unzip -d "${ANDROID_HOME}/cmdline-tools" "${ANDROID_HOME}/.cache/commandlinetools.zip" && rm -rf "${ANDROID_HOME}/.cache/commandlinetools.zip"
RUN mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest"

# Android repo config
RUN mkdir ${HOME}/.android
RUN touch ${HOME}/.android/repositories.cfg

# Install Android SDK
ENV PATH ${PATH}:${ANDROID_HOME}/platform-tools

RUN yes | sdkmanager --licenses
RUN sdkmanager "tools" "platform-tools"
# ----- Add SDK package here -----
