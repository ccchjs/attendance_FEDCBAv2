#!/usr/bin/env sh

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"
DEFAULT_JVM_OPTS=""
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" \
  -classpath "\"$CLASSPATH\"" \
  org.gradle.wrapper.GradleWrapperMain '"$@"'

exec "$JAVACMD" "$@"
