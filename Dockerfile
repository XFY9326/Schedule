FROM ubuntu:20.04

LABEL maintainer="XFY9326@xfy9326.top"

# Initial Settings
ENV ANDROID_COMMAND_LINE_TOOLS_VERSION 6858069

ENV ANDROID_PLATFORM_VERSION 30
ENV ANDROID_BUILD_TOOLS_VERSION 30.0.3

ENV GRADLE_VERSION 6.5
ENV GRADLE_TYPE all
ENV GRADLE_DIGEST 2oz4ud9k3tuxjg84bbf55q0tn

# Environment
ENV DEBIAN_FRONTEND noninteractive

# For Users In China 
RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list

RUN apt-get clean
RUN apt-get update
RUN apt-get install git wget unzip openjdk-8-jdk -y --fix-missing

# JAVA
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV JRE_HOME ${JAVA_HOME}/jre
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib:${JAVA_HOME}/lib/dt.jar:${JAVA_HOME}/lib/tools.jar
ENV PATH ${JAVA_HOME}/bin:$PATH

# Android SDK
ENV ANDROID_SDK /Android/sdk
ENV ANDROID_HOME $ANDROID_SDK
ENV PATH ${ANDROID_SDK}/tools:${ANDROID_SDK}/tools/bin:${ANDROID_SDK}/platform-tools:$PATH

RUN mkdir -p ${ANDROID_SDK}

WORKDIR /Android

RUN mkdir ~/.android
RUN touch ~/.android/repositories.cfg

RUN wget --no-check-certificate -O ${ANDROID_SDK}/tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_COMMAND_LINE_TOOLS_VERSION}_latest.zip"
RUN unzip -d ${ANDROID_SDK} ${ANDROID_SDK}/tools.zip > /dev/null
RUN rm -rf ${ANDROID_SDK}/tools.zip

RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "tools" "platform-tools"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "platforms;android-${ANDROID_PLATFORM_VERSION}"

# Gradle
RUN mkdir -p ~/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}/${GRADLE_DIGEST}/
RUN wget --no-check-certificate -O ~/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}/${GRADLE_DIGEST}/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-${GRADLE_TYPE}.zip"