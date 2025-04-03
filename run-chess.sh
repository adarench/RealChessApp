#!/bin/bash

# Stop any running processes
echo "Starting chess application..."

# Build with dependencies
echo "Building application with dependencies..."
mvn clean package -DskipTests

# Create a temporary database properties file to use in-memory database
mkdir -p server/target/classes
cat > server/target/classes/db.properties << EOF
db.host=localhost
db.port=9000
db.name=chessdb
db.user=sa
db.password=
EOF

# Run the server in background
echo "Starting server..."
cd server
java -jar target/server-jar-with-dependencies.jar &
SERVER_PID=$!

# Give the server a moment to start
sleep 2

# Run the client
echo "Starting client..."
cd ../client
java -jar target/client-jar-with-dependencies.jar

# Kill the server when the client exits
kill $SERVER_PID 2>/dev/null