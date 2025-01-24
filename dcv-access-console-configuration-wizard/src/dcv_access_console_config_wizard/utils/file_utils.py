import shutil
import subprocess
from pathlib import Path
from typing import Any

from dcv_access_console_config_wizard.utils import logger

log = logger.get()


def read_plaintext_file_to_lines(path: Path) -> Any:
    log.debug(f"Reading {path}")
    with open(path, "r") as f:
        data = f.readlines()
        f.close()
    return data


def read_plaintext_file_to_string(path: Path) -> Any:
    log.debug(f"Reading {path}")
    with open(path, "r") as f:
        data = f.read()
        f.close()
    return data


def write_lines_to_file(data: Any, path: Path) -> None:
    log.debug(f"Saving to {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w") as f:
        f.truncate(0)
        f.writelines(data)
        f.close()


# Move a file from the source to the destination, and apply the permissions of a reference file
def move_file_with_reference_permissions(
    source_file_path: Path, destination_file_path: Path, reference_file_path: Path
) -> bool:
    if not move_file(source_file_path, destination_file_path):
        return False
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        if (
            subprocess.run(
                [
                    "sudo",
                    "chmod",
                    f"--reference={str(reference_file_path)}",
                    str(destination_file_path),
                ],
                stderr=logging_file,
                stdout=logging_file,
            ).returncode
            != 0
        ):
            log.warning(
                f"Failed to copy permissions from reference file {reference_file_path} to {destination_file_path}"
            )
            return False
        if (
            subprocess.run(
                [
                    "sudo",
                    "chown",
                    f"--reference={str(reference_file_path)}",
                    str(destination_file_path),
                ],
                stderr=logging_file,
                stdout=logging_file,
            ).returncode
            != 0
        ):
            log.warning(
                f"Failed to copy ownership from reference file {reference_file_path} to {destination_file_path}"
            )
            return False

        if shutil.which("chcon") is not None:
            # This is using SELinux, so we also need to copy the SELinux context
            if (
                subprocess.run(
                    [
                        "sudo",
                        "chcon",
                        f"--reference={str(reference_file_path)}",
                        str(destination_file_path),
                    ],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.debug(
                    f"Failed to copy SELinux context from reference file {reference_file_path} to {destination_file_path} "
                )
                return True

    return True


def move_file(source_file_path: Path, destination_file_path: Path):
    destination_file_path.parent.mkdir(parents=True, exist_ok=True)
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.debug(f"Moving {source_file_path} to {destination_file_path}")
        if (
            subprocess.run(
                ["sudo", "mv", str(source_file_path), str(destination_file_path)],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.warning(f"Failed to move {source_file_path} to {destination_file_path}")
            return False
    return True


def set_line(data: Any, line: str, property_name: str, property_value: str) -> None:
    data[data.index(line)] = f"{property_name}={property_value}\n"
