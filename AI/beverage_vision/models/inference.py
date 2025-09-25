from PIL import Image
import torch, math
from torchvision import transforms

from beverage_vision.models.loader import get_models
from beverage_vision.config import CONF_THR, IOU_THR, MIN_BOX, MIN_FINAL

tf_eval = transforms.Compose([
    transforms.Resize((224,224)),
    transforms.ToTensor(),
    transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225]),
])

def _to_bottom_left_y(y_top: int, h: int, H: int) -> int:
    return H - (y_top + h)

def detect_and_classify(pil_img: Image.Image):
    detector, cls_net, P, labels, TAU, device = get_models()
    W, H = pil_img.size

    r = detector.predict(source=pil_img, conf=CONF_THR, iou=IOU_THR, max_det=100, imgsz=1280, verbose=False)[0]
    boxes = r.boxes.xyxy.tolist() if r.boxes is not None else []
    detcs = r.boxes.conf.tolist() if r.boxes is not None else []

    crops, keep = [], []
    for i, b in enumerate(boxes):
        x1,y1,x2,y2 = map(int, b)
        w,h = x2-x1, y2-y1
        if w < MIN_BOX or h < MIN_BOX:
            continue
        crops.append(tf_eval(pil_img.crop((x1,y1,x2,y2))))
        keep.append(i)

    results = []
    if not crops:
        return results, (W,H)

    X = torch.stack(crops,0).to(device)
    with torch.inference_mode():
        Z = cls_net(X)
        S = Z @ P.T
        smax, idx = S.max(dim=1)

    for j, i in enumerate(keep):
        x1,y1,x2,y2 = map(int, boxes[i])
        w,h = x2-x1, y2-y1
        sim = float(smax[j]); detc = float(detcs[i])

        if sim < TAU:
            continue
        final = detc * sim
        if final < MIN_FINAL:
            continue

        x = x1
        y = _to_bottom_left_y(y1, h, H)  # 좌하단(0,0) 기준 변환
        en = labels[int(idx[j])]
        results.append({
            "name_en": en,
            "x": int(x), "y": int(y), "w": int(w), "h": int(h),
            "center": ((x1+x2)//2, (y1+y2)//2),
            "final_score": final
        })

    results.sort(key=lambda d: d["final_score"], reverse=True)
    return results, (W,H)

def nearest_to_center(valid_items, frame_size):
    W, H = frame_size
    cx0, cy0 = W/2.0, H/2.0
    if not valid_items:
        return None, False
    dets = []
    for it in valid_items:
        cx, cy = it["center"]
        dets.append((math.hypot(cx-cx0, cy-cy0), it))
    dets.sort(key=lambda t: t[0])
    nearest = dets[0][1]
    multiple = (len(valid_items) >= 2)
    return nearest, multiple