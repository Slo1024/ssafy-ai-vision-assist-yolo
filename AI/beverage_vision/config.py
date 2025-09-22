from pathlib import Path
import os, json
from dotenv import load_dotenv

ROOT = Path(__file__).resolve().parents[1]
load_dotenv(ROOT / ".env")

# Default paths for AI directory
DET_WT = os.getenv("DET_WT", str(ROOT / "best_ft.pt"))
CLS_PROFILE = os.getenv("CLS_PROFILE", "effb0").lower()

CLS_EMB = os.getenv("CLS_EMB", str(ROOT / "best_cls_embed_effb0_v2.pt"))
CLS_PROTOS = os.getenv("CLS_PROTOS", str(ROOT / "prototypes_effb0_v2.pt"))
CLS_TAU = os.getenv("CLS_TAU", str(ROOT / "tau_effb0_v2.json"))

CONF_THR = float(os.getenv("CONF_THR", "0.35"))
IOU_THR  = float(os.getenv("IOU_THR",  "0.50"))
MIN_BOX  = int(os.getenv("MIN_BOX", "10"))
MIN_FINAL= float(os.getenv("MIN_FINAL", "0.25"))

def load_tau() -> float:
    with open(CLS_TAU, "r", encoding="utf-8") as f:
        return float(json.load(f)["tau"])