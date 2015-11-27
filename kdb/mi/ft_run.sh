export QHOME=/home/ubuntu/kdb/
Q=/home/ubuntu/kdb/l32/q
MI_HOME=/home/ubuntu/kdb/mi/tick

cd $MI_HOME
nohup $Q tick.q sym ./hdbft -t 1000 -p 5110 < /dev/null >> ./tick_ft.log 2>&1&
echo $! > ./pid_tick_ft

cd $MI_HOME
nohup $Q hdbft/sym -p 5112 < /dev/null >> ./hdb_ft.log 2>&1&
echo $! > ./pid_hdb_ft

cd $MI_HOME
nohup $Q r.q :5110 :5112 -p 5111 < /dev/null >> ./rdb_ft.log 2>&1&
echo $! > ./pid_rdb_ft

cd $MI_HOME
nohup $Q mi.q hdbft :5111 :5112 -p 5113 < /dev/null >> ./mi_ft.log 2>&1&
echo $! > ./pid_mi_ft
