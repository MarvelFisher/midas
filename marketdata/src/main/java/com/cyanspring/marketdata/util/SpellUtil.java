package com.cyanspring.marketdata.util;

import com.cyanspring.marketdata.type.WindDef;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class SpellUtil {
    /**
     * convert Chinese Full Name to Spell Name
     *
     * @param fullName
     * @param isSimple
     * @return
     */
    public static String getSpellName(String fullName, boolean isSimple) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        String spellName = "";
        try {
            for (int i = 0; i < fullName.length(); i++) {
                char word = fullName.charAt(i);
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word, format);
                if (pinyinArray == null) { //pinyin4j不能处理非中文
                    spellName = spellName + String.valueOf(word).trim();
                    if (!isSimple) spellName = spellName + " ";
                    continue;
                }
                if (isSimple) {
                    spellName = spellName + pinyinArray[0].substring(0, 1);
                } else {
                    spellName = spellName + pinyinArray[0] + " ";
                }
            }
            if(spellName.contains(WindDef.STOCK_EX_DIVIDENT)) spellName = spellName.replace(WindDef.STOCK_EX_DIVIDENT, "");
            if(spellName.contains(WindDef.STOCK_EX_RIGHT)) spellName = spellName.replace(WindDef.STOCK_EX_RIGHT, "");
            if(spellName.contains(WindDef.STOCK_EX_RIGHT_DIVIDENT)) spellName = spellName.replace(WindDef.STOCK_EX_RIGHT_DIVIDENT,"");
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return changeFullToHalf(spellName);
    }

    /**
     * change String FullWidth to HalfWidth
     *
     * @param originStr
     * @return destStr
     * @see <a href="http://www.utf8-chartable.de/unicode-utf8-table.pl?start=65280&number=128&unicodeinhtml=dec">UTF-8 encoding table</a>
     */
    public static String changeFullToHalf(String originStr) {
        for (char c : originStr.toCharArray()) {
            originStr = originStr.replaceAll("　", " ");
            if ((int) c >= 65281 && (int) c <= 65374) {
                originStr = originStr.replace(c, (char) (((int) c) - 65248));
            }
        }
        return originStr;
    }
}
