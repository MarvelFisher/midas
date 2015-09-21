package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.common.staticdata.CodeTableData;

/**
 * Created by Shuwei on 2015/8/25.
 */
public class FTFilter implements IWindFilter {

    @Override
    public boolean codeTableFilter(CodeTableData codeTableData) {
        boolean checkPass = false;
        if (codeTableData != null) {
            switch (codeTableData.getSecurityType()) {
                case WindType.FT_INDEX:
                    checkPass = true;
                    break;
                default:
                    break;
            }
        }
        return checkPass;
    }
}
