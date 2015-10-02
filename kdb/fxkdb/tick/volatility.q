/ q ~/q/fxkdb/tick/volatility.q -t 60000
/ query example: select time,x,m,s,sd:sqrt(s%(k-1)) from volaty where sym=`EURRUB

volaty:([]time:`minute$(); sym:`g#`symbol$(); x:`float$(); m:`float$(); s:`float$(); k:`int$())

\l u.q
.u.init[]

calcm:{[x;pm;k]
    pm+(x-pm)%k
    }

calcs:{[x;m;pm;ps]
    ps+(x-pm)*(x-m)
    }

gen:{
    xs:h"select abs last price - first price by time.minute,sym from quote where time.minute=(`minute$.z.P)-1";
    pxs:select last x, last m, last s, pk:last k by sym from volaty;
    cur:aj[`sym;xs;pxs];
    new:select time,sym,x,m,s:calcs[x;m;pm;ps],k from select time:minute,sym,x:price,m:calcm[price;m|0f;(pk|0)+1],pm:m|0f,ps:s|0f,k:(pk|0)+1 from cur;
    volaty,:new;
    .u.pub[`volaty;select time,symbol:sym,scale:x%sd from select time,sym,x,sd:sqrt(s%(k-1)) from new where k>2];
    }

getdaylist:{
    select time, symbol:sym, scale:x%sd from select time,sym,x,sd:sqrt(s%(k-1)) from volaty where k > 2
    }

h:hopen `::5011

.z.ts:gen
