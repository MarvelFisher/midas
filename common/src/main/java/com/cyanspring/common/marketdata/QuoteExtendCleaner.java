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
            quoteExtend.put(QuoteExtDataField.BUYVOL.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.SELLVOL.value())){
            quoteExtend.put(QuoteExtDataField.SELLVOL.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.SETTLEPRICE.value())){
            quoteExtend.put(QuoteExtDataField.SETTLEPRICE.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.OPENINTEREST.value())){
            quoteExtend.put(QuoteExtDataField.OPENINTEREST.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.FREESHARES.value())){
            quoteExtend.put(QuoteExtDataField.FREESHARES.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.TOTOALSHARES.value())){
            quoteExtend.put(QuoteExtDataField.TOTOALSHARES.value(),0);
        }
        if(quoteExtend.fieldExists(QuoteExtDataField.PERATIO.value())){
            quoteExtend.put(QuoteExtDataField.PERATIO.value(),0);
        }
        quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(),Clock.getInstance().now());
        return quoteExtend;
    }
}
