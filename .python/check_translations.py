# -*- coding: utf-8 -*-
"""Checks the translation sources before they reach the generator.

Two classes of mistake are easy to make by hand and expensive to spot later:
a missing key, which makes generation fail outright, and a stray fullwidth
punctuation mark in a language whose house style is halfwidth.

Run: py .python/check_translations.py
"""
import json
import sys
from pathlib import Path

from generate_markdown import (
    CHANGELOG_DIR,
    LANGUAGE_CODES,
    LANGUAGE_CODE_DEFAULT,
    README_DIR,
    ROOT,
)

# Languages whose house style keeps CJK sentence punctuation fullwidth.
# Everything else is expected to read as halfwidth, matching the source language.
FULLWIDTH_ALLOWED = {"zh-Hant-TW", "ja"}

# Fullwidth marks that must not appear in a halfwidth language.
# The ideographic comma is exempt: it has no halfwidth counterpart.
FULLWIDTH_MARKS = "，。；：！？（）"

CHANGELOG_LABEL_KEYS = [
    "changelog_label_hint",
    "changelog_label_feature",
    "changelog_label_fix",
    "changelog_label_improvement",
    "changelog_label_dependency",
]


def load(path):
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def walk_strings(value, trail=""):
    """Yields every (path, text) pair inside a nested JSON value."""
    if isinstance(value, dict):
        for k, v in value.items():
            yield from walk_strings(v, f"{trail}.{k}" if trail else k)
    elif isinstance(value, list):
        for i, v in enumerate(value):
            yield from walk_strings(v, f"{trail}[{i}]")
    elif isinstance(value, str):
        yield trail, value


def check_keys(kind, code, reference, actual, errors):
    missing = sorted(set(reference) - set(actual))
    extra = sorted(set(actual) - set(reference))
    if missing:
        errors.append(f"{kind}/{code}: missing keys: {', '.join(missing)}")
    if extra:
        errors.append(f"{kind}/{code}: unexpected keys: {', '.join(extra)}")

    for key, ref_value in reference.items():
        if isinstance(ref_value, list) and isinstance(actual.get(key), list):
            if len(actual[key]) != len(ref_value):
                errors.append(
                    f"{kind}/{code}: '{key}' has {len(actual[key])} items, expected {len(ref_value)}"
                )


def check_punctuation(kind, code, data, errors):
    if code in FULLWIDTH_ALLOWED:
        return
    for trail, text in walk_strings(data):
        found = sorted({ch for ch in text if ch in FULLWIDTH_MARKS})
        if found:
            errors.append(f"{kind}/{code}: fullwidth punctuation {''.join(found)} at '{trail}'")


def main():
    errors = []

    readme_reference = load(README_DIR / f"lang_{LANGUAGE_CODE_DEFAULT}.json")
    changelog_reference = load(CHANGELOG_DIR / f"lang_{LANGUAGE_CODE_DEFAULT}.json")
    reference_versions = list(changelog_reference["$data"].keys())

    for code in LANGUAGE_CODES:
        readme_path = README_DIR / f"lang_{code}.json"
        changelog_path = CHANGELOG_DIR / f"lang_{code}.json"

        for path in (readme_path, changelog_path):
            if not path.is_file():
                errors.append(f"missing file: {path.relative_to(ROOT)}")

        if not readme_path.is_file() or not changelog_path.is_file():
            continue

        readme = load(readme_path)
        changelog = load(changelog_path)

        check_keys("readme", code, readme_reference, readme, errors)
        check_punctuation("readme", code, readme, errors)

        for key in CHANGELOG_LABEL_KEYS:
            if key not in changelog:
                errors.append(f"changelog/{code}: missing label key '{key}'")
        if "$data" not in changelog:
            errors.append(f"changelog/{code}: missing '$data'")
            continue

        versions = list(changelog["$data"].keys())
        if versions != reference_versions:
            errors.append(
                f"changelog/{code}: versions {versions} do not match {reference_versions}"
            )
        for version, item in changelog["$data"].items():
            reference_item = changelog_reference["$data"].get(version)
            if reference_item is None:
                continue
            if item.get("released_date") != reference_item.get("released_date"):
                errors.append(f"changelog/{code}: '{version}' released_date differs")
            for category, ref_entries in reference_item.items():
                if category == "released_date":
                    continue
                entries = item.get(category, [])
                if len(entries) != len(ref_entries):
                    errors.append(
                        f"changelog/{code}: '{version}.{category}' has {len(entries)} items, "
                        f"expected {len(ref_entries)}"
                    )
        check_punctuation("changelog", code, changelog, errors)

    if errors:
        print(f"Found {len(errors)} problem(s):")
        for error in errors:
            print(f"  - {error}")
        return 1

    print(f"All {len(LANGUAGE_CODES)} languages look consistent.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
