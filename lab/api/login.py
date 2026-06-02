from __future__ import annotations

from typing import Any

import requests

from lab import tbclient
from lab.config import auth_value


def fetch(config: dict[str, Any]) -> requests.Response:
    bduss = auth_value(config, "bduss")
    stoken = auth_value(config, "stoken")
    if not bduss:
        raise SystemExit("missing auth.bduss in config.toml")
    if not stoken:
        raise SystemExit("missing auth.stoken in config.toml")

    data = {
        "bdusstoken": f"{bduss}|null",
        "stoken": stoken,
        "user_id": "",
        "channel_id": "",
        "channel_uid": "",
        "authsid": "null",
    }
    data.update(tbclient.common_params())

    return tbclient.post_form("/c/s/login", data=data)
