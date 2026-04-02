#!/usr/bin/env python3

import re
from pathlib import Path


RELEASE_NAME = re.compile(r"^v(\d+)\.(\d+)\.(\d+)\.md$")
ENTRY_SEPARATOR = "\n\n\n\n"


def parse_release(path: Path) -> tuple[tuple[int, int, int], str]:
    match = RELEASE_NAME.fullmatch(path.name)
    if match is None:
        raise ValueError(f"unsupported release entry: {path.name}")

    major, minor, patch = match.groups()
    version = int(major), int(minor), int(patch)
    content = path.read_text(encoding="utf-8").strip()
    return version, content


def build_changelog(release_dir: Path) -> str:
    releases: list[tuple[tuple[int, int, int], str]] = []

    for path in release_dir.iterdir():
        if not path.is_file():
            raise ValueError(f"unsupported release entry: {path.name}")
        releases.append(parse_release(path))

    releases.sort(key=lambda release: release[0], reverse=True)
    return ENTRY_SEPARATOR.join(content for _, content in releases if content)


def main() -> None:
    base_dir = Path(__file__).resolve().parent
    release_dir = base_dir / "releases"
    output_file = base_dir / "CHANGELOG.md"
    content = build_changelog(release_dir)
    output_file.write_text(f"{content}\n" if content else "", encoding="utf-8")


if __name__ == "__main__":
    main()
