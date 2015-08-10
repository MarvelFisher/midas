#!/bin/bash

if [ -s LTS-INFO_PID ]; then
INFO_PID=`cat LTS-INFO_PID`
kill -9 $INFO_PID
fi


sleep 5

JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:log/info_gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx4g -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=0 -XX:MaxDirectMemorySize=4G -XX:AutoBoxCacheMax=3000000 -XX:MaxPermSize=128m"
echo "Starting LTS-INFO "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -jar jars/cyanspring-info-2.56.jar conf/info_fcserver.xml >> ./log/console.log &
echo $! > LTS-INFO_PID