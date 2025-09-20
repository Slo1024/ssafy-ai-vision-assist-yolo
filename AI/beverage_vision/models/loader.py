from ultralytics import YOLO
import torch, torch.nn as nn, torch.nn.functional as F
import torchvision as tv
from pathlib import Path
import json

from beverage_vision.config import DET_WT, CLS_PROFILE, CLS_EMB, CLS_PROTOS, load_tau

class EmbedNet(nn.Module):
    def __init__(self, backbone: nn.Module, feat_dim: int, embed_dim=256, num_classes=9):
        super().__init__()
        self.bb = backbone
        self.embed = nn.Linear(feat_dim, embed_dim)
        self.cls = nn.Linear(embed_dim, num_classes)  # 학습용 헤드(서빙은 임베딩만 사용)
    def forward(self, x):
        f = self.bb(x)
        z = F.normalize(self.embed(f), dim=1)
        return z

def build_backbone(profile: str):
    if profile == "effb0":
        bb = tv.models.efficientnet_b0(weights=None)
        feat = bb.classifier[1].in_features
        bb.classifier[1] = nn.Identity()
    elif profile == "mnv3":
        bb = tv.models.mobilenet_v3_small(weights="IMAGENET1K_V1")
        feat = bb.classifier[3].in_features
        bb.classifier[3] = nn.Identity()
    else:
        raise ValueError("CLS_PROFILE must be effb0 or mnv3")
    return bb, feat

device = "cuda:0" if torch.cuda.is_available() else "cpu"

# 싱글톤
detector = None
cls_net = None
P_mat = None
labels = None
TAU = None

def init_models():
    global detector, cls_net, P_mat, labels, TAU
    detector = YOLO(DET_WT)

    bb, feat = build_backbone(CLS_PROFILE)
    net = EmbedNet(bb, feat_dim=feat).to(device).eval()
    ckpt = torch.load(CLS_EMB, map_location="cpu")
    net.load_state_dict(ckpt["state_dict"], strict=True)
    cls_net = net

    protos = torch.load(CLS_PROTOS, map_location="cpu")  # {en_label: tensor}
    labels = list(protos.keys())
    P_mat = torch.stack([protos[k] for k in labels], 0).to(device)

    TAU = load_tau()

def get_models():
    if any(x is None for x in [detector, cls_net, P_mat]):
        init_models()
    return detector, cls_net, P_mat, labels, TAU, device