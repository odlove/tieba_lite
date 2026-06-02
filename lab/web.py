from __future__ import annotations

from typing import Any

import requests

from lab.config import auth_value


WEB_BASE_URL = "https://tieba.baidu.com"
WEBVIEW_USER_AGENT = (
    "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Version/4.0 Chrome/125.0.0.0 Mobile Safari/537.36"
)


def auth_cookies(config: dict[str, Any]) -> dict[str, str]:
    cookies = {}
    bduss = auth_value(config, "bduss")
    stoken = auth_value(config, "stoken")
    if bduss:
        cookies["BDUSS"] = bduss
    if stoken:
        cookies["STOKEN"] = stoken
    return cookies


def get(path: str, *, params: dict[str, str], config: dict[str, Any]) -> requests.Response:
    return requests.get(
        f"{WEB_BASE_URL}{path}",
        params=params,
        cookies=auth_cookies(config),
        headers={
            "User-Agent": WEBVIEW_USER_AGENT,
            "Referer": "https://tieba.baidu.com/",
        },
        timeout=15,
    )
