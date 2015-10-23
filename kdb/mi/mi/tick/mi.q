
TI:15 / seconds
TPS:8 / TI = 120 seconds
TPM:40 / TI = 10 minutes
TPL:17280 / TI = 3 days

/ get the ticker plant, rdb and history ports, defaults are 5010,5011,5012
.u.x:.z.x,(count .z.x)_(":5010";":5011";":5012")

if[not system"p";system"p 5013"]

mi:([]time:`timestamp$(); sym:`g#`symbol$(); rvi:`float$(); cvi:`float$(); mi:`float$(); umib:`float$(); umis:`float$())

hdb:hopen `$":",.u.x 2
data:hdb"select[<time] time, sym, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, 15 xbar time.second from quote where time > .z.d-5"
/ show count data

rdb:hopen `$":",.u.x 1
data,:rdb"select from (select[<time] time, sym, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, 15 xbar time.second from quote) where ({x in -1_x};i) fby sym"
/ show count data

getUmiB:{[fs;fm;fl;v;cmfr] ?[(fs>fm)&(fm>fl)&(fl>0)&((abs fs)>(sqrt v))&(cmfr>0.65);(fs%fm)+(fm%fl);0f]}

getUmiS:{[fs;fm;fl;v;cmff] ?[(fs<fm)&(fm<fl)&(fl<0)&((abs fs)>(sqrt v))&(cmff>0.65);(fs%fm)+(fm%fl);0f]}

getnew:{
    new:rdb"select from (select[<time] time, sym, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, 15 xbar time.second from quote where time > .z.P  - 60000000000) where ({x in -1_x};i) fby sym";
    lasttime:select lasttime:(last time) by sym from data;
    data,:select time,sym,sprice,eprice,hprice,lprice,v from aj[`sym;new;lasttime] where time>lasttime;
    / show count data;
    dataS:select from data where ({x in (neg TPS)#x};i) fby sym;
    dataM:select from data where ({x in (neg TPM)#x};i) fby sym;
    dataL:select from data where ({x in (neg TPL)#x};i) fby sym;
    
    viL:select avg(v) by sym from dataL;
    baseVi:(select avg(v) from viL)[`v][0];
    
    stdViS:select last time, sdev(v) by sym from dataS;
    stdViL:select vl:sdev(v) by sym from dataL;
    
    rvi:select sym,rvi:(v%baseVi)|0 from viL;
    cur_vi:select time,sym,cvi:v%vl from aj[`sym;stdViS;stdViL];
    
    show select[3;>rvi] from rvi;
    show select[3;>cvi] from cur_vi;
    
    mfS:select fs:avg((sprice - eprice) % sprice) by sym from dataS;
    mfM:select fm:avg((sprice - eprice) % sprice) by sym from dataM;
    mfL:select fl:avg((sprice - eprice) % sprice) by sym from dataL;
    
    mi:select sym,mi:fs%v from aj[`sym;mfS;viL];
    
    show select[3;>mi] from mi;
    show select[3;<mi] from mi;
    
    cmfrS:select cmfr:avg(eprice%hprice) by sym from dataS;
    cmffS:select cmff:avg(lprice%eprice) by sym from dataS;
    
    umi:select sym,umib:getUmiB[fs;fm;fl;v;cmfr],umis:getUmiS[fs;fm;fl;v;cmff] from aj[`sym;aj[`sym;cmfrS;cmffS];aj[`sym;aj[`sym;aj[`sym;mfS;mfM];mfL];viL]];
    show select from umi where (umib>0|umis>0);
    }

.z.ts:getnew
\t 15000
