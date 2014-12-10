#!/bin/bash
jps|grep cyanspring-server-2.56.jar|cut -d ' ' -f1|xargs kill -9

#ulimit -n 1024000

#JAVA_OPTS="-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=$ip -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:$path/log/gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx12g -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=0 -XX:MaxDirectMemorySize=1G -XX:AutoBoxCacheMax=3000000"
echo "Starting LTS "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -jar jars/cyanspring-server-2.56.jar conf/server_fdt_ib.xml >> ./log/console.log &

sleep 15
echo "Starting AppServer "`date` | tee -a ./log/console.log
cd ../appServer
./run.sh
