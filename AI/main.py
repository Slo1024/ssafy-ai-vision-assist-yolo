from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
import uvicorn
import sys
from contextlib import asynccontextmanager

from api.routes import router
from models.inference import get_inference_engine

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('/app/logs/ai_service.log', mode='a')
    ]
)

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    logger.info("Starting AI Inference Service...")
    try:
        # Initialize inference engine
        engine = get_inference_engine()
        logger.info("Inference engine initialized successfully")
        yield
    except Exception as e:
        logger.error(f"Failed to initialize inference engine: {e}")
        raise e
    finally:
        # Cleanup
        logger.info("Shutting down AI Inference Service...")

# Create FastAPI application
app = FastAPI(
    title="Lookey AI Inference Service",
    description="CLIP-based product recognition for Korean convenience store items",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# CORS middleware for frontend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://j13e101.p.ssafy.io:8081",  # Production backend
        "http://j13e101.p.ssafy.io:8082",  # Development backend
        "http://localhost:8081",
        "http://localhost:8082",
        "http://localhost:3000",  # For any frontend testing
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# Include API routes
app.include_router(router, prefix="/api/v1")

@app.get("/")
async def root():
    """Root endpoint with service info"""
    return JSONResponse(content={
        "service": "Lookey AI Inference Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "predict": "/api/v1/predict",
            "health": "/api/v1/health",
            "model_info": "/api/v1/model/info",
            "docs": "/docs"
        }
    })

@app.get("/health")
async def health():
    """Global health check"""
    try:
        engine = get_inference_engine()
        return JSONResponse(content={
            "status": "healthy",
            "service": "ai-inference",
            "model_loaded": engine.model is not None,
            "device": str(engine.device)
        })
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return JSONResponse(
            status_code=503,
            content={
                "status": "unhealthy",
                "service": "ai-inference",
                "error": str(e)
            }
        )

# Error handlers
@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    """Custom HTTP exception handler"""
    logger.warning(f"HTTP {exc.status_code}: {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.detail,
            "status_code": exc.status_code,
            "path": str(request.url)
        }
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """General exception handler"""
    logger.error(f"Unhandled exception: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "status_code": 500,
            "path": str(request.url)
        }
    )

if __name__ == "__main__":
    # Run with uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8083,
        log_level="info",
        reload=False,  # Set to True for development
        workers=1  # Single worker for model consistency
    )
