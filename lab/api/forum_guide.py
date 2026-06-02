from __future__ import annotations

import datetime as dt
import time
from dataclasses import dataclass
from typing import Any

import requests

from lab import protowire, tbclient
from lab.archive import build_request_archive
from lab.config import auth_value


FORUM_GUIDE_CMD = 309683
FORUM_GUIDE_PATH = "/c/f/forum/forumGuide"
FORUM_GUIDE_MESSAGE = "ForumGuideReqLite"


@dataclass(frozen=True)
class ForumGuideResult:
    response: requests.Response
    request_proto: dict[str, Any]
    request_body: bytes

    def request_archive(self) -> dict[str, Any]:
        request = build_request_archive(self.response.request)
        request["body"] = build_request_body_archive(body_length=len(self.request_body))
        return request

    def response_summary(self) -> dict[str, Any]:
        return summarize_response(self.response)

    def extra_artifacts(self) -> dict[str, dict[str, Any] | bytes]:
        return {
            "request.proto.json": self.request_proto,
            "request.pb": self.request_body,
            "response.pb": self.response.content,
        }


def fetch(config: dict[str, Any]) -> requests.Response:
    return fetch_with_request(config).response


def fetch_with_request(config: dict[str, Any]) -> ForumGuideResult:
    bduss = auth_value(config, "bduss")
    stoken = auth_value(config, "stoken")
    if not bduss:
        raise SystemExit("missing auth.bduss in config.toml")
    if not stoken:
        raise SystemExit("missing auth.stoken in config.toml")

    fields = build_request_fields(bduss=bduss, stoken=stoken)
    body = protowire.encode_message(fields)
    response = tbclient.post_protobuf_multipart(
        FORUM_GUIDE_PATH,
        cmd=FORUM_GUIDE_CMD,
        data=body,
    )
    return ForumGuideResult(
        response=response,
        request_proto=build_request_proto_archive(fields),
        request_body=body,
    )


def build_request_body(bduss: str, stoken: str) -> bytes:
    return protowire.encode_message(build_request_fields(bduss=bduss, stoken=stoken))


def build_request_fields(bduss: str, stoken: str) -> list[protowire.ProtoField]:
    timestamp = int(time.time() * 1000)
    common = [
        protowire.ProtoField(1, "_client_type", "varint", 2),
        protowire.ProtoField(2, "_client_version", "string", tbclient.TBCLIENT_VERSION),
        protowire.ProtoField(3, "_client_id", "string", tbclient.CLIENT_ID),
        protowire.ProtoField(5, "_phone_imei", "string", ""),
        protowire.ProtoField(6, "from", "string", "tieba"),
        protowire.ProtoField(7, "cuid", "string", tbclient.CUID),
        protowire.ProtoField(8, "_timestamp", "varint", timestamp),
        protowire.ProtoField(9, "model", "string", tbclient.MODEL),
        protowire.ProtoField(10, "BDUSS", "string", bduss),
        protowire.ProtoField(12, "net_type", "varint", 1),
        protowire.ProtoField(14, "_phone_newimei", "string", ""),
        protowire.ProtoField(24, "pversion", "string", ""),
        protowire.ProtoField(25, "_os_version", "string", tbclient.OS_VERSION),
        protowire.ProtoField(26, "brand", "string", ""),
        protowire.ProtoField(28, "lego_lib_version", "string", ""),
        protowire.ProtoField(30, "stoken", "string", stoken),
        protowire.ProtoField(32, "cuid_galaxy2", "string", tbclient.CUID),
        protowire.ProtoField(33, "cuid_gid", "string", ""),
        protowire.ProtoField(34, "oaid", "string", ""),
        protowire.ProtoField(35, "c3_aid", "string", tbclient.C3_AID),
        protowire.ProtoField(37, "scr_w", "varint", 1080),
        protowire.ProtoField(38, "scr_h", "varint", 2400),
        protowire.ProtoField(39, "scr_dip", "double", 3.0),
        protowire.ProtoField(40, "q_type", "varint", 0),
        protowire.ProtoField(41, "is_teenager", "varint", 0),
        protowire.ProtoField(42, "sdk_ver", "string", ""),
        protowire.ProtoField(43, "framework_ver", "string", ""),
        protowire.ProtoField(44, "naws_game_ver", "string", ""),
        protowire.ProtoField(49, "active_timestamp", "varint", timestamp),
        protowire.ProtoField(50, "first_install_time", "varint", 0),
        protowire.ProtoField(51, "last_update_time", "varint", 0),
        protowire.ProtoField(53, "event_day", "string", event_day(timestamp)),
        protowire.ProtoField(54, "android_id", "string", ""),
        protowire.ProtoField(55, "cmode", "varint", 1),
        protowire.ProtoField(56, "start_scheme", "string", ""),
        protowire.ProtoField(57, "start_type", "varint", 0),
        protowire.ProtoField(59, "mac", "string", "02:00:00:00:00:00"),
        protowire.ProtoField(62, "user_agent", "string", tbclient.TBCLIENT_USER_AGENT),
        protowire.ProtoField(63, "personalized_rec_switch", "varint", 1),
        protowire.ProtoField(70, "device_score", "string", ""),
    ]
    request_data = [
        protowire.ProtoField(1, "common", "message", common),
        protowire.ProtoField(2, "sort_type", "varint", 3),
        protowire.ProtoField(3, "call_from", "varint", 4),
    ]
    return [protowire.ProtoField(1, "data", "message", request_data)]


def build_request_body_archive(
    body_length: int,
) -> dict[str, Any]:
    return {
        "type": "multipart/form-data",
        "parts": [
            {
                "name": "data",
                "filename": "file",
                "content_type": "application/octet-stream",
                "proto": FORUM_GUIDE_MESSAGE,
                "length": body_length,
                "fields_file": "request.proto.json",
                "raw_file": "request.pb",
            },
        ],
    }


def build_request_proto_archive(fields: list[protowire.ProtoField]) -> dict[str, Any]:
    return {
        "message": FORUM_GUIDE_MESSAGE,
        "fields": protowire.fields_to_json(fields),
    }


def summarize_response(response: requests.Response) -> dict[str, Any]:
    parsed = parse_response(response.content)
    return {
        "format": "protobuf-summary",
        "raw_file": "response.pb",
        "status_code": response.status_code,
        "content_type": response.headers.get("content-type", ""),
        "length": len(response.content),
        **parsed,
    }


def parse_response(data: bytes) -> dict[str, Any]:
    error: dict[str, Any] = {}
    result: dict[str, Any] = {"like_forum": []}
    for number, _, value in protowire.iter_fields(data):
        if number == 1 and isinstance(value, bytes):
            error = parse_error(value)
        elif number == 2 and isinstance(value, bytes):
            result.update(parse_data(value))
    return {
        "error": error,
        "data": result,
    }


def parse_error(data: bytes) -> dict[str, Any]:
    error: dict[str, Any] = {}
    for number, _, value in protowire.iter_fields(data):
        if number == 1 and isinstance(value, int):
            error["errorno"] = value
        elif number == 2:
            error["errmsg"] = protowire.decode_string(value)
        elif number == 3:
            error["usermsg"] = protowire.decode_string(value)
    return error


def parse_data(data: bytes) -> dict[str, Any]:
    result: dict[str, Any] = {"like_forum": []}
    for number, _, value in protowire.iter_fields(data):
        if number == 2 and isinstance(value, bytes):
            result["like_forum"].append(parse_like_forum(value))
        elif number == 4 and isinstance(value, int):
            result["is_login"] = value
        elif number == 5 and isinstance(value, int):
            result["msign_valid"] = value
        elif number == 6:
            result["msign_text"] = protowire.decode_string(value)
        elif number == 7 and isinstance(value, int):
            result["msign_level"] = value
    return result


def parse_like_forum(data: bytes) -> dict[str, Any]:
    forum: dict[str, Any] = {}
    for number, _, value in protowire.iter_fields(data):
        if number == 1 and isinstance(value, int):
            forum["forum_id"] = value
        elif number == 2:
            forum["forum_name"] = protowire.decode_string(value)
        elif number == 3:
            forum["avatar"] = protowire.decode_string(value)
        elif number == 4 and isinstance(value, int):
            forum["hot_num"] = value
        elif number == 10 and isinstance(value, int):
            forum["level_id"] = value
        elif number == 11:
            forum["level_name"] = protowire.decode_string(value)
        elif number == 12 and isinstance(value, int):
            forum["is_sign"] = value
    return forum


def event_day(timestamp_millis: int) -> str:
    date = dt.datetime.fromtimestamp(timestamp_millis / 1000)
    return f"{date.year}{date.month}{date.day:02d}"
