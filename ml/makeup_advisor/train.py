"""
Training script for Makeup Advisor POC.
This script trains a small MobileNetV3-based classifier to predict a simplified skin-tone class and per-product color classes.
For a full production-grade model, expand datasets and training time, and validate carefully.
"""
import tensorflow as tf
from tensorflow.keras import layers, models
import numpy as np
import pandas as pd
from pathlib import Path
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--data', required=False, default='processed', help='Processed images folder')
parser.add_argument('--output', required=False, default='models', help='Output folder')
args = parser.parse_args()

DATA = Path(args.data)
OUT = Path(args.output)
OUT.mkdir(parents=True, exist_ok=True)

labels_csv = DATA / 'labels.csv'
if not labels_csv.exists():
    raise SystemExit('Run preprocess.py first to generate labels.csv')

df = pd.read_csv(labels_csv)
# For POC, create synthetic classes based on brightness (y)

def rgb_to_y(r,g,b):
    return 0.299*r + 0.587*g + 0.114*b

df['y'] = df.apply(lambda r: rgb_to_y(r.avg_r, r.avg_g, r.avg_b), axis=1)
# Bucket into 3 classes: Fair, Medium, Deep
bins = [0, 130, 170, 255]
labels = ['Deep','Medium','Fair']
df['skin_class'] = pd.cut(df['y'], bins=bins, labels=labels)

# Prepare dataset
from tensorflow.keras.preprocessing.image import ImageDataGenerator

train_datagen = ImageDataGenerator(rescale=1./255, validation_split=0.2,
                                   horizontal_flip=True, rotation_range=10)

train_generator = train_datagen.flow_from_dataframe(
    df, directory=DATA.as_posix(), x_col='image', y_col='skin_class',
    target_size=(224,224), class_mode='categorical', subset='training', batch_size=8
)

val_generator = train_datagen.flow_from_dataframe(
    df, directory=DATA.as_posix(), x_col='image', y_col='skin_class',
    target_size=(224,224), class_mode='categorical', subset='validation', batch_size=8
)

base = tf.keras.applications.MobileNetV3Small(input_shape=(224,224,3), include_top=False, weights='imagenet', pooling='avg')
for layer in base.layers:
    layer.trainable = False

x = base.output
x = layers.Dropout(0.3)(x)
x = layers.Dense(128, activation='relu')(x)
outputs = layers.Dense(train_generator.num_classes, activation='softmax')(x)
model = models.Model(inputs=base.input, outputs=outputs)
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

model.fit(train_generator, validation_data=val_generator, epochs=6)

# Save Keras and convert to TFLite
model.save(OUT / 'skin_tone_classifier.h5')

# TFLite conversion (float32). For on-device, consider quantization.
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
(OUT / 'makeup_advisor.tflite').write_bytes(tflite_model)
print('Saved TFLite to', OUT / 'makeup_advisor.tflite')
