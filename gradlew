#!/bin/sh
# Lightweight, source-only Gradle wrapper bootstrap for environments where the
# standard gradle-wrapper.jar cannot be committed. It honours the version and
# distribution URL in gradle/wrapper/gradle-wrapper.properties.
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPERTIES" ]; then
    echo "Missing Gradle wrapper configuration: $PROPERTIES" >&2
    exit 1
fi

DISTRIBUTION_URL=$(sed -n 's/^distributionUrl=//p' "$PROPERTIES" | sed 's#\\:#:#g')
if [ -z "$DISTRIBUTION_URL" ]; then
    echo "distributionUrl is missing from $PROPERTIES" >&2
    exit 1
fi

ARCHIVE_NAME=${DISTRIBUTION_URL##*/}
GRADLE_NAME=${ARCHIVE_NAME%-bin.zip}
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
DIST_ROOT="$GRADLE_USER_HOME/wrapper/dists/$GRADLE_NAME"
GRADLE_HOME="$DIST_ROOT/$GRADLE_NAME"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    mkdir -p "$DIST_ROOT"
    ARCHIVE="$DIST_ROOT/$ARCHIVE_NAME"
    if [ ! -f "$ARCHIVE" ]; then
        echo "Downloading $GRADLE_NAME..." >&2
        TEMP_ARCHIVE="$ARCHIVE.part"
        rm -f "$TEMP_ARCHIVE"
        if command -v curl >/dev/null 2>&1; then
            curl --fail --location --retry 3 --output "$TEMP_ARCHIVE" "$DISTRIBUTION_URL"
        elif command -v wget >/dev/null 2>&1; then
            wget --output-document="$TEMP_ARCHIVE" "$DISTRIBUTION_URL"
        else
            echo "curl or wget is required to download Gradle." >&2
            exit 1
        fi
        mv "$TEMP_ARCHIVE" "$ARCHIVE"
    fi
    rm -rf "$GRADLE_HOME"
    unzip -q "$ARCHIVE" -d "$DIST_ROOT"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
