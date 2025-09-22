from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
import uvicorn
import sys
from contextlib import asynccontextmanager

from api.routes import router

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
    ]
)

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    logger.info("Starting Beverage Vision AI Service...")
    try:
        # Test import of beverage vision modules
        from beverage_vision.models.loader import get_models
        logger.info("Beverage vision models loaded successfully")
        yield
    except Exception as e:
        logger.error(f"Failed to initialize beverage vision: {e}")
        # Continue anyway for health checks
        yield
    finally:
        # Cleanup
        logger.info("Shutting down Beverage Vision AI Service...")

# Create FastAPI application
app = FastAPI(
    title="Lookey Beverage Vision AI Service",
    description="YOLO + EfficientNet based beverage product recognition",
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
app.include_router(router)

@app.get("/")
async def root():
    """Root endpoint with service info"""
    return JSONResponse(content={
        "service": "Lookey Beverage Vision AI Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "shelf_search": "/api/v1/product/search/ai",
            "location_search": "/api/v1/product/search/location/ai",
            "docs": "/docs"
        }
    })

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
        reload=True,  # Set to True for development
        workers=1  # Single worker for model consistency
    )