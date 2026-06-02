from __future__ import annotations

from typing import Any

import requests

from lab import web


def fetch(config: dict[str, Any]) -> requests.Response:
    return web.get(
        "/mo/q/newmoindex",
        params={"need_user": "1"},
        config=config,
    )
