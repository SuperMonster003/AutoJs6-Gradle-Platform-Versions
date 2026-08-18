# -*- coding: utf-8 -*-
"""Declares the Android plugin versions a repository needs, ahead of the settings step.

This is the first of the two migration steps. Module scripts apply the Android and
KSP plugins without a version, relying on the settings buildscript classpath to
supply them. That classpath disappears once a repository moves onto the
platform-versions plugin, so the versions have to be stated somewhere else.

They go in the root build script, declared once and not applied:

    plugins {
        id("com.android.application") version System.getProperty("gradle.agp.version") apply false
        id("com.android.library") version System.getProperty("gradle.agp.version") apply false
    }

Module scripts are then left exactly as they are. That matters for more than tidiness:
Gradle's plugins block in Groovy accepts only string literals, so a version computed at
runtime cannot be written there at all, and several repositories carry Groovy modules.
Declaring once in the root serves both dialects, and keeps the version in one place.

Convention plugins (org.autojs.build.*) are untouched throughout: they come from the
included build and never needed a version.

The two steps have to land together. Between them the repository does not build, since
the root script asks for a version the old settings script does not publish. Run this,
then run migrate_downstream.py for the same repository, and only then build. Reverting
works the same way.

Usage:
  py .python/migrate_modules.py --list
  py .python/migrate_modules.py --dry-run
  py .python/migrate_modules.py --apply --repo AutoJs6-Plugin-OpenCC
  py .python/migrate_modules.py --revert --repo AutoJs6-Plugin-OpenCC
"""
import argparse
import re
import shutil
import sys
from pathlib import Path

BACKUP_SUFFIX = ".pre-platform-versions.bak"

SETTINGS_NAME = "settings.gradle.kts"
ROOT_SCRIPT_NAME = "build.gradle.kts"

# Groovy module scripts declare plugins with the same id("...") syntax, so both
# dialects are scanned when working out what a repository needs.
MODULE_SCRIPT_NAMES = ("build.gradle.kts", "build.gradle")

# Marks a repository that still carries the inlined mechanism, matching the
# companion script so both operate on the same set.
MECHANISM_MARKER = "agpVersionMap"

# Marks a repository whose root script already carries the declarations.
DECLARATION_MARKER = 'version System.getProperty("gradle.agp.version")'

# Plugin id to the system property carrying its version.
VERSIONED_PLUGINS = {
    "com.android.application": "gradle.agp.version",
    "com.android.library": "gradle.agp.version",
    "com.google.devtools.ksp": "gradle.ksp.version",
    "org.jetbrains.kotlin.android": "gradle.kotlin.version",
    "org.jetbrains.kotlin.kapt": "gradle.kotlin.version",
    "org.jetbrains.kotlin.plugin.parcelize": "gradle.kotlin.version",
}

# Short aliases that only resolve while the Kotlin plugin sits on the classpath.
# They have no coordinates of their own, so the declaration names the full id.
PLUGIN_ID_ALIASES = {
    "kotlin-android": "org.jetbrains.kotlin.android",
    "kotlin-kapt": "org.jetbrains.kotlin.kapt",
    "kotlin-parcelize": "org.jetbrains.kotlin.plugin.parcelize",
}

# The kotlin("...") helper resolves against the Kotlin plugin, so a repository using
# it needs that plugin declared too.
KOTLIN_HELPER_IDS = {
    "plugin.parcelize": "org.jetbrains.kotlin.plugin.parcelize",
    "kapt": "org.jetbrains.kotlin.kapt",
    "android": "org.jetbrains.kotlin.android",
}

# Directories that never hold a consuming module script.
EXCLUDED_DIRS = {"build-logic", "build", ".gradle", "buildSrc"}

DECLARATION_BLOCK_HEADER = """// @Hint: declared here, applied by the modules.
//  ! These plugins used to arrive through the settings buildscript classpath, which the
//  ! platform-versions plugin replaces. Declaring them once here keeps the version in a
//  ! single place and leaves the module scripts untouched, which Groovy modules require:
//  ! their plugins block accepts string literals only, so a computed version cannot go there.
//  ! zh-CN: 这些插件原先经由 settings buildscript classpath 提供, 现已被 platform-versions 插件取代.
//  ! 在此声明一次可使版本只出现在一处, 且模块脚本无须改动 -- 这对 Groovy 模块是必需的:
//  ! 它们的 plugins 块只接受字符串字面量, 无法写入运行时计算出的版本.
plugins {
"""

DECLARATION_ENTRY = '    id("{plugin_id}") version System.getProperty("{property_name}") apply false\n'


def is_blocked(repo_root: Path):
    """Returns why a repository cannot complete the migration, or None."""
    if (repo_root / "gradle" / "verification-metadata.xml").is_file():
        return "dependency verification"

    for script in sorted(repo_root.rglob("*.gradle.kts")):
        relative = script.relative_to(repo_root)
        if any(part in EXCLUDED_DIRS for part in relative.parts):
            continue
        if script.name in {SETTINGS_NAME, ROOT_SCRIPT_NAME}:
            continue
        try:
            text = script.read_text(encoding="utf-8")
        except OSError:
            continue
        if re.search(r"import com\.android\.|\bandroid\s*\{|ApplicationExtension|LibraryExtension", text):
            return f"AGP-typed fragment: {relative}"
    return None


def find_repositories(workspace: Path):
    """Yields the repositories this tool has a reason to touch."""
    for settings in sorted(workspace.glob(f"*/{SETTINGS_NAME}")):
        text = settings.read_text(encoding="utf-8")
        if MECHANISM_MARKER in text or "platform-versions" in text:
            yield settings.parent


def find_module_scripts(repo_root: Path):
    """Yields the module scripts of a repository, skipping build infrastructure."""
    found = []
    for name in MODULE_SCRIPT_NAMES:
        for script in repo_root.rglob(name):
            if script.parent == repo_root:
                continue
            relative = script.relative_to(repo_root)
            if any(part in EXCLUDED_DIRS for part in relative.parts):
                continue
            found.append(script)
    return sorted(found)


def required_plugins(repo_root: Path):
    """Returns the plugin ids the modules apply without a version, in declaration order."""
    required = {}
    for script in find_module_scripts(repo_root):
        try:
            text = script.read_text(encoding="utf-8")
        except OSError:
            continue
        for line in text.splitlines():
            stripped = line.strip()

            match = re.match(r'^id\("([^"]+)"\)$', stripped)
            if match:
                plugin_id = PLUGIN_ID_ALIASES.get(match.group(1), match.group(1))
                if plugin_id in VERSIONED_PLUGINS:
                    required[plugin_id] = VERSIONED_PLUGINS[plugin_id]
                continue

            match = re.match(r'^kotlin\("([^"]+)"\)$', stripped)
            if match:
                plugin_id = KOTLIN_HELPER_IDS.get(match.group(1))
                if plugin_id:
                    required[plugin_id] = VERSIONED_PLUGINS[plugin_id]

    # A stable order keeps the generated block diffable across repositories.
    return dict(sorted(required.items()))


def build_declaration_block(required):
    entries = "".join(
        DECLARATION_ENTRY.format(plugin_id=plugin_id, property_name=property_name)
        for plugin_id, property_name in required.items()
    )
    return DECLARATION_BLOCK_HEADER + entries + "}\n"


def locate_top_level_block(lines, header_pattern: str):
    """Returns the half-open line range of a top-level block matching the pattern."""
    start = None
    for index, line in enumerate(lines):
        if re.match(header_pattern, line):
            start = index
            break
    if start is None:
        return None

    depth = 0
    for index in range(start, len(lines)):
        segment = lines[index].split("//")[0]
        depth += segment.count("{") - segment.count("}")
        if depth == 0:
            return start, index + 1
    return None


def migrate_root_script(repo_root: Path, required):
    """Returns the new root script text, or None when nothing needs doing."""
    root_script = repo_root / ROOT_SCRIPT_NAME
    if not root_script.is_file():
        return None

    text = root_script.read_text(encoding="utf-8")
    if DECLARATION_MARKER in text:
        return None

    block = build_declaration_block(required)
    lines = text.splitlines(keepends=True)
    span = locate_top_level_block(lines, r"^plugins\s*\{")

    if span is None:
        # No plugins block yet: the declarations lead the file, since a plugins block
        # has to come before everything but imports.
        insert_at = 0
        for index, line in enumerate(lines):
            if line.startswith("import ") or not line.strip():
                insert_at = index + 1
                continue
            break
        return "".join(lines[:insert_at] + [block, "\n"] + lines[insert_at:])

    # An existing block gains the entries just after its opening brace.
    start, end = span
    entries = [
        DECLARATION_ENTRY.format(plugin_id=plugin_id, property_name=property_name)
        for plugin_id, property_name in required.items()
    ]
    merged = lines[:start + 1] + entries + lines[start + 1:]
    return "".join(merged)


def command_list(repos):
    print(f"{len(repos)} repositories in scope:")
    for repo in repos:
        reason = is_blocked(repo)
        if reason is not None:
            print(f"  {repo.name:50} blocked ({reason})")
            continue

        root_script = repo / ROOT_SCRIPT_NAME
        if root_script.is_file() and DECLARATION_MARKER in root_script.read_text(encoding="utf-8"):
            print(f"  {repo.name:50} already declared")
            continue

        required = required_plugins(repo)
        state = ", ".join(required) if required else "nothing to declare"
        print(f"  {repo.name:50} {state}")


def command_migrate(repos, apply: bool):
    touched, blocked = 0, []
    for repo in repos:
        reason = is_blocked(repo)
        if reason is not None:
            blocked.append((repo.name, reason))
            continue

        required = required_plugins(repo)
        if not required:
            continue

        migrated = migrate_root_script(repo, required)
        if migrated is None:
            continue

        print(f"  {repo.name}: {', '.join(required)}")
        if apply:
            root_script = repo / ROOT_SCRIPT_NAME
            backup = root_script.with_suffix(root_script.suffix + BACKUP_SUFFIX)
            if not backup.exists():
                shutil.copy2(root_script, backup)
            root_script.write_text(migrated, encoding="utf-8", newline="\n")
        touched += 1

    verb = "Declared plugin versions in" if apply else "Would declare plugin versions in"
    print(f"\n{verb} {touched} repositories.")

    if blocked:
        print(f"\n{len(blocked)} repositories cannot complete the migration and were left alone:")
        for name, reason in blocked:
            print(f"  {name} ({reason})")
        print(
            "\nThe settings step could never follow for these, so declaring versions here\n"
            "would only leave them unbuildable. migrate_downstream.py explains each case."
        )

    if touched and not apply:
        print("\nRe-run with --apply to write the changes.")
    if touched and apply:
        print(f"Originals kept alongside as *{BACKUP_SUFFIX}; --revert restores them.")
        print("Now run migrate_downstream.py for the same repositories: until their settings")
        print("scripts are rewritten too, the versions asked for here resolve to null.")


def command_revert(repos):
    restored = 0
    for repo in repos:
        backup = (repo / ROOT_SCRIPT_NAME).with_suffix(f".gradle.kts{BACKUP_SUFFIX}")
        if not backup.is_file():
            continue
        shutil.copy2(backup, repo / ROOT_SCRIPT_NAME)
        backup.unlink()
        print(f"  restored {repo.name}/{ROOT_SCRIPT_NAME}")
        restored += 1
    print(f"\nRestored {restored} root scripts.")


def main():
    root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--workspace", type=Path, default=root.parent,
                        help="directory holding the sibling repositories (default: the parent of this repository)")
    parser.add_argument("--repo", action="append", default=[],
                        help="limit to the named repository; repeatable")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--list", action="store_true", help="show what each repository needs")
    mode.add_argument("--dry-run", action="store_true", help="report what would change without writing")
    mode.add_argument("--apply", action="store_true", help="write the changes, keeping a backup per repository")
    mode.add_argument("--revert", action="store_true", help="restore the backups written by --apply")
    args = parser.parse_args()

    workspace = args.workspace.resolve()
    if not workspace.is_dir():
        print(f"No such workspace: {workspace}")
        return 1

    repos = list(find_repositories(workspace))
    if args.repo:
        wanted = set(args.repo)
        repos = [r for r in repos if r.name in wanted]
        for name in sorted(wanted - {r.name for r in repos}):
            print(f"Not found or without the mechanism: {name}")

    # This repository is the plugin itself and must never be rewritten.
    repos = [r for r in repos if r.resolve() != root]

    if not repos:
        print("Nothing to do.")
        return 0

    if args.list:
        command_list(repos)
        return 0
    if args.revert:
        command_revert(repos)
        return 0

    command_migrate(repos, apply=args.apply)
    return 0


if __name__ == "__main__":
    sys.exit(main())
