#!/bin/bash
# ===================================================================
# LinkyLink Build Script
# Builds both the React frontend and Spring Boot backend into a single JAR.
# ===================================================================

set -e  # Exit on any error

echo "========================================="
echo "  Building LinkyLink Application"
echo "========================================="

# Step 1: Build the React frontend
echo ""
echo "[1/3] Building React frontend..."
cd frontend
npm install
npm run build
cd ..
echo "✓ React build complete (output: src/main/resources/static/app/)"

# Step 2: Build the Spring Boot backend (which now includes the React build)
echo ""
echo "[2/3] Building Spring Boot backend..."
./mvnw clean package -DskipTests
echo "✓ Spring Boot build complete"

# Step 3: Done!
echo ""
echo "[3/3] Build complete!"
echo "========================================="
echo "  JAR file: target/linkylink-1.0.0.jar"
echo ""
echo "  To run locally:"
echo "    java -jar target/linkylink-1.0.0.jar"
echo ""
echo "  To run on EC2:"
echo "    scp target/linkylink-1.0.0.jar ec2-user@<EC2-IP>:~/"
echo "    ssh ec2-user@<EC2-IP>"
echo "    sudo java -jar linkylink-1.0.0.jar --server.port=80"
echo "========================================="
