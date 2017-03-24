package toolkit;

public class MiscFunction {
	
	 /**
	  * 将int型IP转换成字符串型的IP地址
	 * @param i
	 * @return string型的IP
	 */
	public static String intToIp(int i) {       
        
        return (i & 0xFF ) + "." +       
      ((i >> 8 ) & 0xFF) + "." +       
      ((i >> 16 ) & 0xFF) + "." +       
      ( i >> 24 & 0xFF) ;  
   }   

}
