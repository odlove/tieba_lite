from __future__ import annotations

import argparse
import sys
from pathlib import Path

from lab.api import forum_guide, login, my_info
from lab.config import DEFAULT_CONFIG_PATH, load_config
from lab.samples import save_exchange


def command_my_info(args: argparse.Namespace) -> None:
    config = load_config(args.config)
    response = my_info.fetch(config)
    save_exchange("my-info", response)


def command_login(args: argparse.Namespace) -> None:
    config = load_config(args.config)
    response = login.fetch(config)
    save_exchange("login", response)


def command_forum_guide(args: argparse.Namespace) -> None:
    config = load_config(args.config)
    result = forum_guide.fetch_with_request(config)
    summary = result.response_summary()

    save_exchange(
        "forum-guide",
        result.response,
        response_summary=summary,
        request_archive=result.request_archive(),
        extra_artifacts=result.extra_artifacts(),
    )


def add_common_args(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--config", type=Path, default=DEFAULT_CONFIG_PATH, help="Path to config.toml")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Manual Tieba API probe tool")
    subparsers = parser.add_subparsers(dest="command", required=True)

    my_info_parser = subparsers.add_parser("my-info", help="Fetch web my-info page")
    add_common_args(my_info_parser)
    my_info_parser.set_defaults(func=command_my_info)

    login_parser = subparsers.add_parser("login", help="Fetch tbclient login info")
    add_common_args(login_parser)
    login_parser.set_defaults(func=command_login)

    forum_guide_parser = subparsers.add_parser("forum-guide", help="Fetch followed forum list")
    add_common_args(forum_guide_parser)
    forum_guide_parser.set_defaults(func=command_forum_guide)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    args.func(args)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
