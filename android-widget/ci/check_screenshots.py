from pathlib import Path
import sys

from PIL import Image

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else "smoke")
CHECKS = [
    "00-edit-cold.png",
    "03-week.png",
    "04-before.png",
    "05-active.png",
    "06-lunch.png",
    "07-gap.png",
    "08-after.png",
]

failed = []
for name in CHECKS:
    path = ROOT / name
    if not path.exists():
        failed.append(f"missing:{name}")
        continue
    image = Image.open(path).convert("RGB").resize((270, 480))
    pixels = list(image.getdata())
    dark = sum(1 for r, g, b in pixels if r + g + b < 600) / len(pixels)
    chroma = sum(1 for r, g, b in pixels if max(r, g, b) - min(r, g, b) > 20) / len(pixels)
    print(f"{name}: dark={dark:.4f} chroma={chroma:.4f}")
    if dark < 0.01 and chroma < 0.01:
        failed.append(f"blank:{name}")

if failed:
    raise SystemExit("Invalid timetable screenshots: " + ", ".join(failed))
