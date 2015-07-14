/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * <p/>
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataManager extends RefDataService {

    Map<String, RefData> map = new HashMap<String, RefData>();
    private XStream xstream = new XStream(new DomDriver("UTF-8"));
    private boolean changeMode = false;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws Exception {
        log.info("initialising with " + refDataFile);
        XStream xstream = new XStream(new DomDriver());
        File file = new File(refDataFile);
        List<RefData> list;
        if (file.exists()) {
            list = (List<RefData>) xstream.fromXML(file);
        } else {
            throw new Exception("Missing refdata file: " + refDataFile);
        }

        for (RefData refData : list) {
            updateMarginRate(refData);
            map.put(refData.getSymbol(), refData);
        }
    }

    @Override
    public boolean update(String tradeDate) throws Exception {
        return changeMode;
    }

    @Override
    public void uninit() {
        log.info("uninitialising");
        map.clear();
    }

    @Override
    public RefData getRefData(String symbol) {
        return map.get(symbol);
    }

    @Override
    public List<RefData> getRefDataList() {
        return new ArrayList<RefData>(map.values());
    }

    @Override
    public void saveRefDataList(List<RefData> refDataList) {
        changeMode = true;
        saveRefDataToFile(refDataFile, refDataList);
    }

    private void saveRefDataToFile(String path, List<RefData> list) {
        File file = new File(path);
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(list, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
