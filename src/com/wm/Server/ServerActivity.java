package com.wm.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import toolkit.Converts;
import toolkit.MiscFunction;
import toolkit.WmToastUtil;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.AndroidSDK.UartConfigMgr;
import com.wm.uart2net.R;

/**
 * @author Administrator
 *
 */
public class ServerActivity extends Activity {
	private TextView TextViewIpDisplay;
	private EditText mPortConfigure;
	private Button  BtListen;
	private int port;
	boolean start = true;
	
	/****************************************/
	private ArrayAdapter<String> mArrayAdapterCmd=null;
	private EditText mEditText;
	private ListView listView_at_show;

	private CheckBox mCheckBoxRecvHex;
	private CheckBox mCheckBoxSendHex;

	private boolean RecvHex = false;
	private boolean SendHex = false;

	private final String OPEN_UART = "打开串口";
	private final String CLOSE_UART = "关闭串口";

	private Button mButtonClear;
	private Button mButtonConfigOK;
	private Button mButtonSend;

	private Spinner mSpinnerUartName;
	private Spinner mSpinnerBd;
	private Spinner mSpinnerDataBit;
	private Spinner mSpinnerChkBit;
	private Spinner mSpinnerStopBit;

	private String[] devName = {"ttySAC2", "ttySAC3", "ttySAC1", "ttySAC0",
			"ttyUSB0", "ttyUSB1", "ttyUSB2", "ttyUSB3", "ttyUSB4", "ttyUSB5",
			"ttyUSB6", "ttyUSB7", "ttyUSB8", "ttyUSB9" };
	private static final String[] bd = { "115200", "57600", "38400", "19200",
			"14400", "18400", "9600", "4800" };
	private static final String[] dataBit = { "8", "7", "6", "5" };
	private static final String[] ChkBit = { "无", "奇校验", "偶校验" };
	private static final String[] StopBit = { "1", "2" };

	private ArrayAdapter<String> adapterUartName;
	private ArrayAdapter<String> adapterBd;
	private ArrayAdapter<String> adapterDataBit;
	private ArrayAdapter<String> adapterChkBit;
	private ArrayAdapter<String> adapterStopBit;

	String devNameGet = "/dev/" + devName[0];
	long baud = Integer.parseInt(bd[0], 10);
	int dataBits = Integer.parseInt(dataBit[0], 10);
	int stopBits = Integer.parseInt(StopBit[0], 10);
	ArrayList<UartSocketThread> list = new ArrayList<UartSocketThread>();
	
	private List<String> dataAtCmdUser = new ArrayList<String>();//动态字符串数组	
	
	private UartConfigMgr mUartConfigMgr = new UartConfigMgr();
	private int mUartFd = -1;
	private ReadThread mReadThread = null;	
	Context mContext;	
	byte[] bufferToShow = new byte[8 * 1024];
	/**********************************************/
	@Override	
	public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.server);
        TextViewIpDisplay = (TextView)findViewById(R.id.local_ip_display);
        mPortConfigure = (EditText)findViewById(R.id.local_port_config);
        BtListen = (Button)findViewById(R.id.btnListen);
        mPortConfigure.setInputType(EditorInfo.TYPE_CLASS_PHONE); // 设置端口号输入只能是数字和小数点；
        
        
        initConfigUartViews();
        
        BtListen.setOnClickListener(new OnClickListener(){
			public void onClick(View v)
			{   
				if(mPortConfigure.getText().toString().length()==0) {
					
				Toast.makeText(ServerActivity.this, "请设置端口号！", Toast.LENGTH_SHORT).show();
				
				} else{
					port = Integer.valueOf(mPortConfigure.getText().toString());
					  //获取wifi服务  
			        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
			        //判断wifi是否开启  
			        if (!wifiManager.isWifiEnabled()) {  
			        wifiManager.setWifiEnabled(true);    
			        }  
			        WifiInfo wifiInfo = wifiManager.getConnectionInfo();       
			        int ipAddress = wifiInfo.getIpAddress();  			        
			        String ip = MiscFunction.intToIp(ipAddress);  			       
			        TextViewIpDisplay.setText(ip); 
			       	
				}	
			}	
		});		        
        
	}
	 
	 /**************************初始化函数************************/
		void initConfigUartViews() {
			
			mSpinnerUartName = (Spinner) findViewById(R.id.serial_dev_name);
			mSpinnerBd = 	   (Spinner) findViewById(R.id.serial_dev_bd);
			mSpinnerDataBit =  (Spinner) findViewById(R.id.serial_data_bit);
			mSpinnerChkBit =   (Spinner) findViewById(R.id.serial_check_bit);
			mSpinnerStopBit =  (Spinner) findViewById(R.id.serial_stop_bit);
			
			mCheckBoxRecvHex = (CheckBox) findViewById(R.id.checkBoxHexRecv);
			mCheckBoxSendHex = (CheckBox) findViewById(R.id.checkBoxHexSend);
			mCheckBoxRecvHex.setOnCheckedChangeListener(mOnCheckedChangeListener);
			mCheckBoxSendHex.setOnCheckedChangeListener(mOnCheckedChangeListener);
			
			mButtonConfigOK = (Button) this.findViewById(R.id.buttonUartConfgOK);
			mButtonClear =    (Button) this.findViewById(R.id.buttonClear);		
			mButtonSend = 	  (Button)this.findViewById(R.id.buttonSend);		
			mButtonConfigOK.setOnClickListener(mbuttonOnClickListener);	
			mButtonClear.setOnClickListener(mbuttonOnClickListener);	
			mButtonSend.setOnClickListener(mbuttonOnClickListener);			
			mEditText = (EditText) findViewById(R.id.editTextDataInput);		
			listView_at_show = (ListView) findViewById(R.id.list_at_show);		
			mArrayAdapterCmd = new ArrayAdapter<String>(this,R.layout.at_cmd_view_list, dataAtCmdUser);		
			listView_at_show.setAdapter(mArrayAdapterCmd);	
			
			/***************************串口号配置******************************/
			// 将可选内容与ArrayAdapter连接起来
			// 设置下拉列表的风格
			// 添加事件Spinner事件监听
			adapterUartName = new ArrayAdapter<String>(this, R.layout.wm_spinner,devName);		
			adapterUartName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
			mSpinnerUartName.setOnItemSelectedListener(new SpinnerSelectedListener());
			mSpinnerUartName.setAdapter(adapterUartName);
			
			/***************************波特率配置******************************/		
			adapterBd = new ArrayAdapter<String>(this, R.layout.wm_spinner, bd);		
			adapterBd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
			mSpinnerBd.setOnItemSelectedListener(new SpinnerSelectedListener());
			mSpinnerBd.setAdapter(adapterBd);
			
			/***************************数据位配置******************************/
			adapterDataBit = new ArrayAdapter<String>(this, R.layout.wm_spinner,dataBit);
			adapterDataBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mSpinnerDataBit.setOnItemSelectedListener(new SpinnerSelectedListener());
			mSpinnerDataBit.setAdapter(adapterDataBit);
			
			/***************************校验位配置******************************/	
			adapterChkBit = new ArrayAdapter<String>(this, R.layout.wm_spinner,	ChkBit);		
			adapterChkBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
			mSpinnerChkBit.setOnItemSelectedListener(new SpinnerSelectedListener());
			mSpinnerChkBit.setAdapter(adapterChkBit);
			mSpinnerChkBit.setClickable(false);
			
			/***************************停止位配置******************************/		
			adapterStopBit = new ArrayAdapter<String>(this, R.layout.wm_spinner,StopBit);	
			adapterStopBit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
			mSpinnerStopBit.setOnItemSelectedListener(new SpinnerSelectedListener());
			mSpinnerStopBit.setAdapter(adapterStopBit);
		}
		
		@Override
		protected void onStart() {		
			super.onStart();
			//启动串口，传入参数true;
			CtrlUart(true);
		}
		class SpinnerSelectedListener implements OnItemSelectedListener {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg0.getId()) {
				case R.id.serial_dev_name:
					devNameGet = "/dev/" + devName[arg2];				
					break;
				case R.id.serial_dev_bd:
					baud = Integer.parseInt(bd[arg2]);				
					break;
				case R.id.serial_data_bit:
					dataBits = Integer.parseInt(dataBit[arg2]);
					break;
				case R.id.serial_stop_bit:
					stopBits = Integer.parseInt(StopBit[arg2]);
					break;
				}			
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		}
	/******************************按键监听**************************/
		OnClickListener mbuttonOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View arg0) {			
				switch (arg0.getId()) {
				case R.id.buttonUartConfgOK:
					
					mUartConfigMgr.SaveConfig(devNameGet, baud, dataBits, stopBits);
					if (mUartFd > 0) {
						CtrlUart(false);
					} else {
						CtrlUart(true);
					}
					break;

				case R.id.buttonClear:	
					
					dataAtCmdUser.clear();
					mArrayAdapterCmd.notifyDataSetChanged();			
					break;
		
				case R.id.buttonSend:
					
					start_send();				
					break;

				}
			}
		};
	/************************可选框选中监听，勾选十六进制发送或十六进制显示*************************/
		OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {			
				int id = arg0.getId();
				switch (id) {	
				case R.id.checkBoxHexRecv:
					if (arg1) {
						RecvHex = true;				

					} else {
						RecvHex = false;				
					}
					break;
					
				case R.id.checkBoxHexSend:
					if (arg1) {
						SendHex = true;					
					} else {
						SendHex = false;				
					}
					break;
				}
			}
		};
		
		@Override
		protected void onStop() {		
			super.onStop();
			CtrlUart(false);
		}
		/**************启动程序打开串口***************/
		public void CtrlUart(boolean start) {
			if (start) {			
				mUartFd = HardwareControler.openSerialPort(
						mUartConfigMgr.getDevName(), mUartConfigMgr.getBaud(),
						mUartConfigMgr.getDataBits(), mUartConfigMgr.getStopBits());	
				
				Log.e("MainActivity ","mUartConfigMgr.getDevName()="+mUartConfigMgr.getDevName());

				if (mUartFd < 0) {			
					toastInfo("串口打开失败");
				} else {
					mReadThread = new ReadThread(mUartFd);
					mReadThread.start();			
					toastInfo("串口打开成功");				
					mButtonConfigOK.setText(CLOSE_UART);
					set_uart_spinners_enable_disable(false);
				}
			} else {
				if (mReadThread != null)
					mReadThread.interrupt();	

				HardwareControler.close(mUartFd);
				mButtonConfigOK.setText(OPEN_UART);
				mUartFd = -1;			
				toastInfo("串口关闭");
				set_uart_spinners_enable_disable(true);
			}
		}
		/**Spinner初始化状态**/
		void set_uart_spinners_enable_disable(boolean b) {
			mSpinnerUartName.setClickable(b);
			mSpinnerBd.setClickable(b);
			mSpinnerDataBit.setClickable(b);
			mSpinnerChkBit.setClickable(b);
			mSpinnerStopBit.setClickable(b);
		}
		
		private class ReadThread extends Thread {

			public ReadThread(int uartFd) {
				super();
			}
			@Override
			public void run() {
				super.run();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while (!isInterrupted()) {
					int lastindex = 0;
					// 直接输出
					if (System.currentTimeMillis() - lastshowUartData > 600
							&& bos.size() > 0) {

						onStringReceived(bos.toByteArray(), bos.size());// new																	
						bos.reset();
						System.out.println("last >600 ms:" + bos.size());
					}

					byte[] retbts = readbytesForUart();
					if (retbts != null) {
						for (int i = 0; i < retbts.length; i++) {
							//
							if (retbts[i] == '\n') {
								// buffer have data,copy out and add new Line
								if (bos.size() > 0) {
									byte[] tmp = new byte[bos.size() + i];								
									System.arraycopy(bos.toByteArray(), 0, tmp, 0,
											bos.size());
									System.arraycopy(retbts, lastindex, tmp,
											bos.size(), i);
									// 输出一行加之前缓存
									onStringReceived(tmp, bos.size() + i);// new								
									bos.reset();							
									lastindex = i;

								} else 
								{
									// 直接输出这一行数据
									byte[] tmp = new byte[i - lastindex];							
									System.arraycopy(retbts, lastindex, tmp, 0, i
											- lastindex);
									onStringReceived(tmp, bos.size() + i);// new							
									lastindex = i;
								}
							}
						}
						if (lastindex < retbts.length - 1) {
							byte[] tmp = new byte[retbts.length - lastindex];
							
							System.arraycopy(retbts, lastindex, tmp, 0,
									retbts.length - lastindex);
							try {
								bos.write(tmp);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					try {
						Thread.sleep(180);
					} catch (InterruptedException e) {}
				}
			}
		}	
		
		public byte[] readbytesForUart() {
			int size = HardwareControler.read(mUartFd, bufferToShow,
					bufferToShow.length);
			if (size > 0) {
				byte[] ret = new byte[size];
				System.arraycopy(bufferToShow, 0, ret, 0, size);
				return ret;
			} else {
				return null;
			}
			
		}

		
		Context getMyContext() {
			return this;
		}
		/************************Toast消息提示*******************************/
		protected void toastInfo(final String msg) {
			runOnUiThread(new Runnable() {
				public void run() {
					WmToastUtil.show_prompt_msg(getMyContext(), msg);
				}
			});
		}
		
		/*********************读取数据显示在listView上的方法************************/
		long lastshowUartData = 0;
		protected void onStringReceived(final byte[] buffer, final int size) {
			lastshowUartData = System.currentTimeMillis();
			runOnUiThread(new Runnable() {

				public void run() {			
					if (RecvHex) // 十六进制显示
					{
						String hex = Converts.bytesToStringAddSpace(buffer);
						dataAtCmdUser.add(hex);//将hex添加到动态数组里
					} else {
						String hex = "";
						try {
							hex = new String(buffer, "GB2312");
						} catch (UnsupportedEncodingException e) {					
						}
						dataAtCmdUser.add(hex);
					}

					mArrayAdapterCmd.notifyDataSetChanged();
					listView_at_show.setSelection(listView_at_show.getBottom());
				}
			});
		}
		/************************发送数据的方法************************/
		void start_send() {		
			if (mEditText.getText().toString().length() > 0) {
				if (mUartFd > 0) {

					String snd = mEditText.getText().toString();
					String hex = snd;
					Log.e("MainActivity",hex);
					byte sendbytes[] = null;
					if (SendHex) {					
						sendbytes = Converts.stringToBytes(snd);
						hex = Converts.bytesToString(sendbytes);
					} else {					
							sendbytes = snd.getBytes();
						
					}

					if (hex.length() == 0) {
						WmToastUtil.show_prompt_msg(ServerActivity.this, "格式输入错误");
						return;
					}			
					int ret = HardwareControler.write(mUartFd, sendbytes);	
				
				} else {
					Toast.makeText(ServerActivity.this, "串口没有打开", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(ServerActivity.this, "请输入数据", Toast.LENGTH_SHORT).show();
			}
		}
		/**
		 * @author 服务器监听子线程
		 *
		 */
		class SocketServerThread extends Thread {
			ServerSocket serverSocket = null;
			byte[] byts = new byte[1024];

			// Socket socket = null;
			public void closeServer() {
				start = false;
				if (null != serverSocket) {
					try {
						// if(socket!= null)
						// {
						// socket.close();
						// }

						serverSocket.close();
						serverSocket = null;
						list.clear();
						System.out.println("wifi closeServer serverSocket=null");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					serverSocket = new ServerSocket(port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (!isInterrupted()) {
					try {
						if (serverSocket == null) {
							break;
						}

						Socket socket = serverSocket.accept();
						if (socket != null) {
							UartSocketThread th = new UartSocketThread(socket);
							System.out.println("accept a client:"
									+ socket.getRemoteSocketAddress());
							list.add(th);
							th.start();

						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * @author client访问子线程
		 *
		 */
		class UartSocketThread extends Thread {
			Socket socket = null;
			byte[] byts = new byte[7];

			public UartSocketThread(Socket isocket) {
				socket = isocket;
			}

			public boolean send_ledStatus(String staus) {
				// TODO Auto-generated method stub
				if (socket == null) {
					System.err.println("socket == null");
					return false;
				}
				if (!socket.isClosed()) {
					try {

						System.err.println("send_ledStatus write~~~~~~~~~~~~~~~~~:"
								+ staus);

						socket.getOutputStream().write(staus.getBytes());
						socket.getOutputStream().flush();

						System.err.println("send_ledStatu~~~~~~~~~~~~~~~~~:"
								+ staus);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				} else {
					System.err.println("socket isClosed");
					return false;
				}
				return true;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while (!isInterrupted()) {
					try {
						if (socket == null) {
							System.err.println("socket == null");
							break;
						}
						while (!socket.isClosed()) {
							int len = socket.getInputStream().read(byts);
							if (len < 0)
								break;
							String str = new String(byts, 0, len);

							System.out.println("~~~~~~~~~~~recv:" + str);
							
						}

						System.err
								.println("client close sokcet################################");
						break;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						break;
					}
				}

				synchronized (list) {
					int size = list.size();
					for (int i = 0; i < size; i++) {
						if (list.get(i).equals(this)) {
							list.remove(i);
							System.err
									.println("list.remove thead ################################");
							i--;
							size--;
						}
					}

				}

			}

		}
}
