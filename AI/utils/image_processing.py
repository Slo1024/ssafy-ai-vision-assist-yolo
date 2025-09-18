from PIL import Image, ImageOps
import numpy as np
from typing import Tuple, Optional
import logging

logger = logging.getLogger(__name__)

def validate_and_normalize_image(
    image: Image.Image,
    max_size: Tuple[int, int] = (1024, 1024),
    min_size: Tuple[int, int] = (32, 32)
) -> Image.Image:
    """
    Validate and normalize input image for processing

    Args:
        image: PIL Image object
        max_size: Maximum allowed image dimensions
        min_size: Minimum allowed image dimensions

    Returns:
        Processed PIL Image
    """
    try:
        # Check image mode and convert if necessary
        if image.mode not in ['RGB', 'RGBA']:
            logger.warning(f"Converting image from {image.mode} to RGB")
            image = image.convert('RGB')
        elif image.mode == 'RGBA':
            # Convert RGBA to RGB with white background
            background = Image.new('RGB', image.size, (255, 255, 255))
            background.paste(image, mask=image.split()[-1])  # Use alpha channel as mask
            image = background

        # Validate image size
        width, height = image.size
        if width < min_size[0] or height < min_size[1]:
            raise ValueError(f"Image too small: {width}x{height}, minimum: {min_size}")

        # Resize if too large
        if width > max_size[0] or height > max_size[1]:
            logger.info(f"Resizing image from {width}x{height} to fit {max_size}")
            image.thumbnail(max_size, Image.Resampling.LANCZOS)

        # Auto-orient based on EXIF data
        try:
            image = ImageOps.exif_transpose(image)
        except Exception as e:
            logger.warning(f"Could not auto-orient image: {e}")

        return image

    except Exception as e:
        logger.error(f"Image validation failed: {e}")
        raise

def get_image_stats(image: Image.Image) -> dict:
    """
    Get basic statistics about the image

    Args:
        image: PIL Image object

    Returns:
        Dictionary with image statistics
    """
    try:
        # Convert to numpy for analysis
        img_array = np.array(image)

        stats = {
            "width": image.size[0],
            "height": image.size[1],
            "mode": image.mode,
            "format": image.format,
            "channels": len(img_array.shape) if len(img_array.shape) > 2 else 1,
            "mean_brightness": float(np.mean(img_array)),
            "std_brightness": float(np.std(img_array)),
            "min_value": int(np.min(img_array)),
            "max_value": int(np.max(img_array))
        }

        return stats

    except Exception as e:
        logger.error(f"Failed to compute image stats: {e}")
        return {"error": str(e)}

def create_thumbnail(
    image: Image.Image,
    size: Tuple[int, int] = (128, 128)
) -> Image.Image:
    """
    Create thumbnail version of image

    Args:
        image: PIL Image object
        size: Thumbnail size

    Returns:
        Thumbnail PIL Image
    """
    try:
        thumbnail = image.copy()
        thumbnail.thumbnail(size, Image.Resampling.LANCZOS)
        return thumbnail
    except Exception as e:
        logger.error(f"Failed to create thumbnail: {e}")
        raise

# Future enhancement functions (commented for MVP)
# def apply_image_augmentations(image: Image.Image) -> List[Image.Image]:
#     """
#     Apply data augmentations for better inference robustness
#     """
#     augmented = []
#
#     # Original image
#     augmented.append(image)
#
#     # Slight rotations
#     for angle in [-5, 5]:
#         rotated = image.rotate(angle, expand=True, fillcolor=(255, 255, 255))
#         augmented.append(rotated)
#
#     # Brightness adjustments
#     from PIL import ImageEnhance
#     for factor in [0.9, 1.1]:
#         enhancer = ImageEnhance.Brightness(image)
#         enhanced = enhancer.enhance(factor)
#         augmented.append(enhanced)
#
#     return augmented
#
# def preprocess_for_mobile_streaming(image: Image.Image) -> Image.Image:
#     """
#     Optimize image processing for mobile streaming scenarios
#     """
#     # Fast preprocessing for real-time scenarios
#     # - Aggressive compression
#     # - Quick resize
#     # - Noise reduction
#     pass