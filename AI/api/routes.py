from fastapi import APIRouter, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from PIL import Image
import io
import time
import logging
from typing import List

from beverage_vision.models.inference import detect_and_classify, nearest_to_center
from beverage_vision.utils.mapping import EN2KR

logger = logging.getLogger(__name__)

router = APIRouter()

@router.get("/health")
async def health_check():
    """Health check endpoint"""
    try:
        return JSONResponse(content={
            "status": "healthy",
            "service": "beverage-vision",
            "timestamp": time.time()
        })
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return JSONResponse(
            status_code=503,
            content={
                "status": "unhealthy",
                "message": str(e),
                "timestamp": time.time()
            }
        )

# Beverage Vision Endpoints

@router.post("/api/v1/product/search/ai")
async def shelf_search_ai(shelf_images: List[UploadFile] = File(...)):
    """
    AI-powered shelf product search

    Args:
        shelf_images: List of uploaded shelf images

    Returns:
        JSON with all detected products from shelf images
    """
    try:
        items = []
        for f in shelf_images:
            # Validate file type
            if not f.content_type.startswith('image/'):
                raise HTTPException(
                    status_code=400,
                    detail=f"File must be an image. Got: {f.content_type}"
                )

            # Read and process image
            contents = await f.read()
            pil = Image.open(io.BytesIO(contents)).convert("RGB")

            # Detect and classify products
            dets, _ = detect_and_classify(pil)

            # Convert to Korean names and format response
            for d in dets:
                name_kr = EN2KR.get(d["name_en"], d["name_en"])
                items.append({
                    "name": name_kr,
                    "x": d["x"],
                    "y": d["y"],
                    "w": d["w"],
                    "h": d["h"]
                })

        return JSONResponse({"items": items})

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Shelf search AI error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error: {str(e)}"
        )

@router.post("/api/v1/product/search/location/ai")
async def frame_location_ai(current_frame: UploadFile = File(...)):
    """
    AI-powered product location search from current frame

    Args:
        current_frame: Current camera frame image

    Returns:
        JSON with detected products and whether multiple items are found
    """
    try:
        # Validate file type
        if not current_frame.content_type.startswith('image/'):
            raise HTTPException(
                status_code=400,
                detail=f"File must be an image. Got: {current_frame.content_type}"
            )

        # Read and process image
        contents = await current_frame.read()
        pil = Image.open(io.BytesIO(contents)).convert("RGB")

        # Detect and classify products
        dets, frame_size = detect_and_classify(pil)

        # Find nearest product to center
        nearest, multiple = nearest_to_center(dets, frame_size)

        if not nearest:
            return JSONResponse({"multiple": False, "items": []})

        # Convert to Korean name
        name_kr = EN2KR.get(nearest["name_en"], nearest["name_en"])

        return JSONResponse({
            "multiple": multiple,
            "items": [name_kr]
        })

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Frame location AI error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error: {str(e)}"
        )