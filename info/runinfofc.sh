#!/bin/bash

if [ -s LTS-INFO_PID ]; then
LTS_PID=`cat LTS-INFO_PID`
kill -9 $LTS-INFO_PID
fi


sleep 5

JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:log/gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=0 -XX:MaxDirectMemorySize=1G -XX:AutoBoxCacheMax=3000000"
echo "Starting LTS-INFO "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -jar jars/cyanspring-info-2.56.jar conf/info_fcserver.xml >> ./log/console.log &
echo $! > LTS-INFO_PID