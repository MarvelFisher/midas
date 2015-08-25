package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.common.staticdata.CodeTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Shuwei.Kuo on 15/8/11.
 */
public class SCFilter implements IWindFilter {

    private List<String> indexList = new ArrayList<String>(Arrays.asList(
            "399001.SZ",
            "399006.SZ",
            "399300.SZ",
            "399905.SZ",
            "999987.SH",
            "999999.SH"
    ));

    SCFilter() {
        Collections.sort(indexList);
    }

    @Override
    public boolean codeTableFilter(CodeTableData codeTableData) {
        boolean checkPass = false;
        if (codeTableData != null) {
            switch (codeTableData.getSecurityType()) {
                case WindType.IC_INDEX:
                    if (Collections.binarySearch(indexList, codeTableData.getWindCode()) >= 0) {
                        checkPass = true;
                    }
                    break;
                case WindType.SC_SHARES_A:
                case WindType.SC_SHARES_S:
                case WindType.SC_SHARES_G:
                    checkPass = true;
                    break;
                default:
                    break;
            }
        }
        return checkPass;
    }
}
