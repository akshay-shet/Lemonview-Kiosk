Makeup Advisor — Dataset preparation and training

Overview
- Scripts in this folder prepare public datasets, train a multi-head model (skin-tone classification + makeup color prediction), and export a TFLite model for on-device inference.

Datasets (public)
- CelebA (https://mmlab.ie.cuhk.edu.hk/projects/CelebA.html) — face images for skin tone extraction.
- Makeup in the Wild / Makeup-Transfer datasets (various) — labeled makeup images for product color mapping.
- Optional: FFHQ for additional face diversity.

Steps
1) Install dependencies: see `requirements.txt`.
2) Run `python download_datasets.py` to download selected datasets (manual steps may be required for CelebA login/download).
3) Run `python preprocess.py --input <raw_dir> --output <processed_dir>` to detect faces, crop, and extract skin color labels.
4) Run `python train.py --data <processed_dir> --output ./models` to train a model and export `makeup_advisor.tflite`.

Notes
- Training to 95% requires large labeled datasets and GPU. The scripts include options for transfer learning (MobileNetV3) and data augmentation.
- The generated TFLite model expected outputs (example):
  - `skin_tone_logits` (num_classes)
  - `product_color_indices` (5 ints) or `product_color_rgb` (15 floats)

Android integration
- Place `makeup_advisor.tflite` into `app/src/main/assets/models/`.
- Place `colors.json` (mapping from color class id to hex & name) into `app/src/main/assets/models/`.
- The app will use `MakeupModelInterpreter` to perform inference and generate `MakeupAnalysisResult`.

License
- Ensure dataset licenses are respected when using and distributing derived models.
