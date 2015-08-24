package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.common.staticdata.CodeTableData;

/**
 * Created by Shuwei.Kuo on 15/8/11.
 */
public class StockFilter implements IWindFilter{
    @Override
    public boolean codeTableFilter(CodeTableData codeTableData) {
        if (codeTableData == null || codeTableData.getSecurityType() >= 22 || "36".equals(codeTableData.getWindCode().substring(0, 2))) {
            return false;
        }
        return true;
    }
}
