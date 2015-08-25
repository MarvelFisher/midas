package com.cyanspring.adaptor.future.wind.filter;

import com.cyanspring.adaptor.future.wind.WindType;
import com.cyanspring.common.staticdata.CodeTableData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Shuwei on 2015/8/25.
 */
public class FCIFilter implements IWindFilter {

    private List<String> indexList = new ArrayList<String>(Arrays.asList(
            "399001.SZ",
            "399006.SZ",
            "399300.SZ",
            "399905.SZ",
            "999987.SH",
            "999999.SH"
    ));

    FCIFilter() {
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
                case WindType.FC_INDEX:
                case WindType.FC_INDEX_CX:
                    checkPass = true;
                    break;
                default:
                    break;
            }
        }
        return checkPass;
    }
}
