from __future__ import annotations

import tomllib
from pathlib import Path
from typing import Any


DEFAULT_CONFIG_PATH = Path("config.toml")


def load_config(path: Path) -> dict[str, Any]:
    if not path.exists():
        raise SystemExit(f"config file not found: {path}")
    with path.open("rb") as file:
        return tomllib.load(file)


def auth_value(config: dict[str, Any], name: str) -> str:
    auth = config.get("auth", {})
    return str(auth.get(name, "")).strip()
