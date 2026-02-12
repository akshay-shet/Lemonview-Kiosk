"""
Preprocessing: face detection and crop using MTCNN, skin color extraction, and creating CSV labels.
"""
from mtcnn import MTCNN
from PIL import Image
import numpy as np
from pathlib import Path
import argparse
import csv

parser = argparse.ArgumentParser()
parser.add_argument('--input', required=False, default='raw', help='Raw images folder')
parser.add_argument('--output', required=False, default='processed', help='Processed output folder')
args = parser.parse_args()

INPUT = Path(args.input)
OUTPUT = Path(args.output)
OUTPUT.mkdir(parents=True, exist_ok=True)

detector = MTCNN()

with open(OUTPUT / 'labels.csv', 'w', newline='', encoding='utf-8') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['image','face_x','face_y','face_w','face_h','avg_r','avg_g','avg_b'])

    for img_path in INPUT.glob('*.*'):
        try:
            img = Image.open(img_path).convert('RGB')
            arr = np.array(img)
            results = detector.detect_faces(arr)
            if len(results) == 0:
                print('No face:', img_path)
                continue
            # pick largest face
            face = max(results, key=lambda r: r['box'][2]*r['box'][3])
            x, y, w, h = face['box']
            x = max(0, x); y = max(0,y)
            crop = arr[y:y+h, x:x+w]
            # sample center region to get skin color
            ch = crop[h//4: 3*h//4, w//4: 3*w//4]
            avg = ch.mean(axis=(0,1)).astype(int)
            Image.fromarray(crop).resize((224,224)).save(OUTPUT / img_path.name)
            writer.writerow([img_path.name,x,y,w,h,avg[0],avg[1],avg[2]])
            print('Processed', img_path.name, 'avg', avg)
        except Exception as e:
            print('Error', img_path, e)
