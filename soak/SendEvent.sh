#!/bin/bash

if [ "$#" -ne "2" ]; then
    echo "Please input the right parameters."
    exit 0
fi

java -cp "jars/*" com.cyanspring.soak.EventSender $1 $2

echo "Start canceling pending orders..."
sleep 30

HOME = ~ubuntu
if [ ! -d "$HOME/appServer" ]; then
    echo "Directory is not exist, cannot shutdown appServer."
else
    cd $HOME
    cd appServer/
    ./run.sh stop
fi

if [ ! -d "$HOME/LTS" ]; then
    echo "Directory is not exist, cannot shutdown LTSServer."
else
    cd $HOME
    cd LTS/

    if [ -s LTS-INFO_PID ]; then
        INFO_PID=`cat LTS-INFO_PID`
        kill $INFO_PID
    else
        echo "Cannot find LTS-INFO PID to shutdown server."
    fi

    if [ -s LTS_PID ]; then
        LTS_PID=`cat LTS_PID`
        echo "Starting shutdown LTSServer."
        kill $LTS_PID
    else
        echo "Cannot find LTS PID to shutdown server."
    fi
fi
