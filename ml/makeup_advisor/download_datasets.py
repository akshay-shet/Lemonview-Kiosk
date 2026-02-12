"""
Download public datasets for Makeup Advisor POC.
This script downloads a small subset of CelebA as an example and places images in ./raw/
For full datasets (CelebA, Makeup datasets), follow dataset pages and place the data under ./raw/.
"""

import os
import urllib.request
from pathlib import Path

RAW_DIR = Path(__file__).parent / "raw"
RAW_DIR.mkdir(parents=True, exist_ok=True)

SAMPLE_IMAGES = [
    # public sample images used for quick POC (placeholders)
    ("https://upload.wikimedia.org/wikipedia/en/7/7d/Lenna_%28test_image%29.png", "lena.png"),
    ("https://github.com/scikit-image/scikit-image/raw/main/skimage/data/astronaut.png", "astronaut.png"),
]


def download_samples():
    for url, name in SAMPLE_IMAGES:
        dest = RAW_DIR / name
        if dest.exists():
            print(f"Skipping {name}, already exists")
            continue
        print(f"Downloading {url} -> {dest}")
        try:
            urllib.request.urlretrieve(url, dest)
        except Exception as e:
            print(f"Failed to download {url}: {e}")
            continue


if __name__ == '__main__':
    download_samples()
    print("Sample images downloaded to:", RAW_DIR)
