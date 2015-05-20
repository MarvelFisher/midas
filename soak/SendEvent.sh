#!/bin/bash

if [ "$#" -ne "2" ]; then
    echo "Please input the right parameters."
    exit 0
fi

java -cp "jars/*" com.cyanspring.soak.EventSender $1 $2

echo "Start canceling pending orders..."
sleep 20

cd ~ubuntu

if [ ! -d "appServer/" ]; then
    echo "Directory is not exist, cannot shutdown appServer."
else
    cd appServer/
    echo "Starting shutdown appServer"
    ./run.sh stop
fi

sleep 3

cd ..

if [ ! -d "LTS/" ]; then
    echo "Directory is not exist, cannot shutdown LTSServer."
else
    cd LTS/

    if [ -s LTS-INFO_PID ]; then
        INFO_PID=`cat LTS-INFO_PID`
        echo "Starting shutdown LTS-INFO server, PID: $INFO_PID"
        kill -9 $INFO_PID
    else
        echo "Cannot find LTS-INFO PID to shutdown server."
    fi

    sleep 3

    if [ -s LTS_PID ]; then
        LTS_PID=`cat LTS_PID`
        echo "Starting shutdown LTSServer, PID: $LTS_PID"
        kill $LTS_PID
    else
        echo "Cannot find LTS PID to shutdown server."
    fi
fi
