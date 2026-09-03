# Unified Fullstack Production Dockerfile for DataForge ML
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install python dependencies
COPY backend/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy backend & frontend source
COPY backend/ ./backend/
COPY frontend/ ./frontend/

# Expose port (standard 8000 / $PORT)
EXPOSE 8000

# Mount static frontend into FastAPI app directly
WORKDIR /app/backend
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
