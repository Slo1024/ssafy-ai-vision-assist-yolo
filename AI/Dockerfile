# Use Python 3.10 slim image for better compatibility
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Install minimal system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    wget \
    git \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Create necessary directories
RUN mkdir -p /app/logs /app/models

# Copy application code
COPY . .

# Create models directory and set permissions
RUN chmod -R 755 /app && \
    mkdir -p /models

# Set environment variables
ENV PYTHONPATH=/app
ENV PYTHONUNBUFFERED=1
ENV MODEL_PATH=/models/clip_linear_head.pt

# Create non-root user for security
RUN useradd -m -u 1000 aiuser && \
    chown -R aiuser:aiuser /app /models
USER aiuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8083/health')" || exit 1

# Expose port
EXPOSE 8083

# Command to run the application
CMD ["python", "main.py"]