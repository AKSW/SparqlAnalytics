#!/bin/bash

# Default Settings
defaultPort=5522


#DIR="$( cd "$( dirname "$0" )" && pwd )"
DIR=`pwd`

configDirArg="$1"

if [[ "${configDirArg:0:1}" == "/" ]]; then
	configDir="$configDirArg"
else
	configDir="$DIR/$configDirArg"
fi


port="${2:-$defaultPort}"
contextPath="${3:-/}"

cd "$DIR/../sparql-analytics-server"

echo "Starting Sparqlify Platform"
echo "---------------------------"
echo "Port  : $port"
echo "Config: $configDir"
echo "---------------------------"

mvn war:war jetty:run-war "-Djetty.port=$port" "-Dplatform.linkedData.contextPath=$contextPath" "-DconfigDirectory=$configDir" 


