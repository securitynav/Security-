#!/usr/bin/env sh
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    else
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no java command could be found in your PATH."
fi

# Use the maximum available, or set MAX_FD != -1 to use that value
MAX_FD="maximum"

# Setup the classpath
find_gradle_home() {
    # Simple gradle wrapper bootstrap
    GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
    WRAPPED_DIST="$GRADLE_USER_HOME/wrapper/dists/gradle-8.7-bin/b7730999/gradle-8.7"
    echo "$WRAPPED_DIST"
}

# Ejecutar gradle mediante el jar del wrapper si existe, o invocar gradle directamente
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    exec "$JAVACMD" -jar "gradle/wrapper/gradle-wrapper.jar" "$@"
else
    # Si no hay jar, intentar usar gradle del sistema o descargar la distribucion
    if command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "Error: No se encuentra gradle-wrapper.jar ni el comando gradle en el sistema."
        exit 1
    fi
fi
