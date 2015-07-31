#!/bin/bash

if [ -s LTS-INFO_SA_PID ]; then
INFO_PID=`cat LTS-INFO_SA_PID`
kill -9 $INFO_PID
fi


sleep 5

#JAVA_OPTS="-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=10.0.0.52 -Dcom.sun.management.jmxremote.port=1239 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:log/info_gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx2g -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=0 -XX:MaxDirectMemorySize=2G -XX:AutoBoxCacheMax=3000000 -XX:MaxPermSize=128m"
echo "Starting LTS-INFO "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -jar InfoServer.jar conf/info_sa_scserver.xml >> ./log/console.log &
echo $! > LTS-INFO_SA_PID