# -*- coding: utf-8 -*-
"""Migrates downstream repositories onto the platform-versions settings plugin.

Each repository carries its own near-copy of the version decision mechanism inside
the pluginManagement block of settings.gradle.kts. This replaces that block with a
short bootstrap that applies the plugin, and leaves everything outside the block
untouched, since that part holds project-specific module wiring.

The rewrite is deliberately structural rather than textual: the block is located by
its top-level braces, so the many line-count variants across the fleet are all handled
without pattern matching on their contents.

Repositories that declare AGP on the settings buildscript classpath are reported and
skipped, because no settings plugin can supply that classpath in time. Those need their
modules switched to the plugins DSL first, applying the Android and Kotlin plugins with
the versions this plugin publishes as system properties; --force then rewrites them.

Usage:
  py .python/migrate_downstream.py --list
  py .python/migrate_downstream.py --dry-run
  py .python/migrate_downstream.py --apply --repo AutoJs6-Plugin-OpenCC
  py .python/migrate_downstream.py --apply
  py .python/migrate_downstream.py --revert
"""
import argparse
import re
import shutil
import sys
from pathlib import Path

SETTINGS_NAME = "settings.gradle.kts"
BACKUP_SUFFIX = ".pre-platform-versions.bak"

# The marker that tells a migrated repository from an unmigrated one.
PLUGIN_ID = "org.autojs.build.platform-versions"

# Recognizes the mechanism rather than a plain pluginManagement block, so repositories
# that merely declare repositories are left alone.
MECHANISM_MARKER = "agpVersionMap"

# The plugin is applied above includeBuild so the included build sees the system
# properties it publishes; settings plugins are applied before includeBuild is evaluated.
#
# A project that also needs AGP on the settings buildscript classpath cannot be served by
# the plugin alone. Gradle pins the shape down hard: a buildscript block may not precede
# pluginManagement, only one is allowed per script, its classpath cannot be extended once
# resolved, and settings plugins are applied only after that resolution. The inlined
# mechanism escaped all of it by having the decision code in the same block, with nothing
# to resolve first. Such projects keep a buildscript block of their own; --check reports
# them so they can be handled deliberately.
BOOTSTRAP_TEMPLATE = """pluginManagement {{
    repositories {{
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }}
    plugins {{
        id("{plugin_id}") version "{plugin_version}"{extra_plugins}
    }}
}}
"""

# Gradle allows one plugins block per script, so the plugin joins the existing one
# rather than getting a block of its own. It has to be applied before includeBuild,
# which is why that block moves up when it sits below.
ROOT_PLUGINS_TEMPLATE = """plugins {{
    id("{plugin_id}")
}}
"""

ROOT_PLUGIN_ENTRY = """    id("{plugin_id}")
"""

EXTRA_PLUGIN_TEMPLATE = """
        id("{plugin_id}") version "{version}\""""

# A repository whose settings script puts AGP on its own buildscript classpath cannot be
# migrated by this tool; see the note on BOOTSTRAP_TEMPLATE.
AGP_CLASSPATH_MARKER = "com.android.tools.build:gradle"

# Plugins that module scripts currently apply without a version, relying on that
# classpath. Kept in step with migrate_modules.py.
VERSIONED_PLUGIN_IDS = {
    "com.android.application",
    "com.android.library",
    "com.google.devtools.ksp",
    "org.jetbrains.kotlin.android",
}

PLUGIN_GROUP = "org.autojs.build"
PLUGIN_ARTIFACT = "autojs6-gradle-platform-versions"

# Plugins the root plugins block applies without a version, mapped to their
# version catalog key.
ROOT_APPLIED_PLUGINS = {
    "org.gradle.toolchains.foojay-resolver-convention": "foojay-resolver-convention",
}


def find_repositories(workspace: Path):
    """Yields every sibling repository this tool has a reason to touch.

    That means one still carrying the inlined mechanism, or one already migrated,
    since the latter is what --revert and --list need to report on. Matching only
    on the mechanism marker would make a migrated repository invisible, leaving no
    way back.
    """
    for settings in sorted(workspace.glob(f"*/{SETTINGS_NAME}")):
        text = settings.read_text(encoding="utf-8")
        has_backup = settings.with_suffix(settings.suffix + BACKUP_SUFFIX).is_file()
        if MECHANISM_MARKER not in text and PLUGIN_ID not in text and not has_backup:
            continue
        yield settings


def locate_top_level_block(lines, header_pattern: str):
    """Returns the half-open line range of a top-level block matching the pattern.

    Brace depth is tracked so nested blocks do not end the range early. Braces inside
    string literals and comments are ignored, which matters because the mechanism
    prints brace-bearing text.
    """
    start = None
    for index, line in enumerate(lines):
        if re.match(header_pattern, line):
            start = index
            break
    if start is None:
        return None

    depth = 0
    in_block_comment = False
    for index in range(start, len(lines)):
        line = lines[index]
        cleaned = []
        i = 0
        in_string = False
        while i < len(line):
            pair = line[i:i + 2]
            if in_block_comment:
                if pair == "*/":
                    in_block_comment = False
                    i += 2
                    continue
                i += 1
                continue
            if not in_string and pair == "/*":
                in_block_comment = True
                i += 2
                continue
            if not in_string and pair == "//":
                break
            if line[i] == '"' and (i == 0 or line[i - 1] != "\\"):
                in_string = not in_string
            if not in_string:
                cleaned.append(line[i])
            i += 1
        segment = "".join(cleaned)
        depth += segment.count("{") - segment.count("}")
        if depth == 0:
            return start, index + 1
    return None


def read_catalog_versions(repo_root: Path):
    """Reads the [versions] table of the repository's version catalog."""
    catalog = repo_root / "gradle" / "libs.versions.toml"
    if not catalog.is_file():
        return {}

    versions = {}
    in_versions = False
    pattern = re.compile(r'^\s*([A-Za-z0-9._-]+)\s*=\s*"(.*?)"\s*(#.*)?$')
    for raw in catalog.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("["):
            in_versions = line == "[versions]"
            continue
        if not in_versions:
            continue
        match = pattern.match(line)
        if match:
            versions[match.group(1)] = match.group(2)
    return versions


def build_bootstrap(repo_root: Path, plugin_version: str, root_plugins_text: str) -> str:
    """Assembles the bootstrap block, carrying over versions the root block needs."""
    catalog = read_catalog_versions(repo_root)
    extra = ""
    for plugin_id, version_key in ROOT_APPLIED_PLUGINS.items():
        # Only declare it when the root block actually applies it without a version.
        if f'id("{plugin_id}")' not in root_plugins_text:
            continue
        version = catalog.get(version_key)
        if version is None:
            continue
        extra += EXTRA_PLUGIN_TEMPLATE.format(plugin_id=plugin_id, version=version)

    return BOOTSTRAP_TEMPLATE.format(
        plugin_id=PLUGIN_ID,
        plugin_version=plugin_version,
        extra_plugins=extra,
    )


def needs_agp_classpath(text: str) -> bool:
    """Tells whether the settings script puts AGP on its own buildscript classpath."""
    return AGP_CLASSPATH_MARKER in text


def has_unversioned_modules(repo_root: Path) -> bool:
    """Tells whether any module still applies a versioned plugin without a version.

    Such a repository is not ready for this step: its modules would lose the classpath
    that currently supplies those plugins. migrate_modules.py handles them first.
    """
    for script in sorted(repo_root.rglob("build.gradle.kts")):
        if script.parent == repo_root:
            continue
        relative = script.relative_to(repo_root)
        if any(part in {"build-logic", "build", ".gradle", "buildSrc"} for part in relative.parts):
            continue
        for line in script.read_text(encoding="utf-8").splitlines():
            match = re.match(r'^\s*id\("([^"]+)"\)\s*$', line)
            if match and match.group(1) in VERSIONED_PLUGIN_IDS:
                return True
    return False


def migrate_text(text: str, plugin_version: str, repo_root: Path):
    """Returns the migrated settings text, or None when nothing needs doing."""
    if PLUGIN_ID in text:
        return None

    lines = text.splitlines(keepends=True)
    span = locate_top_level_block(lines, r"^pluginManagement\s*\{")
    if span is None:
        return None

    start, end = span
    # Everything outside the block is kept as is, so a version-free plugin
    # application there still needs its version supplied by the bootstrap.
    outside = "".join(lines[:start] + lines[end:])
    bootstrap = build_bootstrap(repo_root, plugin_version, outside)
    remaining = lines[:start] + lines[end:]

    # The root plugins block is lifted out and re-inserted with the bootstrap, since the
    # plugin has to be applied before includeBuild and only one such block is allowed.
    root_plugins_span = locate_top_level_block(remaining, r"^plugins\s*\{")
    if root_plugins_span is None:
        root_plugins = [ROOT_PLUGINS_TEMPLATE.format(plugin_id=PLUGIN_ID), "\n"]
    else:
        plugins_start, plugins_end = root_plugins_span
        block = remaining[plugins_start:plugins_end]
        # Insert the plugin id just after the opening brace, keeping the rest verbatim.
        block = block[:1] + [ROOT_PLUGIN_ENTRY.format(plugin_id=PLUGIN_ID)] + block[1:]
        root_plugins = block + ["\n"]
        remaining = remaining[:plugins_start] + remaining[plugins_end:]
        while plugins_start < len(remaining) and remaining[plugins_start].strip() == "":
            remaining.pop(plugins_start)

    # Both blocks have to precede includeBuild: the included build reads the properties
    # the plugin publishes, and settings plugins are applied before it is evaluated.
    insert_at = next(
        (i for i, line in enumerate(remaining) if re.match(r"^includeBuild\s*\(", line)),
        None,
    )
    if insert_at is None:
        insert_at = min(start, len(remaining))

    return "".join(remaining[:insert_at] + [bootstrap, "\n"] + root_plugins + remaining[insert_at:])


def read_plugin_version(root: Path) -> str:
    version_file = root / "version.properties"
    for line in version_file.read_text(encoding="utf-8").splitlines():
        if line.startswith("VERSION_NAME="):
            return line.split("=", 1)[1].strip()
    raise ValueError(f"VERSION_NAME not found in {version_file}")


def command_list(settings_files):
    print(f"{len(settings_files)} repositories in scope:")
    for settings in settings_files:
        text = settings.read_text(encoding="utf-8")
        if PLUGIN_ID in text:
            state = "migrated"
        elif needs_agp_classpath(text) and has_unversioned_modules(settings.parent):
            state = f"{len(text.splitlines())} lines, run migrate_modules.py first"
        else:
            state = f"{len(text.splitlines())} lines, ready"
        print(f"  {settings.parent.name:55} {state}")


def command_migrate(settings_files, plugin_version: str, apply: bool, force: bool):
    changed, skipped, blocked = 0, 0, []
    for settings in settings_files:
        text = settings.read_text(encoding="utf-8")

        if needs_agp_classpath(text) and has_unversioned_modules(settings.parent) and not force:
            blocked.append(settings.parent.name)
            continue

        migrated = migrate_text(text, plugin_version, settings.parent)
        if migrated is None:
            skipped += 1
            continue

        before = len(text.splitlines())
        after = len(migrated.splitlines())
        print(f"  {settings.parent.name:55} {before} -> {after} lines")

        if apply:
            backup = settings.with_suffix(settings.suffix + BACKUP_SUFFIX)
            if not backup.exists():
                shutil.copy2(settings, backup)
            settings.write_text(migrated, encoding="utf-8", newline="\n")
        changed += 1

    verb = "Migrated" if apply else "Would migrate"
    print(f"\n{verb} {changed} repositories, skipped {skipped}.")

    if blocked:
        print(
            f"\n{len(blocked)} repositories declare AGP on the settings buildscript classpath "
            "and still have unversioned module scripts:"
        )
        for name in blocked:
            print(f"  {name}")
        print(
            "\nNo settings plugin can feed that classpath: it resolves before any settings\n"
            "plugin is applied, only one buildscript block is allowed per script, and it\n"
            "cannot be extended afterwards. The modules have to name their versions instead,\n"
            "reading them from the system properties this plugin publishes.\n"
            "\n"
            "Run migrate_modules.py for these repositories first, then this script again.\n"
            "Note that the pair has to land together: in between, the module scripts ask for\n"
            "a version the old settings script does not publish, and the build fails."
        )

    if changed and not apply:
        print("\nRe-run with --apply to write the changes.")
    if changed and apply:
        print(f"\nOriginals kept alongside as *{BACKUP_SUFFIX}; --revert restores them.")


def command_revert(settings_files):
    restored = 0
    for settings in settings_files:
        backup = settings.with_suffix(settings.suffix + BACKUP_SUFFIX)
        if not backup.exists():
            continue
        shutil.copy2(backup, settings)
        backup.unlink()
        print(f"  restored {settings.parent.name}")
        restored += 1
    print(f"\nRestored {restored} repositories.")


def main():
    root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--workspace", type=Path, default=root.parent,
                        help="directory holding the sibling repositories (default: the parent of this repository)")
    parser.add_argument("--repo", action="append", default=[],
                        help="limit to the named repository; repeatable")
    parser.add_argument("--plugin-version", default=None,
                        help="plugin version to bootstrap (default: VERSION_NAME of this repository)")
    parser.add_argument("--force", action="store_true",
                        help="rewrite even repositories that declare AGP on the settings buildscript classpath")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--list", action="store_true", help="show which repositories carry the mechanism")
    mode.add_argument("--dry-run", action="store_true", help="report what would change without writing")
    mode.add_argument("--apply", action="store_true", help="write the changes, keeping a backup per repository")
    mode.add_argument("--revert", action="store_true", help="restore the backups written by --apply")
    args = parser.parse_args()

    workspace = args.workspace.resolve()
    if not workspace.is_dir():
        print(f"No such workspace: {workspace}")
        return 1

    settings_files = list(find_repositories(workspace))
    if args.repo:
        wanted = set(args.repo)
        settings_files = [s for s in settings_files if s.parent.name in wanted]
        missing = wanted - {s.parent.name for s in settings_files}
        for name in sorted(missing):
            print(f"Not found or without the mechanism: {name}")

    # This repository is the plugin itself and must never be rewritten.
    settings_files = [s for s in settings_files if s.parent.resolve() != root]

    if not settings_files:
        print("Nothing to do.")
        return 0

    if args.list:
        command_list(settings_files)
        return 0
    if args.revert:
        command_revert(settings_files)
        return 0

    plugin_version = args.plugin_version or read_plugin_version(root)
    print(f"Bootstrapping {PLUGIN_ID} version {plugin_version}\n")
    command_migrate(settings_files, plugin_version, apply=args.apply, force=args.force)
    return 0


if __name__ == "__main__":
    sys.exit(main())
