/ q mi.q 15 8 40 17280 5
/ q mi.q 60 4 30 7200 7 -p 5014

/ TI:15 / seconds
/ TPS:8 / TI = 120 seconds
/ TPM:40 / TI = 10 minutes
/ TPL:17280 / TI = 3 days

\c 25 220

TI:"I" $ (.z.x 0)
TPS:"I" $ (.z.x 1)
TPM:"I" $ (.z.x 2)
TPL:"I" $ (.z.x 3)
DATADAYS:"I" $ (.z.x 4)
TIBUFFER:TI*4*1000000000

/ get the ticker plant, rdb and history ports, defaults are 5010,5011,5012
/ .u.x:.z.x,(count .z.x)_(":5010";":5011";":5012")
.u.x:(":5010";":5011";":5012")

if[not system"p";system"p 5013"]

idx:([]time:`timestamp$(); sym:`g#`symbol$(); ask:`float$(); bid:`float$(); vis:`float$(); vil:`float$(); rvi:`float$(); cvi:`float$(); mi:`float$(); umib:`float$(); umis:`float$(); cmfr:`float$(); cmff:`float$(); absfs:`float$(); grvl:`float$(); cr1:`boolean$(); cr2:`boolean$(); cr3:`boolean$(); cf1:`boolean$(); cf2:`boolean$(); cf3:`boolean$())

hdb:hopen `$":",.u.x 2
data:hdb "" sv ("select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote where time > .z.D-"; string DATADAYS)
/ show count data

rdb:hopen `$":",.u.x 1
data,:rdb "" sv ("select from (select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote) where ({x in -1_x};i) fby sym")
/ show count data

getUmiB:{[fs;fm;fl;v;cmfr] ?[(fs>fm)&(fm>fl)&(fl>0)&((abs fs)>(0.65 * v))&(cmfr<0.3);(fs%fm)+(fm%fl);0f]}

getUmiS:{[fs;fm;fl;v;cmff] ?[(fs<fm)&(fm<fl)&(fl<0)&((abs fs)>(0.65 * v))&(cmff<0.3);(fs%fm)+(fm%fl);0f]}

getnew:{
    new:rdb "" sv ("select from (select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote where time > .z.P  - "; string TIBUFFER;") where ({x in -1_x};i) fby sym");
    if[(count new)=0;:0]
    lasttime:select lasttime:(last time) by sym from data;
    data,:select time,sym,ask,bid,sprice,eprice,hprice,lprice,v from aj[`sym;new;lasttime] where time>lasttime;
    / show count data;
    dataS:select from data where ({x in (neg TPS)#x};i) fby sym;
    dataM:select from data where ({x in (neg TPM)#x};i) fby sym;
    dataL:select from data where ({x in (neg TPL)#x};i) fby sym;
    
    viL:select avg(v) by sym from dataL;
    baseVi:(select avg(v) from viL)[`v][0];
    
    stdViS:select last time, last ask, last bid, sdev(v) by sym from dataS;
    stdViL:select vl:sdev(v) by sym from dataL;
    
    rvi:select sym,rvi:(v%baseVi)|0 from viL;
    cur_vi:select time,sym,ask,bid,cvi:v%vl from aj[`sym;stdViS;stdViL];
    
    / show select[3;>rvi] from rvi;
    / show select[3;>cvi] from cur_vi;
    
    mfS:select fs:avg((eprice - sprice) % sprice) by sym from dataS;
    mfM:select fm:avg((eprice - sprice) % sprice) by sym from dataM;
    mfL:select fl:avg((eprice - sprice) % sprice) by sym from dataL;
    
    mi:select sym,mi:fs%v from aj[`sym;mfS;viL];
    
    / show select[3;>mi] from mi;
    / show select[3;<mi] from mi;
    
    cmfrS:select vis:avg(v),cmfr:avg(((hprice-eprice)%eprice)%avg(v)) by sym from dataS;
    cmffS:select cmff:avg(((eprice-lprice)%eprice)%avg(v)) by sym from dataS;
    
    umi:select sym,vis,vil:v,umib:getUmiB[fs;fm;fl;v;cmfr],umis:getUmiS[fs;fm;fl;v;cmff],cmfr,cmff,absfs:abs fs,grvl:sqrt v,cr1:(fs>fm)&(fm>fl)&(fl>0),cr2:((abs fs)>(0.65 * v)),cr3:cmfr<0.3,cf1:(fs<fm)&(fm<fl)&(fl<0),cf2:((abs fs)>(0.65 * v)),cf3:cmff<0.3 from aj[`sym;aj[`sym;cmfrS;cmffS];aj[`sym;aj[`sym;aj[`sym;mfS;mfM];mfL];viL]];
    / show select from umi where (umib>0|umis>0);
    
    result:select from aj[`sym;aj[`sym;aj[`sym;cur_vi;rvi];mi];umi] where time>=(.z.P - TIBUFFER);
    show select[3;>mi] from result;
    show select[3;<mi] from result;
    
    milasttime:select lasttime:(last time) by sym from idx;
    idx,:select time,sym,ask,bid,vis,vil,rvi,cvi,mi,umib,umis,cmfr,cmff,absfs,grvl,cr1,cr2,cr3,cf1,cf2,cf3 from aj[`sym;result;milasttime] where time>lasttime;
    }

.z.ts:getnew
system "t ", string (TI * 1000)
