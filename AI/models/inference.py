import torch
import torch.nn as nn
from torchvision import transforms
from PIL import Image
import numpy as np
import logging
from pathlib import Path
from typing import List, Tuple, Dict, Any
import clip

logger = logging.getLogger(__name__)

class CLIPLinearHead(nn.Module):
    """CLIP Linear Head model for product classification"""

    def __init__(self, clip_model_name: str = "ViT-B/32", num_classes: int = 9):
        super().__init__()
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

        # Load CLIP model
        self.clip_model, self.clip_preprocess = clip.load(clip_model_name, device=self.device)

        # Freeze CLIP parameters
        for param in self.clip_model.parameters():
            param.requires_grad = False

        # Linear classification head
        self.head = nn.Linear(512, num_classes)  # CLIP ViT-B/32 outputs 512 features

        # Product classes (Korean convenience store products)
        self.classes = [
            "꿀홍삼 180ML",
            "델몬트포도 400ML",
            "비타500 100ML",
            "아침햇살 500ML",
            "양반유자제로 500ML",
            "얼라이브망고 500ML",
            "위생천 75ML",
            "제주감귤 500ML",
            "티로그복숭아아이스티 500ML"
        ]

        self.to(self.device)

    def forward(self, image):
        """Forward pass through CLIP + linear head"""
        with torch.no_grad():
            features = self.clip_model.encode_image(image)

        # Apply linear head
        logits = self.head(features.float())
        return logits

class InferenceEngine:
    """Main inference engine for product classification"""

    def __init__(self, model_path: str = "/models/clip_linear_head.pt"):
        self.model_path = Path(model_path)
        self.model = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.confidence_threshold = 0.3  # Low threshold for MVP

        # Image preprocessing
        self.transform = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.48145466, 0.4578275, 0.40821073],
                               std=[0.26862954, 0.26130258, 0.27577711])
        ])

        logger.info(f"Inference engine initialized on device: {self.device}")

    def load_model(self) -> bool:
        """Load the trained CLIP linear head model"""
        try:
            if not self.model_path.exists():
                logger.error(f"Model file not found: {self.model_path}")
                return False

            # Initialize model
            self.model = CLIPLinearHead()

            # Load checkpoint
            checkpoint = torch.load(self.model_path, map_location=self.device)

            # Handle different checkpoint formats
            if isinstance(checkpoint, dict):
                if 'head' in checkpoint:
                    # Load only the linear head weights
                    self.model.head.load_state_dict(checkpoint['head'])
                    logger.info("Loaded linear head weights from checkpoint")
                elif 'model_state_dict' in checkpoint:
                    self.model.load_state_dict(checkpoint['model_state_dict'])
                elif 'state_dict' in checkpoint:
                    self.model.load_state_dict(checkpoint['state_dict'])
                else:
                    # Assume it's the state dict itself
                    self.model.load_state_dict(checkpoint)
            else:
                logger.warning("Unexpected checkpoint format, attempting direct load")
                return False

            self.model.eval()
            logger.info(f"Model loaded successfully from {self.model_path}")
            return True

        except Exception as e:
            logger.error(f"Failed to load model: {e}")
            return False

    def preprocess_image(self, image: Image.Image) -> torch.Tensor:
        """Preprocess image for CLIP model"""
        try:
            # Convert to RGB if needed
            if image.mode != 'RGB':
                image = image.convert('RGB')

            # Apply CLIP preprocessing
            processed = self.model.clip_preprocess(image).unsqueeze(0)
            return processed.to(self.device)

        except Exception as e:
            logger.error(f"Image preprocessing failed: {e}")
            raise

    def predict(self, image: Image.Image) -> Dict[str, Any]:
        """Run inference on a single image"""
        try:
            if self.model is None:
                raise ValueError("Model not loaded. Call load_model() first.")

            # Preprocess image
            processed_image = self.preprocess_image(image)

            # Run inference
            with torch.no_grad():
                logits = self.model(processed_image)
                probabilities = torch.softmax(logits, dim=1)

            # Get predictions
            probs = probabilities.cpu().numpy()[0]
            predicted_idx = np.argmax(probs)
            confidence = float(probs[predicted_idx])
            predicted_class = self.model.classes[predicted_idx]

            # Prepare response
            result = {
                "predicted_class": predicted_class,
                "confidence": confidence,
                "all_predictions": [
                    {
                        "class": self.model.classes[i],
                        "confidence": float(probs[i])
                    }
                    for i in range(len(self.model.classes))
                ],
                "meets_threshold": confidence >= self.confidence_threshold,
                "threshold": self.confidence_threshold
            }

            logger.info(f"Prediction: {predicted_class} (confidence: {confidence:.3f})")
            return result

        except Exception as e:
            logger.error(f"Prediction failed: {e}")
            raise

    def update_threshold(self, new_threshold: float) -> bool:
        """Update confidence threshold for predictions"""
        try:
            if 0.0 <= new_threshold <= 1.0:
                self.confidence_threshold = new_threshold
                logger.info(f"Confidence threshold updated to {new_threshold}")
                return True
            else:
                logger.warning(f"Invalid threshold value: {new_threshold}")
                return False
        except Exception as e:
            logger.error(f"Failed to update threshold: {e}")
            return False

    # Future hot-swapping capability (commented for now)
    # def hot_swap_model(self, new_model_path: str) -> bool:
    #     """Hot swap model without restarting service"""
    #     try:
    #         # Load new model to temporary instance
    #         temp_engine = InferenceEngine(new_model_path)
    #         if temp_engine.load_model():
    #             # Swap models atomically
    #             old_model = self.model
    #             self.model = temp_engine.model
    #             self.model_path = Path(new_model_path)
    #
    #             # Clean up old model
    #             del old_model
    #             torch.cuda.empty_cache() if torch.cuda.is_available() else None
    #
    #             logger.info(f"Model hot-swapped to {new_model_path}")
    #             return True
    #         return False
    #     except Exception as e:
    #         logger.error(f"Hot swap failed: {e}")
    #         return False

# Global inference engine instance
inference_engine = None

def get_inference_engine() -> InferenceEngine:
    """Get global inference engine instance"""
    global inference_engine
    if inference_engine is None:
        inference_engine = InferenceEngine()
        if not inference_engine.load_model():
            logger.error("Failed to initialize inference engine")
            raise RuntimeError("Model loading failed")
    return inference_engine