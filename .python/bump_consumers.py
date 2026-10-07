# -*- coding: utf-8 -*-
"""Bumps every sibling consumer of the shared Gradle plugins to this repository's version.

Consumers pin both plugin ids in the `pluginManagement.plugins` block of their
settings.gradle.kts and nothing updates that pin automatically, so releases lag
behind in the fleet. This walks the sibling repositories (the parent directory of
this checkout, or --parent), rewrites the pinned version of

  io.github.supermonster003.autojs6-platform-versions
  io.github.supermonster003.autojs6-native-alignment

to VERSION_NAME from version.properties (or --version), and can commit each change
on its own as `build: upgrade shared Gradle plugins to <version>`. Only the settings
script is touched and staged; nothing else in a consumer's working tree is swept
into the commit.

Run it after the version has been published, otherwise the consumers cannot resolve
it (an unpublished version can still be tested through the
`autojs.buildPlugins.includeBuild` property that every consumer honours).

Usage:
  py .python/bump_consumers.py --list
  py .python/bump_consumers.py --dry-run
  py .python/bump_consumers.py --apply
  py .python/bump_consumers.py --apply --commit
  py .python/bump_consumers.py --apply --repo AutoJs6-Plugin-NodeJs-Runtime
"""
import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PLUGIN_IDS = (
    "io.github.supermonster003.autojs6-platform-versions",
    "io.github.supermonster003.autojs6-native-alignment",
)
PIN_PATTERN = re.compile(
    r'(id\("(?P<id>' + "|".join(re.escape(i) for i in PLUGIN_IDS) + r')"\)\s*version\s*")(?P<version>[^"]+)(")'
)


def current_version() -> str:
    for line in (ROOT / "version.properties").read_text(encoding="utf-8").splitlines():
        if line.startswith("VERSION_NAME="):
            return line.split("=", 1)[1].strip()
    raise SystemExit("VERSION_NAME is missing from version.properties")


def consumers(parent: Path, only: str | None):
    for repo in sorted(parent.iterdir()):
        if not repo.is_dir() or repo.resolve() == ROOT.resolve():
            continue
        if only and repo.name != only:
            continue
        settings = repo / "settings.gradle.kts"
        if not settings.is_file():
            continue
        text = settings.read_text(encoding="utf-8")
        pins = {m.group("id"): m.group("version") for m in PIN_PATTERN.finditer(text)}
        if pins:
            yield repo, settings, text, pins


def rewrite(text: str, version: str) -> str:
    return PIN_PATTERN.sub(lambda m: f'{m.group(1)}{version}{m.group(4)}', text)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--parent", type=Path, default=ROOT.parent, help="directory holding the consumer repositories")
    parser.add_argument("--version", help="target version (default: VERSION_NAME of this checkout)")
    parser.add_argument("--repo", help="limit to one consumer directory name")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--list", action="store_true", help="show consumers and their pinned versions")
    mode.add_argument("--dry-run", action="store_true", help="show what would change (default)")
    mode.add_argument("--apply", action="store_true", help="rewrite the settings scripts")
    parser.add_argument("--commit", action="store_true", help="with --apply: commit each rewritten settings script")
    args = parser.parse_args()

    version = args.version or current_version()
    changed = 0
    for repo, settings, text, pins in consumers(args.parent, args.repo):
        stale = {plugin: pinned for plugin, pinned in pins.items() if pinned != version}
        if args.list:
            print(f"{repo.name}: " + ", ".join(f"{plugin.rsplit('.', 1)[1]}={pinned}" for plugin, pinned in pins.items()))
            continue
        if not stale:
            continue
        changed += 1
        summary = ", ".join(f"{plugin.rsplit('.', 1)[1]} {pinned} -> {version}" for plugin, pinned in stale.items())
        if not args.apply:
            print(f"would update {repo.name}: {summary}")
            continue
        settings.write_text(rewrite(text, version), encoding="utf-8", newline="\n" if "\r\n" not in text else "\r\n")
        print(f"updated {repo.name}: {summary}")
        if args.commit:
            # Only the settings script is staged, so a consumer's unrelated uncommitted work stays out of
            # the commit; a repository that is not a git work tree is reported and left modified.
            try:
                subprocess.run(["git", "-C", str(repo), "add", "--", "settings.gradle.kts"], check=True, capture_output=True)
                subprocess.run(["git", "-C", str(repo), "commit", "-q", "-m", f"build: upgrade shared Gradle plugins to {version}"], check=True, capture_output=True)
                print(f"committed {repo.name}")
            except subprocess.CalledProcessError as error:
                detail = (error.stderr or b"").decode("utf-8", "replace").strip().splitlines()
                print(f"NOT committed {repo.name}: {detail[-1] if detail else error}")
    if not args.list:
        print(f"{changed} consumer(s) {'updated' if args.apply else 'to update'} for {version}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
