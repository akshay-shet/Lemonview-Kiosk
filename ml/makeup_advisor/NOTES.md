Training & evaluation plan to reach high accuracy

1) Data
- Gather large labeled datasets: CelebA (face attributes) + Makeup datasets (e.g., Makeup in the Wild, BeautyGAN datasets) + FFHQ for diversity.
- Create label schema: skin_tone buckets (Fair, Light, Medium, Tan, Deep) and product color classes (palette indices) for foundation, blush, eye shadow, eyeliner, lipstick.
- Clean labels, remove low-quality images, and balance classes via augmentation.

2) Preprocessing
- Face detection + alignment (MTCNN or dlib) and crop central face.
- Normalize colors using color constancy (gray-world / histogram matching) to reduce lighting effects.

3) Model
- Multi-head network with MobileNetV3Small (or EfficientNet-Lite for on-device) backbone.
- Heads:
  - Skin-tone classification (softmax)
  - Product color classification heads (softmax per product) or regression (RGB values) depending on label availability.
- Loss: categorical crossentropy per head; weight heads to emphasize skin-tone accuracy.

4) Training & Validation
- Use stratified K-fold cross validation.
- Use heavy augmentations: brightness/contrast, color jitter, gaussian blur.
- Early stopping and learning rate scheduling.
- Fine-tune backbone after head warmup.

5) Evaluation
- Report per-class precision/recall and confusion matrices.
- If target is 95% accuracy, narrow class granularity (fewer classes) or increase dataset size.

6) Export to TFLite
- Export float32 TFLite first, validate.
- Apply post-training quantization (float16 or integer) and validate on-device.

7) Integration
- Add `makeup_advisor.tflite` and `colors.json` into `app/src/main/assets/models/`.
- App uses `MakeupModelInterpreter` automatically when TFLite exists.

8) Continuous improvement
- Add an in-app feedback loop where users can confirm/adjust predicted skin-tone and color (opt-in). Use these corrections to fine-tune the model periodically.

Tips to reach 95%:
- Ensure high-quality, balanced labels and consider hierarchical classification (coarse -> fine).
- Use ensembling (averaging predictions from models trained on different augmentations) and calibration.
- Prefer per-product classifiers rather than regressing RGB directly unless you have high-quality paired makeup labels.
