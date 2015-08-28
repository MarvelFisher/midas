package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;

/**
 * Created by Shuwei.Kuo on 15/7/28.
 */
public class QuoteExtendCleaner implements IQuoteExtendCleaner{
    @Override
    public DataObject clear(DataObject quoteExtend) {
        if(quoteExtend.fieldExists(QuoteExtDataField.BUYVOL.value())){
            quoteExtend.put(QuoteExtDataField.BUYVOL.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.SELLVOL.value())){
            quoteExtend.put(QuoteExtDataField.SELLVOL.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.UNCLASSIFIEDVOL.value())){
            quoteExtend.put(QuoteExtDataField.UNCLASSIFIEDVOL.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.BUYTURNOVER.value())){
            quoteExtend.put(QuoteExtDataField.BUYTURNOVER.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.SELLTURNOVER.value())){
            quoteExtend.put(QuoteExtDataField.SELLTURNOVER.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.UNCLASSIFIEDTURNOVER.value())){
            quoteExtend.put(QuoteExtDataField.UNCLASSIFIEDTURNOVER.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.FTURNOVER.value())){
            quoteExtend.put(QuoteExtDataField.FTURNOVER.value(),(long)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.SETTLEPRICE.value())){
            quoteExtend.put(QuoteExtDataField.SETTLEPRICE.value(),(double)0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.OPENINTEREST.value())){
            quoteExtend.put(QuoteExtDataField.OPENINTEREST.value(),(long)0);
        }
        quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(),Clock.getInstance().now());
        return quoteExtend;
    }
}
