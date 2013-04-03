#!/bin/bash
cd ..
mvn clean install
cd bin
./run-platform.sh ../sparql-analytics-server/config/example/
