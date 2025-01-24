import logging
import random
import string
import sys
from pathlib import Path

from dcv_access_console_config_wizard.constants.constants import PRODUCT_NAME

# Create a random 10 digit alphanumeric string for the logging directory
logging_temp_directory = "".join(
    random.choice(string.ascii_uppercase + string.digits) for _ in range(10)
)

DEFAULT_LOG_LEVEL = logging.INFO
DEFAULT_LOGGER_NAME = "DCV Configuration Wizard"
DEFAULT_LOGGER_FILE_HANDLER_NAME = "file_handler"
DEFAULT_FORMAT = f"[{logging_temp_directory}] %(levelname)s :  %(message)s "
DEFAULT_LOG_FOLDER = Path("./output/logs")
DEFAULT_LOG_FILE = Path(DEFAULT_LOG_FOLDER / logging_temp_directory / "logs.txt")


def set_logging_path(logging_path=None):
    logger = logging.getLogger(DEFAULT_LOGGER_NAME)
    if not logging_path:
        logging_path = DEFAULT_LOG_FILE
    try:
        # Create the logging file
        Path(logging_path).parent.mkdir(parents=True, exist_ok=True)
        print(
            f"\nStarted the {PRODUCT_NAME} Wizard. Full logs can be found at {logging_path.resolve()}\n"
        )

        # Create another handler that will store all the logs in the logging file
        file_handler = logging.FileHandler(logging_path)
        file_handler.setLevel(logging.DEBUG)
        file_handler.setFormatter(formatter)
        file_handler.set_name(DEFAULT_LOGGER_FILE_HANDLER_NAME)

        logger.addHandler(file_handler)
    except PermissionError:
        logger.error(
            "Unable to create file for logging due to a permissions error. Full logs will not be saved"
        )


def get():
    return logging.getLogger(DEFAULT_LOGGER_NAME)


formatter = logging.Formatter(DEFAULT_FORMAT)

# Create a handler to log to the console
stdout_handler = logging.StreamHandler(sys.stdout)
stdout_handler.setLevel(DEFAULT_LOG_LEVEL)
stdout_handler.setFormatter(formatter)

get().addHandler(stdout_handler)


# We need to set the logging level for the entire logger to the lowest level, otherwise the file won't get the full logs
get().setLevel(logging.DEBUG)


def set_debug_level():
    stdout_handler.setLevel(logging.DEBUG)


def get_verbose_logging_file():
    for handler in get().handlers:
        if handler.name == DEFAULT_LOGGER_FILE_HANDLER_NAME:
            return handler.baseFilename