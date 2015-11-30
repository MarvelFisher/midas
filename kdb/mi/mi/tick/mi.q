/ q mi.q 15 8 40 17280 5
/ q mi.q 60 4 30 7200 7 -p 5014

/ TI:15 / seconds
/ TPS:8 / TI = 120 seconds
/ TPM:40 / TI = 10 minutes
/ TPL:17280 / TI = 3 days

\c 25 220

/ get the ticker plant, rdb and history ports, defaults are 5010,5011,5012
/ .u.x:.z.x,(count .z.x)_(":5010";":5011";":5012")
/ .u.x:(":5010";":5011";":5012")
.u.x:.z.x,(count .z.x)_("hdb";":5011";":5012")

if[not system"p";system"p 5013"]

hdb:hopen `$":",.u.x 2
rdb:hopen `$":",.u.x 1

HDBDIR:.u.x 0

getUmiB:{[fs;fm;fl;v;cmfr] ?[(fs>fm)&(fm>fl)&(fl>0)&((abs fs)>(0.65 * v))&(cmfr<0.3);(fs%fm)+(fm%fl);0f]}

getUmiS:{[fs;fm;fl;v;cmff] ?[(fs<fm)&(fm<fl)&(fl<0)&((abs fs)>(0.65 * v))&(cmff<0.3);(fs%fm)+(fm%fl);0f]}

init:{[params]
    TI:params[`TI];
    TPS:params[`TPS];
    TPM:params[`TPM];
    TPL:params[`TPL];
    DATADAYS:params[`DATADAYS];
    TIBUFFER:params[`TIBUFFER];
    
    (`$("idx",string TI)) set ([]time:`timestamp$(); sym:`g#`symbol$(); ask:`float$(); bid:`float$(); vis:`float$(); vil:`float$(); rvi:`float$(); minrvi:`float$(); maxrvi:`float$(); cvi:`float$(); mincvi:`float$(); maxcvi:`float$(); mi:`float$(); minmi:`float$(); maxmi:`float$(); umib:`float$(); maxumib:`float$(); umis:`float$(); maxumis:`float$(); cmfr:`float$(); cmff:`float$(); absfs:`float$(); grvl:`float$(); cr1:`boolean$(); cr2:`boolean$(); cr3:`boolean$(); cf1:`boolean$(); cf2:`boolean$(); cf3:`boolean$());
    (`$("data",string TI)) set (hdb "" sv ("select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote where time > .z.D-"; string DATADAYS));
    (`$("data",string TI)) set (get (`$("data",string TI))) , (rdb "" sv ("select from (select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote) where ({x in -1_x};i) fby sym"));
    
    if[() ~ (key hsym `$ HDBDIR,"/index",string TI);
    (`$("hidx",string TI)) set ([]time:`timestamp$(); sym:`g#`symbol$(); ask:`float$(); bid:`float$(); vis:`float$(); vil:`float$(); rvi:`float$(); minrvi:`float$(); maxrvi:`float$(); cvi:`float$(); mincvi:`float$(); maxcvi:`float$(); mi:`float$(); minmi:`float$(); maxmi:`float$(); umib:`float$(); maxumib:`float$(); umis:`float$(); maxumis:`float$(); cmfr:`float$(); cmff:`float$(); absfs:`float$(); grvl:`float$(); cr1:`boolean$(); cr2:`boolean$(); cr3:`boolean$(); cf1:`boolean$(); cf2:`boolean$(); cf3:`boolean$());
    (`$("minmax",string TI)) set ([]sym:`g#`symbol$(); minrvi:`float$(); maxrvi:`float$(); mincvi:`float$(); maxcvi:`float$(); minmi:`float$(); maxmi:`float$(); maxumib:`float$(); maxumis:`float$());
    :0
    ];
    
    system "l ",HDBDIR,"/index",string TI;
    
    n::`int$(86400%TI); / must be global to be used in fby
    tmph:select from get (`$("indexes",string TI)) where time.date>=.z.D-3;
    (`$("hidx",string TI)) set (select from tmph where ({x in (neg n)#x};i) fby sym);
    tmp:select avgrvi:avg rvi, sdevrvi:sdev rvi, avgcvi:avg cvi, sdevcvi:sdev cvi, avgmi:avg mi, sdevmi:sdev mi, maxumib:max umib, maxumis:max umis by sym from get (`$("hidx",string TI));
    (`$("minmax",string TI)) set (select sym, minrvi:?[(avgrvi-(2*sdevrvi))<0;0f;avgrvi-(2*sdevrvi)], maxrvi:avgrvi+(2*sdevrvi), mincvi:?[(avgcvi-(2*sdevcvi))<0;0f;avgcvi-(2*sdevcvi)], maxcvi:avgcvi+(2*sdevcvi), minmi:avgmi-(2*sdevmi), maxmi:avgmi+(2*sdevmi), maxumib, maxumis from tmp);
    
    system "cd ../../";
    }

getnew:{[funcs;TI;TPS;TPM;TPL;DATADAYS;TIBUFFER]

    new:rdb "" sv ("select from (select[<time] time, sym, ask, bid, sprice, eprice, hprice, lprice, v:(abs sprice-eprice) % sprice from select last time, last ask, last bid, sprice:first price, eprice:last price, hprice:max price, lprice:min price by sym, "; string TI; " xbar time.second from quote where time > .z.P  - "; string TIBUFFER;") where ({x in -1_x};i) fby sym");
    
    if[(count new)=0;
    if[(count select from (get (`$("idx",string TI))) where time.date=.z.D-1)>0; endday[TI]];
    :0];
    
    lasttime:select lasttime:(last time) by sym from get (`$("data",string TI));
    (`$("data",string TI)) set (get (`$("data",string TI))) , (select time,sym,ask,bid,sprice,eprice,hprice,lprice,v from aj[`sym;new;lasttime] where time>lasttime);
    / show count data;
    
    dataS:select from get (`$("data",string TI)) where (funcs[0];i) fby sym;
    dataM:select from get (`$("data",string TI)) where (funcs[1];i) fby sym;
    dataL:select from get (`$("data",string TI)) where (funcs[2];i) fby sym;
    
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
    / show select[3;>mi] from result;
    / show select[3;<mi] from result;
    
    milasttime:select lasttime:(last time) by sym from get (`$("idx",string TI));
    new:(select time,sym,ask,bid,vis,vil,rvi,minrvi,maxrvi,cvi,mincvi,maxcvi,mi,minmi,maxmi,umib,maxumib,umis,maxumis,cmfr,cmff,absfs,grvl,cr1,cr2,cr3,cf1,cf2,cf3 from aj[`sym;aj[`sym;result;milasttime];get (`$("minmax",string TI))] where time>lasttime);
    (`$("idx",string TI)) set (get (`$("idx",string TI))) , new;
    .u.pub[(`$("idx",string TI)); new];
    
    if[(count select from (get (`$("idx",string TI))) where time.date=.z.D-1)>0; endday[TI]; (`$("data",string TI)) set dataL];
    }

endday:{[TI]

    n::`int$(86400%TI); / must be global to be used in fby
    (`$("hidx",string TI)) set (get (`$("hidx",string TI))) , (select from (get (`$("idx",string TI))) where time.date=.z.D-1);
    (`$("hidx",string TI)) set (select from (get (`$("hidx",string TI))) where ({x in (neg n)#x};i) fby sym);
    tmp:select avgrvi:avg rvi, sdevrvi:sdev rvi, avgcvi:avg cvi, sdevcvi:sdev cvi, avgmi:avg mi, sdevmi:sdev mi, maxumib:max umib, maxumis:max umis by sym from get (`$("hidx",string TI));
    (`$("minmax",string TI)) set (select sym, minrvi:?[(avgrvi-(2*sdevrvi))<0;0f;avgrvi-(2*sdevrvi)], maxrvi:avgrvi+(2*sdevrvi), mincvi:?[(avgcvi-(2*sdevcvi))<0;0f;avgcvi-(2*sdevcvi)], maxcvi:avgcvi+(2*sdevcvi), minmi:avgmi-(2*sdevmi), maxmi:avgmi+(2*sdevmi), maxumib, maxumis from tmp);

    (`$("indexes",string TI)) set (select from (get (`$("idx",string TI))) where time.date=.z.D-1);
    .Q.dpft[`$":",HDBDIR,"/index",string TI;.z.D-1;`sym;(`$("indexes",string TI))];
    (`$("idx",string TI)) set (select from (get (`$("idx",string TI))) where time.date=.z.D);
    }

/ 15*4*1000000000=60000000000
params1:`TI`TPS`TPM`TPL`DATADAYS`TIBUFFER!15 8 40 17280 5 60000000000
init[params1]
funcs1:({x in (neg params1[`TPS])#x}; {x in (neg params1[`TPM])#x}; {x in (neg params1[`TPL])#x})

/ 60*4*1000000000=240000000000
params2:`TI`TPS`TPM`TPL`DATADAYS`TIBUFFER!60 4 30 7200 7 240000000000
init[params2]
funcs2:({x in (neg params2[`TPS])#x}; {x in (neg params2[`TPM])#x}; {x in (neg params2[`TPL])#x})

\l u.q
.u.init[]

.z.ts:{
    if[0=(floor (`int$.z.T)%1000) mod 15; getnew[funcs1;params1[`TI];params1[`TPS];params1[`TPM];params1[`TPL];params1[`DATADAYS];params1[`TIBUFFER]]];
    if[0=(floor (`int$.z.T)%1000) mod 60; getnew[funcs2;params2[`TI];params2[`TPS];params2[`TPM];params2[`TPL];params2[`DATADAYS];params2[`TIBUFFER]]];
    }

\t 1000

/ .z.ts:getnew
/ system "t ", string (TI * 1000)
