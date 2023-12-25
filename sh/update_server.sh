#!/bin/bash

echo "Updating repository"
git pull

echo "Building project"
./gradlew build -x test
