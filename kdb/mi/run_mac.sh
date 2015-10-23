export QHOME=/Users/jiayun/kdb/
Q=/Users/jiayun/kdb/m32/q
MI_HOME=/Users/jiayun/kdb/mi/tick

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

# cd $MI_HOME
# echo $! > ./mi_tick

