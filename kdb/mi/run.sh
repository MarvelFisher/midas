export QHOME=/home/ubuntu/kdb/
Q=/home/ubuntu/kdb/l32/q
MI_HOME=/home/ubuntu/kdb/mi/tick

cd $MI_HOME
nohup $Q tick.q sym ./hdb -t 1000 -p 5010 < /dev/null >> ./tick.log 2>&1&
echo $! > ./pid_tick

cd $MI_HOME
nohup $Q hdb/sym -p 5012 < /dev/null >> ./hdb.log 2>&1&
echo $! > ./pid_hdb

cd $MI_HOME
# nohup $Q w.q :5010 :5012 -koe -p 5011 < /dev/null >> ./rdb.log 2>&1&
nohup $Q r.q -p 5011 < /dev/null >> ./rdb.log 2>&1&
echo $! > ./pid_rdb

cd $MI_HOME
nohup $Q mi.q hdb :5011 :5012 -p 5013 < /dev/null >> ./mi.log 2>&1&
echo $! > ./pid_mi
