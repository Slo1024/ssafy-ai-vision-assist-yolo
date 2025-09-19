from fastapi import APIRouter, File, UploadFile, HTTPException, Depends
from fastapi.responses import JSONResponse
from PIL import Image
import io
import time
import logging
from typing import Dict, Any

from models.inference import get_inference_engine, InferenceEngine

logger = logging.getLogger(__name__)

router = APIRouter()

# Dependency to get inference engine
def get_engine() -> InferenceEngine:
    return get_inference_engine()

@router.post("/predict", response_model=Dict[str, Any])
async def predict_product(
    file: UploadFile = File(...),
    engine: InferenceEngine = Depends(get_engine)
):
    """
    Predict product from uploaded image

    Args:
        file: Uploaded image file (JPEG, PNG)

    Returns:
        JSON with prediction results and confidence scores
    """
    start_time = time.time()

    try:
        # Validate file type
        if not file.content_type.startswith('image/'):
            raise HTTPException(
                status_code=400,
                detail=f"File must be an image. Got: {file.content_type}"
            )

        # Read and validate image
        contents = await file.read()
        if len(contents) == 0:
            raise HTTPException(status_code=400, detail="Empty file")

        # Convert to PIL Image
        try:
            image = Image.open(io.BytesIO(contents))
        except Exception as e:
            raise HTTPException(
                status_code=400,
                detail=f"Invalid image file: {str(e)}"
            )

        # Run inference
        result = engine.predict(image)

        # Add performance metrics
        inference_time = time.time() - start_time
        result["inference_time_seconds"] = round(inference_time, 3)
        result["status"] = "success"

        logger.info(
            f"Prediction completed in {inference_time:.3f}s - "
            f"{result['predicted_class']} ({result['confidence']:.3f})"
        )

        return JSONResponse(content=result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error: {str(e)}"
        )

@router.get("/health")
async def health_check():
    """Health check endpoint"""
    try:
        engine = get_inference_engine()
        if engine.model is None:
            return JSONResponse(
                status_code=503,
                content={
                    "status": "unhealthy",
                    "message": "Model not loaded",
                    "timestamp": time.time()
                }
            )

        return JSONResponse(content={
            "status": "healthy",
            "model_loaded": True,
            "device": str(engine.device),
            "confidence_threshold": engine.confidence_threshold,
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

@router.get("/model/info")
async def model_info(engine: InferenceEngine = Depends(get_engine)):
    """Get model information"""
    try:
        return JSONResponse(content={
            "model_path": str(engine.model_path),
            "device": str(engine.device),
            "classes": engine.model.classes if engine.model else [],
            "num_classes": len(engine.model.classes) if engine.model else 0,
            "confidence_threshold": engine.confidence_threshold,
            "model_loaded": engine.model is not None
        })
    except Exception as e:
        logger.error(f"Model info error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/model/threshold")
async def update_threshold(
    threshold: float,
    engine: InferenceEngine = Depends(get_engine)
):
    """Update confidence threshold"""
    try:
        if not 0.0 <= threshold <= 1.0:
            raise HTTPException(
                status_code=400,
                detail="Threshold must be between 0.0 and 1.0"
            )

        success = engine.update_threshold(threshold)
        if success:
            return JSONResponse(content={
                "status": "success",
                "new_threshold": threshold,
                "message": f"Threshold updated to {threshold}"
            })
        else:
            raise HTTPException(
                status_code=400,
                detail="Failed to update threshold"
            )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Threshold update error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# Future hot-swapping endpoint (commented for MVP)
# @router.post("/model/reload")
# async def reload_model(
#     model_path: str = None,
#     engine: InferenceEngine = Depends(get_engine)
# ):
#     """Reload model (hot-swap capability for future)"""
#     try:
#         if model_path:
#             success = engine.hot_swap_model(model_path)
#         else:
#             success = engine.load_model()
#
#         if success:
#             return JSONResponse(content={
#                 "status": "success",
#                 "message": "Model reloaded successfully",
#                 "model_path": str(engine.model_path)
#             })
#         else:
#             raise HTTPException(
#                 status_code=500,
#                 detail="Failed to reload model"
#             )
#
#     except Exception as e:
#         logger.error(f"Model reload error: {e}")
#         raise HTTPException(status_code=500, detail=str(e))