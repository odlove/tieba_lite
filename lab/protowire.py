from __future__ import annotations

import struct
from collections.abc import Iterable
from dataclasses import dataclass
from typing import Any


WIRE_VARINT = 0
WIRE_FIXED64 = 1
WIRE_LENGTH_DELIMITED = 2


@dataclass(frozen=True)
class ProtoField:
    number: int
    name: str
    kind: str
    value: int | float | str | list["ProtoField"]


def encode_field(field: ProtoField) -> bytes:
    if field.kind == "varint":
        return field_varint(field.number, int(field.value))
    if field.kind == "double":
        return field_double(field.number, float(field.value))
    if field.kind == "string":
        return field_string(field.number, str(field.value))
    if field.kind == "message":
        if not isinstance(field.value, list):
            raise TypeError(f"message field needs nested fields: {field.name}")
        return field_message(field.number, encode_message(field.value))
    raise ValueError(f"unsupported proto field kind: {field.kind}")


def encode_message(fields: Iterable[ProtoField]) -> bytes:
    return b"".join(encode_field(field) for field in fields)


def fields_to_json(fields: Iterable[ProtoField]) -> list[dict[str, Any]]:
    return [field_to_json(field) for field in fields]


def field_to_json(field: ProtoField) -> dict[str, Any]:
    value: Any = field.value
    if field.kind == "message":
        if not isinstance(value, list):
            raise TypeError(f"message field needs nested fields: {field.name}")
        value = fields_to_json(value)
    return {
        "number": field.number,
        "name": field.name,
        "type": field.kind,
        "value": value,
    }


def encode_varint(value: int) -> bytes:
    output = bytearray()
    while value >= 0x80:
        output.append((value & 0x7F) | 0x80)
        value >>= 7
    output.append(value)
    return bytes(output)


def field_varint(number: int, value: int) -> bytes:
    return encode_varint((number << 3) | WIRE_VARINT) + encode_varint(value)


def field_double(number: int, value: float) -> bytes:
    return encode_varint((number << 3) | WIRE_FIXED64) + struct.pack("<d", value)


def field_string(number: int, value: str) -> bytes:
    data = value.encode()
    return encode_varint((number << 3) | WIRE_LENGTH_DELIMITED) + encode_varint(len(data)) + data


def field_message(number: int, data: bytes) -> bytes:
    return encode_varint((number << 3) | WIRE_LENGTH_DELIMITED) + encode_varint(len(data)) + data


def read_varint(data: bytes, offset: int) -> tuple[int, int]:
    shift = 0
    value = 0
    while True:
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if byte < 0x80:
            return value, offset
        shift += 7


def iter_fields(data: bytes) -> Iterable[tuple[int, int, bytes | int]]:
    offset = 0
    while offset < len(data):
        key, offset = read_varint(data, offset)
        number = key >> 3
        wire_type = key & 0x07
        if wire_type == WIRE_VARINT:
            value, offset = read_varint(data, offset)
            yield number, wire_type, value
        elif wire_type == WIRE_FIXED64:
            value = data[offset : offset + 8]
            offset += 8
            yield number, wire_type, value
        elif wire_type == WIRE_LENGTH_DELIMITED:
            size, offset = read_varint(data, offset)
            value = data[offset : offset + size]
            offset += size
            yield number, wire_type, value
        else:
            raise ValueError(f"unsupported wire type: {wire_type}")


def decode_string(value: bytes | int) -> str:
    if not isinstance(value, bytes):
        return ""
    return value.decode(errors="replace")
