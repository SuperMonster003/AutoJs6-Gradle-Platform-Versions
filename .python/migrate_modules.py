# -*- coding: utf-8 -*-
"""Prepares module scripts for the settings migration.

This is the first of the two migration steps. Module scripts currently apply the
Android and KSP plugins without a version, relying on the settings buildscript
classpath to supply them. Once a repository moves onto the platform-versions
plugin that classpath is gone, so each application has to name its version, which
it reads from the system properties the plugin publishes.

Convention plugins (org.autojs.build.*) are left alone: they come from the
included build and never needed a version. So is kotlin("plugin.parcelize"),
which is not an id() call; see the caveat below.

Two things this deliberately does not handle:

- kotlin("...") helpers resolve against the Kotlin plugin already on the
  classpath. They keep working while AGP supplies its built-in Kotlin, which
  is the case from AGP 9 onwards, and would need attention only if a project
  moved back to AGP 8.
- Some repositories carry a convention plugin that applies
  org.jetbrains.kotlin.android itself when AGP predates 9. That path is dead
  under AGP 9 and untested under AGP 8 after migration.

The two steps have to land together. Between them the repository does not build:
the module scripts already ask for a version that the old settings script does
not publish, so Gradle rejects the null. Run this, then run migrate_downstream.py
for the same repository, and only then build. Reverting works the same way.

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
MODULE_SCRIPT_NAME = "build.gradle.kts"

# Marks a repository that still carries the inlined mechanism, matching the
# companion script so both operate on the same set.
MECHANISM_MARKER = "agpVersionMap"

# Plugin id to the system property carrying its version.
VERSIONED_PLUGINS = {
    "com.android.application": "gradle.agp.version",
    "com.android.library": "gradle.agp.version",
    "com.google.devtools.ksp": "gradle.ksp.version",
    "org.jetbrains.kotlin.android": "gradle.kotlin.version",
}

# Directories that never hold a consuming module script.
EXCLUDED_DIRS = {"build-logic", "build", ".gradle", "buildSrc"}


def find_repositories(workspace: Path):
    """Yields the repositories this tool has a reason to touch."""
    for settings in sorted(workspace.glob(f"*/{SETTINGS_NAME}")):
        text = settings.read_text(encoding="utf-8")
        if MECHANISM_MARKER in text or "platform-versions" in text:
            yield settings.parent


def find_module_scripts(repo_root: Path):
    """Yields the module scripts of a repository, skipping build infrastructure."""
    for script in sorted(repo_root.rglob(MODULE_SCRIPT_NAME)):
        if script.parent == repo_root:
            continue
        relative = script.relative_to(repo_root)
        if any(part in EXCLUDED_DIRS for part in relative.parts):
            continue
        yield script


def locate_plugins_block(lines):
    """Returns the half-open line range of the top-level plugins block."""
    start = None
    for index, line in enumerate(lines):
        if re.match(r"^plugins\s*\{", line):
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


def rewrite_plugins_block(lines, span):
    """Adds a version to each versionless application of a versioned plugin."""
    start, end = span
    changed = []
    rewritten = list(lines)

    for index in range(start, end):
        line = rewritten[index]
        match = re.match(r'^(\s*)id\("([^"]+)"\)\s*$', line.rstrip("\n"))
        if match is None:
            continue
        indent, plugin_id = match.groups()
        property_name = VERSIONED_PLUGINS.get(plugin_id)
        if property_name is None:
            continue
        newline = "\n" if line.endswith("\n") else ""
        rewritten[index] = (
            f'{indent}id("{plugin_id}") version System.getProperty("{property_name}"){newline}'
        )
        changed.append(plugin_id)

    return rewritten, changed


def migrate_script(script: Path):
    """Returns (new_text, changed_plugin_ids), or (None, []) when nothing to do."""
    text = script.read_text(encoding="utf-8")
    lines = text.splitlines(keepends=True)

    span = locate_plugins_block(lines)
    if span is None:
        return None, []

    rewritten, changed = rewrite_plugins_block(lines, span)
    if not changed:
        return None, []

    return "".join(rewritten), changed


def command_list(repos):
    print(f"{len(repos)} repositories in scope:")
    for repo in repos:
        plugin_ids = set()
        pending = 0
        for script in find_module_scripts(repo):
            _, changed = migrate_script(script)
            if changed:
                pending += 1
                plugin_ids.update(changed)
        if pending:
            state = f"{pending} module script(s): {', '.join(sorted(plugin_ids))}"
        else:
            state = "already versioned"
        print(f"  {repo.name:50} {state}")


def command_migrate(repos, apply: bool):
    touched_repos, touched_scripts = 0, 0
    for repo in repos:
        pending = []
        for script in find_module_scripts(repo):
            migrated, changed = migrate_script(script)
            if migrated is None:
                continue
            pending.append((script, migrated, changed))

        if not pending:
            continue

        print(f"  {repo.name}")
        for script, migrated, changed in pending:
            print(f"    {script.relative_to(repo)}: {', '.join(changed)}")
            if apply:
                backup = script.with_suffix(script.suffix + BACKUP_SUFFIX)
                if not backup.exists():
                    shutil.copy2(script, backup)
                script.write_text(migrated, encoding="utf-8", newline="\n")
            touched_scripts += 1
        touched_repos += 1

    verb = "Updated" if apply else "Would update"
    print(f"\n{verb} {touched_scripts} module scripts across {touched_repos} repositories.")
    if touched_scripts and not apply:
        print("Re-run with --apply to write the changes.")
    if touched_scripts and apply:
        print(f"Originals kept alongside as *{BACKUP_SUFFIX}; --revert restores them.")
        print("Now run migrate_downstream.py for the same repositories: until their settings")
        print("scripts are rewritten too, the versions asked for here resolve to null.")


def command_revert(repos):
    restored = 0
    for repo in repos:
        for backup in sorted(repo.rglob(f"{MODULE_SCRIPT_NAME}{BACKUP_SUFFIX}")):
            # Strip the whole suffix rather than one extension: the backup name ends in
            # ".pre-platform-versions.bak", and with_suffix("") would leave the first half.
            script = backup.with_name(backup.name[: -len(BACKUP_SUFFIX)])
            shutil.copy2(backup, script)
            backup.unlink()
            print(f"  restored {script.relative_to(repo.parent)}")
            restored += 1
    print(f"\nRestored {restored} module scripts.")


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
    mode.add_argument("--apply", action="store_true", help="write the changes, keeping a backup per script")
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
