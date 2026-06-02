from __future__ import annotations

from typing import Any
from urllib.parse import parse_qsl, urlsplit

import requests


def build_body_archive(request: requests.PreparedRequest) -> dict[str, Any] | None:
    body = request.body
    if body is None:
        return None

    if isinstance(body, bytes):
        try:
            text = body.decode()
        except UnicodeDecodeError:
            return {
                "type": "bytes",
                "length": len(body),
            }
    else:
        text = str(body)

    content_type = request.headers.get("Content-Type", "")
    if "application/x-www-form-urlencoded" in content_type:
        return {
            "type": "application/x-www-form-urlencoded",
            "text": text,
            "form": parse_qsl(text, keep_blank_values=True),
        }

    return {
        "type": "text",
        "text": text,
    }


def build_request_archive(request: requests.PreparedRequest) -> dict[str, Any]:
    url = urlsplit(request.url or "")
    archive: dict[str, Any] = {
        "method": request.method,
        "url": f"{url.scheme}://{url.netloc}{url.path}",
        "query": parse_qsl(url.query, keep_blank_values=True),
        "headers": dict(request.headers),
    }
    archive["body"] = build_body_archive(request)
    return archive
