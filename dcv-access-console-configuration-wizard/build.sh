#!/usr/bin/env bash
# shellcheck disable=SC1090

set -e

# If VERBOSE is set, or we're running on the build fleet, enable -x.
if [ -n "$VERBOSE" ]
then
    set -x
fi

################ COMMANDS ################
# Execute by running `./build.sh <command>`.
# All others can be command-line tools in the venv path.
# The only required command is `release`, as it's the target the build fleet will execute
# when doing production builds.
# Use double hashes '##' before command definitions so that they will appear when running "help".

## release - Perform a full build.
release() {
    copy_configs

    # Lint the code.
    lint

    # Clean the dist directory.
    rm -rf .tox/.pkg/dist

    # Tox builds the artifacts and tests them so we know we're vending tested code.
    tox_test -v --parallel --parallel-no-spinner

    # This publishes artifacts to the workspace repository so it can either be consumed within
    # the version set or published to CodeArtifact as a part of a pipeline.
#    twine upload .tox/.pkg/dist/*

    copy_supporting_files

    zip_wizard
}

copy_configs() {

  HANDLER_ARTIFACTS="../dcv-access-console-handler/src/main/resources"
  WEBCLIENT_ARTIFACTS="../dcv-access-console-web-client/server"
  cp "$HANDLER_ARTIFACTS/access-console-handler.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
  cp "$HANDLER_ARTIFACTS/access-console-handler-secrets.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
#  cp "$AUTH_SERVER_ARTIFACTS/access-console-auth-server.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
#  cp "$AUTH_SERVER_ARTIFACTS/access-console-auth-server-secrets.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
  cp "$WEBCLIENT_ARTIFACTS/access-console-web-client.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
  cp "$WEBCLIENT_ARTIFACTS/access-console-web-client-secrets.properties" "src/dcv_access_console_config_wizard/assets/default_configs/"
}

copy_supporting_files() {
  cp src/wizard.py build/lib
  cp src/wizard_input.json build/lib
  cp src/onebox_wizard_input.json build/lib
  cp README.md build/lib
  cp requirements.txt build/lib/dcv_access_console_config_wizard
}

zip_wizard() (
  cd build
  rm -rf "wizard"
  rm -f "wizard.zip"

  mkdir "wizard"
  cp -r lib/ "wizard"
  zip -r "wizard.zip" "wizard"
)

## test - Run tests.
tox_test() {
    coverage erase
    tox "$@"
    coverage report --show-missing
    coverage xml && coverage html
}

## lint - Run a linting check.
lint() {
    # Perform type-checking.
#    mypy src test

    # Validate that the source code is properly formatted.
    black --check src test

    # Check for linting issues.
    flake8 src test

    # Validate that imports are sorted.
    isort --check src test
}

## guard - Run the tests in a continuous loop.
guard() {
    # Run the tests in a continuous loop rerunning testing when the source or tests change
    pytest --looponfail "$@"
}

## format - Run code formatters.
format() {
    black src test
    isort src test
}

## venv - Activate the virtual environment.
venv() {
    # Used for doing local development which yields exports you can eval to be in the same
    echo "source .venv/bin/activate"
}

## clean - Clean up build artifacts and cache files.
clean() {
    deactivate
    echo "Cleaning artifacts..."
    rm -rf src/*.egg-info ./**/__pycache__ .coverage .mypy_cache .pytest_cache .venv .tox build
}

## help - List available commands.
help() {
    >&2 echo "Usage: ./build.sh <command>"
    >&2 echo
    >&2 echo "Available commands:"
    >&2 sed -n 's/^## //p' "$0"
}

################# MAIN #################

# Set default command to be 'release' if no arguments are passed.
COMMAND="release"

# First, create and activate a virtual environment and install the library, dev and testing extras.
# This puts a virtual environment under `.venv` so it can be easily referred to and activated.
python -m venv .venv && . .venv/bin/activate && pip install --editable '.[dev,testing]'

# Execute given command with remaining arguments, defaulting the command to release.
if [ $# -gt 0 ]
then
    COMMAND="$1"
    shift

    # Helpful redirect of `test` to `tox_test`.
    if [ "$COMMAND" = "test" ]
    then
        COMMAND="tox_test"
    fi
fi

# Keep the output of "help" clean and easy to read. Otherwise, begin logging all commands.
if [ "$COMMAND" != "help" ]
then
    set -x
fi

$COMMAND "$@"
