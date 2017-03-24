package toolkit;


// Copyright (C) 2012 WHTY


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 閺夌儐鍓氬畷鏌ュ箼瀹ュ嫮绋婇柣銊ュ鎼存粓宕濋埡浣告瘣闁轰緤鎷烽弶鈺傜☉閸╂绋婄�顔斤紵闁汇劌瀚ù鍡涘箲閿燂拷濞戞挸绉烽—鍛村礉閻樻彃寮抽柛蹇旀构缁剟寮憴鍕�)
 *
 */
public abstract class Converts {

    /**
     * 闁轰礁鐡ㄩ弳鐔告姜椤戣儻绀嬮柟绋挎搐閻ｉ箖姊归崹顔碱唺闁汇劌瀚悺褏绮敂鑳湂
     *
     * @param n
     *            闁轰礁鐡ㄩ弳锟�     * @param len
     *            闁圭娲ら悾楣冩⒐閸喖顔�
     * @return
     */
    public static String intToString(int n, int len) {
        String str = String.valueOf(n);
        int strLen = str.length();
        String zeros = "";
        for (int loop = len - strLen; loop > 0; loop--) {
            zeros += "0";
        }
        if (n >= 0) {
            return zeros + str;
        }
        else {
            return "-" + zeros + str.substring(1);
        }
    }

    /**
     * 閻庢稒顨犳俊顓㈠极閹殿喚鐭嬮弶鐑嗗厸鐠愶拷6閺夆晜绋戦崺锟�     *
     * @param bytes
     *            閻庢稒顨犳俊顓㈠极閹殿喚鐭�
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        int len = bytes.length;
        for (int j = 0; j < len; j++) {
            if ((bytes[j] & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(bytes[j] & 0xff));
        }
        return buff.toString();
    }

    /**
     * 閻庢稒顨熼浣圭▔閼艰埖绁柟璇℃瀺鐠愮喓锟藉Δ鍕濋柡浣瑰缁拷
     * <p>
     * stringToBytes("0710BE8716FB"); return: b[0]=0x07;b[1]=0x10;...b[5]=0xfb;
     */
    public static byte[] stringToBytes(String string) {
        if (string == null || string.length() == 0
                || string.length() % 2 != 0) {
            return null;
        }
        int stringLen = string.length();
        byte byteArrayResult[] = new byte[stringLen / 2];
        StringBuffer sb = new StringBuffer(string);
        String strTemp;
        int i = 0;
        while (i < sb.length() - 1) {
            strTemp = string.substring(i, i + 2);
            try {
				
			
            byteArrayResult[i / 2] = (byte) Integer.parseInt(strTemp, 16);
            i += 2;
            } catch ( NumberFormatException e) {
				// TODO: handle exception
            	return null;
			}
        }
        return byteArrayResult;
    }

    public static int hex2Int(byte []data)
    {
    	return ((data[0]<<24) | (data[1]<<16) | (data[2]<<8) | data[3]);
    }
    /**
     * 闁轰焦娼欓懜鐗堟姜椤掍礁搴婂☉鎾虫惈閻⊙囨嚍閸屾稒娈剁紓渚婃嫹
     * <p>
     * n=1000000000(0x3B9ACA00) return: byte[0]:3b byte[1]:9a byte[2]:ca
     * byte[3]:00
     * <p>
     * 婵炲鍔嶉崜锟�闁轰焦娼欓懜浼存嚑閸愩劍绾☉鎿勬嫹 [ -2^32, 2^32-1]
     */
    public static byte[] intToBytes(int n) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(n);
        return bb.array();
    }
    
    public static byte[] shortToBytes(short n) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(n);
        return bb.array();
    }

    /**
     * Long閺夌儐鍓氬畷鍙夌▔閸濆嫮鎽熼柤鍝勫�閺嗙喓绱掗敓锟�    *
     * @param l
     * @return
     */
    public static byte[] longToBytes(long l) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(l);
        return bb.array();
    }

    /**
     * 閻忓繐妫欓弳锝夊极閹峰本绁☉鎿勬嫹6閺夆晜绋栭、鎴﹀极閺夋寧鍊垫鐐存构娴滄帡骞愰崶褏鏆伴梻锟界仢鐎硅櫕娼婚弬鎸庣闁挎稑鐗嗙紞瀣拷閻愮儤顎庨梻锟界仢鐎硅櫕寰勮缁剟骞愰崶褏鏆伴梻锟界仢鐎规娊寮捄鍝勬锭閺夆晜鏌ㄥú鏍ㄧ鎼淬垺姹炲ù锝呯Т缁辨垶鎱ㄧ�顐㈢樄閻庤宀搁弳杈ㄦ償閿旂偓鐣遍柛濠勩�缁憋拷
     *
     * @param val
     *            int 鐎垫澘鎳撳ù鍡涘箲閵忊剝娈婚柡渚婃嫹
     * @param len
     *            int 闁圭娲ら悾楣冩⒐閸喖顔�
     * @return String
     */
    public static String intToHex(int val, int len) {
        String result = Integer.toHexString(val).toUpperCase();
        int rlen = result.length();
        if (rlen > len) {
            return result.substring(rlen - len, rlen);
        }
        if (rlen == len) {
            return result;
        }
        StringBuffer strBuff = new StringBuffer(result);
        for (int i = 0; i < len - rlen; i++) {
            strBuff.insert(0, '0');
        }
        return strBuff.toString();
    }

    /**
     * 閻忓繐妫濋弳閬嶅极鐎涙ɑ娈堕弶鐑嗗厸鐠愶拷6閺夆晜绋栭、鎴﹀极閺夋寧鍊垫鐐存构娴滄帡骞愰崶褏鏆伴梻锟界仢鐎硅櫕娼婚弬鎸庣闁挎稑鐗嗙紞瀣拷閻愮儤顎庨梻锟界仢鐎硅櫕寰勮缁剟骞愰崶褏鏆伴梻锟界仢鐎规娊寮捄鍝勬锭閺夆晜鏌ㄥú鏍ㄧ鎼淬垺姹炲ù锝呯Т缁辨垶鎱ㄧ�顐㈢樄閻庤宀搁弳杈ㄦ償閿旂偓鐣遍柛濠勩�缁憋拷
     *
     * @param val
     *            int 鐎垫澘鎳撳ù鍡涘箲閵忋倖姣愰柡浣哥摠閺嗭拷
     * @param len
     *            int 闁圭娲ら悾楣冩⒐閸喖顔�
     * @return String
     */
    public static String longToHex(long val, int len) {
        String result = Long.toHexString(val).toUpperCase();
        int rlen = result.length();
        if (rlen > len) {
            return result.substring(rlen - len, rlen);
        }
        if (rlen == len) {
            return result;
        }
        StringBuffer strBuff = new StringBuffer(result);
        for (int i = 0; i < len - rlen; i++) {
            strBuff.insert(0, '0');
        }
        return strBuff.toString();
    }

    /**
     * @param hex
     *            閻忓骏鎷�閺夆晜绋戦崺妤呮儍閸掔劌cii 閺夌儐鍓氶崹姘▔椤撶喐鐎�
     * @return
     */
    public static String hexToAscii(String hex) {
        byte[] buffer = new byte[hex.length() / 2];
        String strByte;

        for (int i = 0; i < buffer.length; i++) {
            strByte = hex.substring(i * 2, i * 2 + 2);
            buffer[i] = (byte) Integer.parseInt(strByte, 16);
        }

        return new String(buffer);
    }

    /**
     * @param hex
     *            婵絽绻嬬悮杈ㄧ▔椤忓嫮鎽熼柤鍝勫�缁绘鎮扮仦绛嬫█闁荤儑鎷�     * @return
     */
    public static byte[] hexToBytes(String hex) {
        byte[] buffer = new byte[hex.length() / 2];
        String strByte;

        for (int i = 0; i < buffer.length; i++) {
            strByte = hex.substring(i * 2, i * 2 + 2);
            buffer[i] = (byte) Integer.parseInt(strByte, 16);
        }

        return buffer;
    }

    /**
     * 閻庢稒顨犳俊顓㈠极閹殿喚鐭嬮弶鐑嗗厸鐠愶拷6閺夆晜绋戦崺锟�     *
     * @param bytes
     *            閻庢稒顨犳俊顓㈠极閹殿喚鐭�
     * @return
     */
    public static String asciiToHex(String asciiString) {
        if (asciiString == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        byte[] bytes = asciiString.getBytes();
        int len = bytes.length;
        for (int j = 0; j < len; j++) {
            if ((bytes[j] & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(bytes[j] & 0xff));
        }
        return buff.toString();
    }

    /**
     * 闁告瑦鐗曞畷鍕交濞戞ê鐓戦柡浣规緲娴兼捇寮０浣虹Т闁挎稑鑻稊蹇曟偘閿燂拷
     *
     * @param n
     *            闁告ぞ娴囩换姗�礆閼稿灚娈�
     * @param number
     *            濞达絽绉甸弳锟�     * @return
     */
    public static String getDecimal(int n, int number) {
        Integer nl = Integer.valueOf(n);
        String strN = nl.toString();
        int j = number - strN.length();
        for (int i = 0; i < j; i++) {
            strN = "0" + strN;
        }
        return strN;
    }

    public static String getLengthTLV(int n) {
        int ns = n / 2;
        String hex = "";
        if (ns < 128) {
            hex = intToHex(ns, 2);
        }
        else if (ns >= 128 && ns < 256) {
            hex = "81" + intToHex(ns, 2);
        }
        else if (ns >= 256) {
            hex = "82" + intToHex(ns, 4);
        }
        return hex;
    }



    /**
     * 閻忓繐妫楅悺褔鎳為崒娑欐缂備礁瀚ù鍡涘箲椤ゅ秷绀嬮柛妞剧閸欐碍娼诲☉妯虹厬閻庢稒顨熼浣圭▔閿燂拷     *
     * @param b
     * @return
     */
    public static String bytesToString(byte b[]) {
    	if(b == null)
    	{
    		return "";
    	}
        StringBuilder sb = new StringBuilder();
        for (byte element : b) {
            int tmp = element & 0xff;
            sb.append(String.format("%02X", tmp));
        }
        return sb.toString();
    }
    
    
    /**
     * 閻忓繐妫楅悺褔鎳為崒娑欐缂備礁瀚ù鍡涘箲椤ゅ秷绀嬮柛妞剧閸欐碍娼诲☉妯虹厬閻庢稒顨熼浣圭▔閿燂拷     *
     * @param b
     * @return
     */
    public static String bytesToStringAddSpace(byte b[]) {
    	if(b == null)
    	{
    		return "";
    	}
        StringBuilder sb = new StringBuilder();
        for (byte element : b) {
            int tmp = element & 0xff;
            sb.append(String.format("%02X", tmp));
            sb.append(' ');
        }
        return sb.toString();
    }


    /**
     * 濠靛鍋勯崢锟�闁轰胶澧楀畵渚�晬鐏炵瓔娓鹃柡瀣矌缁劑寮稿鍕闁硅鍠栧锟犲及閿熶粙鎯冮崟顐嫹闁轰礁搴滅槐婵囩▔瀹ュ懎鏅欓弶鈺傜椤㈡垶娼婚挊澶婎灊,濠碘�鍊归悘澶嬬▔瀹ュ棙笑,鐎归潻绠掕棢0闁挎稑鐬煎ú鍧楀礆閻楀牊娈堕柟璇″枛濞硷繝鎯冮崟顖涙瘣閹艰揪闄勫Σ锟介柣銊ュ閿熶粙寮懜顒婃嫹
     *
     * @param data
     *            鐎垫澘鎳庨敐鐐哄礂閿熶粙鎯冮崟顒佹闁圭櫢鎷�     * @return
     */
    public static String padding0(String data1) {
        String data = data1;
        int padlen = 8 - (data.length()) % 8;
        if (padlen != 8) {
            String padstr = "";
            for (int i = 0; i < padlen; i++) {
                padstr += "0";
            }
            data = padstr + data;
            return data;
        }
        else {
            return data;
        }
    }

    /**
     * 閻忓繐妫欓弳锝夊极閹峰本绁☉鎿勬嫹6閺夆晜绋栭、鎴﹀极閺夋寧鍊垫鐐存构娴滄帡骞愰崶褏鏆伴梻锟界仢鐎硅櫕娼婚弬鎸庣闁挎稑鐗嗙紞瀣拷閻愮儤顎庨梻锟界仢鐎硅櫕寰勮缁剟骞愰崶褏鏆伴梻锟界仢鐎规娊寮捄鍝勬锭閺夆晜鏌ㄥú鏍ㄧ鎼淬垺姹炲ù锝呯Т缁辨垶鎱ㄧ�顐㈢樄閻庤宀搁弳杈ㄦ償閿旂偓鐣遍柛濠勩�缁憋拷
     *
     * @param val
     *            鐎垫澘鎳撳ù鍡涘箲閵忊剝娈婚柡渚婃嫹
     * @param len
     *            闁圭娲ら悾楣冩⒐閸喖顔�
     * @return String
     */
    public static String int2HexStr(int val, int len) {
        String result = Integer.toHexString(val).toUpperCase(); // EEEEEEEEE
        int rLen = result.length();
        if (rLen > len) {
            return result.substring(rLen - len, rLen);
        }
        if (rLen == len) {
            return result;
        }
        StringBuffer strBuff = new StringBuffer(result);
        for (int i = 0; i < len - rLen; i++) {
            strBuff.insert(0, '0');
        }
        return strBuff.toString();
    }

}
