from __future__ import annotations

import datetime as dt
import json
from pathlib import Path
from typing import Any

import requests

from lab.archive import build_request_archive


SAMPLES_DIR = Path("samples")


def sample_timestamp() -> str:
    return dt.datetime.now().astimezone().strftime("%Y%m%dT%H%M%S")


def sample_dir(name: str) -> Path:
    path = SAMPLES_DIR / f"{sample_timestamp()}-{name}"
    path.mkdir(parents=True, exist_ok=False)
    return path


def save_text_file(directory: Path, name: str, text: str) -> Path:
    path = directory / name
    path.write_text(text, encoding="utf-8")
    return path


def save_json_file(directory: Path, name: str, data: Any) -> Path:
    return save_text_file(directory, name, json.dumps(data, ensure_ascii=False, indent=2))


def save_bytes_file(directory: Path, name: str, data: bytes) -> Path:
    path = directory / name
    path.write_bytes(data)
    return path


def save_exchange(
    name: str,
    response: requests.Response,
    *,
    response_summary: dict[str, Any] | None = None,
    extra_artifacts: dict[str, dict[str, Any] | bytes] | None = None,
    request_archive: dict[str, Any] | None = None,
) -> list[Path]:
    directory = sample_dir(name)
    request = request_archive or build_request_archive(response.request)

    paths = [save_json_file(directory, "request.json", request)]
    paths.extend(save_response_files(directory, response, response_summary=response_summary))
    for filename, content in (extra_artifacts or {}).items():
        if isinstance(content, bytes):
            paths.append(save_bytes_file(directory, filename, content))
        else:
            paths.append(save_json_file(directory, filename, content))

    print()
    print(f"saved: {directory}")
    for path in paths:
        print(f"saved: {path}")
    return paths


def save_response_files(
    directory: Path,
    response: requests.Response,
    *,
    response_summary: dict[str, Any] | None,
) -> list[Path]:
    if response_summary is not None:
        return [save_json_file(directory, "response.json", response_summary)]

    try:
        parsed = response.json()
        return [save_json_file(directory, "response.json", parsed)]
    except ValueError:
        return [save_text_file(directory, "response.txt", response.text)]
