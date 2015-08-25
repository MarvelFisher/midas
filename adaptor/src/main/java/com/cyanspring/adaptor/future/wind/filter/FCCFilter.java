package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.common.staticdata.CodeTableData;

/**
 * Created by FDT on 2015/8/25.
 */
public class FCCFilter implements IWindFilter {

    @Override
    public boolean codeTableFilter(CodeTableData codeTableData) {
        boolean checkPass = false;
        if (codeTableData != null) {
            switch (codeTableData.getSecurityType()) {
                case WindType.FC_COMMODITY:
                case WindType.FC_COMMODITY_CX:
                    checkPass = true;
                    break;
                default:
                    break;
            }
        }
        return checkPass;
    }
}
