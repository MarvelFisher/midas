package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.common.staticdata.CodeTableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Shuwei on 2015/8/25.
 */
public class FTFilter implements IWindFilter {

    private static final Logger log = LoggerFactory
            .getLogger(FTFilter.class);

    @Override
    public boolean codeTableFilter(CodeTableData codeTableData) {
        boolean checkPass = false;
        if (codeTableData != null) {
            switch (codeTableData.getSecurityType()) {
                case WindType.FT_INDEX:
                    String extractStr = codeTableData.getWindCode().replaceAll("\\D+","");
                    if(extractStr == null || extractStr.length() < 6){
                        log.debug("windCode isn't FT format," + codeTableData.getWindCode());
                        return false;
                    }
                    checkPass = true;
                    break;
                default:
                    break;
            }
        }
        return checkPass;
    }
}
