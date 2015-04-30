#!/bin/bash

if [ -s LTS-INFO_PID ]; then
INFO_PID=`cat LTS-INFO_PID`
kill -9 $INFO_PID
fi


sleep 5

JAVA_OPTS="-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=10.0.0.51 -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:log/gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=0 -XX:MaxDirectMemorySize=1G -XX:AutoBoxCacheMax=3000000 -XX:MaxPermSize=128m"
echo "Starting LTS-INFO "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -jar jars/cyanspring-info-2.56.jar conf/info_fxhk_server.xml >> ./log/console.log &
echo $! > LTS-INFO_PID