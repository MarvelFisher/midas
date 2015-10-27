set QHOME=C:\java\git\LTS\kdb\mi
set Q=C:\java\git\LTS\kdb\mi\w32\q
set MI_HOME=C:\java\git\LTS\kdb\mi\mi\tick

cd %MI_HOME%
start %Q% tick.q sym ./hdb -t 1000 -p 5010 >> ./tick.log

cd %MI_HOME%
start %Q% hdb/sym -p 5012 >> ./hdb.log

cd %MI_HOME%
start %Q% r.q -p 5011 >> ./rdb.log

