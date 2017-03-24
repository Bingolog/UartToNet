package toolkit;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class WmToastUtil {
	 private static final String TAG="WMLIB"; 
	 public static void show_prompt_msg(Context context,String msg)
	 {
	 	Toast toast1 = Toast.makeText(context,
	 	msg, Toast.LENGTH_LONG);
	 	toast1.setGravity(Gravity.BOTTOM, 0, 0);
	 	
	 	toast1.show();
	 }
	 
	 public static  void LOGE(String s)
	 {
		 Log.e(TAG, s);
	 }
	 
	 public static  void LOGI(String s)
	 {
		 Log.i(TAG, s);
	 }
	 public static  void LOGD(String s)
	 {
		 Log.d(TAG, s);
		
	 }
	 public static  void LOGW(String s)
	 {
		 Log.w(TAG, s);
		
	 }
}
