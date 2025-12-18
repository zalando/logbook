#!/usr/bin/env bash
set -euxo pipefail

# Define default commands
COMPILE_CMD="./mvnw compile"
PACKAGE_CMD="./mvnw package -DskipTests -Djacoco.skip=true"
VERIFY_CMD="./mvnw verify -B"
INSTALL_CMD="./mvnw install -DskipTests -Djacoco.skip=true"

# Flags to track selected options
COMPILE=false
NO_TEST_INSTALL=false
PACKAGE=false

# Parse options
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --package|-p) PACKAGE=true ;;
        --compile|-c) COMPILE=true ;;
        --no-test-install|-i) NO_TEST_INSTALL=true ;;
        -ci|-ic) COMPILE=true; NO_TEST_INSTALL=true ;;
        -cp|-pc) PACKAGE=true; COMPILE=true ;;
        -ip|-pi) PACKAGE=true; NO_TEST_INSTALL=true ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

# Execute commands based on the flags
if $PACKAGE; then
    echo "Running package..."
    eval "$PACKAGE_CMD"
fi

if $COMPILE; then
    echo "Running compile..."
    eval "$COMPILE_CMD"
fi

if $NO_TEST_INSTALL; then
    echo "Running install without tests..."
    eval "$INSTALL_CMD"
fi

if ! $COMPILE && ! $NO_TEST_INSTALL && ! $PACKAGE; then
    echo "Running default commands..."
    eval "$VERIFY_CMD"
fi
