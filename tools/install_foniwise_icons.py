#!/usr/bin/env python3
"""Install the eight approved FoniWise Canva PNG assets into Android resources.

Usage:
    python3 tools/install_foniwise_icons.py /path/to/exported/pngs

Expected filenames are documented in docs/foniwise-approved-canva-assets.md.
Only original exported PNGs are accepted; this script does not synthesize icons.
"""
from __future__ import annotations

import argparse
import shutil
import struct
from pathlib import Path

ASSETS = {
    "foniwise_icon_security": (99, 100),
    "foniwise_icon_notifications": (99, 100),
    "foniwise_icon_battery": (99, 99),
    "foniwise_icon_apps": (99, 100),
    "foniwise_icon_cleaning": (99, 100),
    "foniwise_nav_phone": (60, 79),
    "foniwise_nav_learn": (80, 60),
    "foniwise_nav_recommendations": (79, 60),
}
DESTINATION = Path(__file__).resolve().parents[1] / "app/src/main/res/drawable-nodpi"


def png_dimensions(path: Path) -> tuple[int, int]:
    with path.open("rb") as source:
        header = source.read(24)
    if len(header) < 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise ValueError(f"{path.name}: invalid PNG")
    return struct.unpack(">II", header[16:24])


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source_dir", type=Path, help="Folder containing eight original PNG exports")
    args = parser.parse_args()
    source = args.source_dir.resolve()
    errors: list[str] = []
    for name, expected in ASSETS.items():
        path = source / f"{name}.png"
        if not path.is_file():
            errors.append(f"Missing {path.name}")
            continue
        try:
            actual = png_dimensions(path)
            if actual != expected:
                errors.append(f"{path.name}: dimensions {actual}, expected {expected}")
        except ValueError as exc:
            errors.append(str(exc))
    if errors:
        parser.exit(1, "Cannot install assets:\n" + "\n".join(errors) + "\n")
    DESTINATION.mkdir(parents=True, exist_ok=True)
    for name in ASSETS:
        filename = f"{name}.png"
        shutil.copyfile(source / filename, DESTINATION / filename)
        print(f"Installed {filename}")
    print("Eight assets installed. Update MainActivity.kt to use painterResource, then build the APK.")


if __name__ == "__main__":
    main()
