"""Generate review-only FoniWise launcher PNG sizes from the approved rounded preview.

Usage: python3 tools/generate_foniwise_icon_previews.py /path/to/approved-rounded.png OUTPUT_DIR
Requires Pillow. Does not modify AndroidManifest.xml or existing app icons.
"""
from pathlib import Path
from PIL import Image
import sys

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python3 tools/generate_foniwise_icon_previews.py SOURCE.png OUTPUT_DIR")
    source, output = Path(sys.argv[1]), Path(sys.argv[2])
    if not source.is_file():
        raise SystemExit(f"Source not found: {source}")
    image = Image.open(source).convert("RGBA")
    if image.size != (512, 512):
        raise SystemExit(f"Expected approved 512x512 preview, got {image.size}")
    for density, size in SIZES.items():
        target = output / f"mipmap-{density}" / "foniwise_launcher_preview.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        image.resize((size, size), Image.Resampling.LANCZOS).save(target)
        print(f"{density}: {size}x{size} -> {target}")


if __name__ == "__main__":
    main()
