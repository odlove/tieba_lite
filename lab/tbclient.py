from __future__ import annotations

import hashlib
import random
import time
import uuid

import requests


TBCLIENT_BASE_URL = "https://tiebac.baidu.com"
TBCLIENT_VERSION = "12.41.7.1"
TBCLIENT_SIGN_SALT = "tiebaclient!!!"
TBCLIENT_USER_AGENT = f"bdtb for Android {TBCLIENT_VERSION}"
CLIENT_ID = f"wappc_{int(time.time() * 1000)}_{random.randint(0, 1000)}"
CUID = uuid.uuid4().hex
C3_AID = uuid.uuid4().hex
MODEL = "Android"
OS_VERSION = "35"


def common_params() -> dict[str, str]:
    return {
        "_client_id": CLIENT_ID,
        "_client_type": "2",
        "_client_version": TBCLIENT_VERSION,
        "_os_version": OS_VERSION,
        "_timestamp": str(int(time.time() * 1000)),
        "model": MODEL,
        "net_type": "1",
        "phone_imei": "",
    }


def sign_form(data: dict[str, str]) -> dict[str, str]:
    sorted_raw = "".join(f"{key}={value}" for key, value in sorted(data.items()))
    signed = dict(data)
    signed["sign"] = hashlib.md5((sorted_raw + TBCLIENT_SIGN_SALT).encode()).hexdigest()
    return signed


def post_form(path: str, *, data: dict[str, str]) -> requests.Response:
    return post_form_unsigned(path, data=sign_form(data))


def post_form_unsigned(path: str, *, data: dict[str, str]) -> requests.Response:
    return requests.post(
        f"{TBCLIENT_BASE_URL}{path}",
        data=data,
        headers={
            "User-Agent": TBCLIENT_USER_AGENT,
            "Cookie": "ka=open",
        },
        timeout=15,
    )


def post_protobuf_multipart(
    path: str,
    *,
    cmd: int,
    data: bytes,
    extra_headers: dict[str, str] | None = None,
    extra_parts: dict[str, str] | None = None,
) -> requests.Response:
    headers = protobuf_multipart_headers()
    if extra_headers:
        headers.update(extra_headers)

    files = {"data": ("file", data, "application/octet-stream")}
    return requests.post(
        f"{TBCLIENT_BASE_URL}{path}",
        params={"cmd": str(cmd), "format": "protobuf"},
        data=extra_parts or {},
        files=files,
        headers=headers,
        timeout=15,
    )


def build_cookie() -> str:
    return f"ka=open;CUID={CUID};TBBRAND={MODEL};"


def protobuf_multipart_headers() -> dict[str, str]:
    return {
        "Charset": "UTF-8",
        "client_type": "2",
        "cookie": build_cookie(),
        "cuid": CUID,
        "cuid_galaxy2": CUID,
        "cuid_gid": "",
        "c3_aid": C3_AID,
        "User-Agent": TBCLIENT_USER_AGENT,
        "x_bd_data_type": "protobuf",
    }
