/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.cyanspring.common.business.RefDataField;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataUtil {
	enum Category{
		STOCK
	}
	public static String getOnlyChars(String symbol) {
		Pattern pattern = Pattern.compile("[a-zA-Z]*");
		Matcher matcher = pattern.matcher(symbol);
		if (matcher.find())
			return matcher.group(0);
		return null;
	}

    public static String getCategory(String symbol){
    	
    	if(!StringUtils.hasText(symbol))
    		return null;
    	
		String category =  symbol.replaceAll(".[A-Z]+$", "").replaceAll("\\d", "");
		
		//Stock Category
		if(!StringUtils.hasText(category)){
			return Category.STOCK.name();
		}
		
		//Future Category
		if(category.length() > 2 ){
			return category.substring(0, 2);
		}else{
			return category;
		}
	}
//	private static ArrayList<Double> getVolProfile() {
//		ArrayList<Double> volProfile;
//		volProfile = new ArrayList<Double>();
//		volProfile.add(10.0);
//		volProfile.add(18.0);
//		volProfile.add(25.0);
//		volProfile.add(27.0);
//		volProfile.add(31.0);
//		volProfile.add(36.0);
//		volProfile.add(41.0);
//		volProfile.add(45.0);
//		volProfile.add(51.0);
//		volProfile.add(57.0);
//
//		volProfile.add(61.0);
//		volProfile.add(63.0);
//		volProfile.add(66.0);
//		volProfile.add(69.0);
//		volProfile.add(72.0);
//		volProfile.add(76.0);
//		volProfile.add(78.0);
//		volProfile.add(83.0);
//		volProfile.add(87.0);
//		volProfile.add(100.0);
//
//		return volProfile;
//	}
	
	public static void main(String args[]) {
		XStream xstream = new XStream(new DomDriver());
		ArrayList<RefData> list;

		list = new ArrayList<RefData>();
		RefData refData;
		
		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "0005.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 400);
		list.add(refData);
		
		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "0016.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 1000);
		list.add(refData);

		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "1398.HK");
		refData.put(RefDataField.LOT_SIZE.value(), 1000);
		list.add(refData);

		File file = new File("refdata/refDataSample.xml");
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(list, os);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
