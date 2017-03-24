package com.wm.Client;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import toolkit.Converts;
import toolkit.WmToastUtil;
import toolkit.WmLogUtil;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import android.widget.Toast;

import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.AndroidSDK.UartConfigMgr;
import com.wm.uart2net.R;

public class ClientActivity extends Activity {	
	private String TAG = "ClientActivity";
	
	private ArrayAdapter<String> mArrayAdapterCmd=null;
	
	private EditText mEditText;
	private EditText mEditTextTimming;
	private EditText edtIP;
	private EditText edtPort;
	
	private ListView listView_at_show;

	private CheckBox mCheckBoxRecvHex;
	private CheckBox mCheckBoxSendHex;
	private CheckBox mCheckBoxSendToNet;
	private CheckBox mCheckBoxSendToLocal;
	private CheckBox mCheckBoxLineFeed;
	private CheckBox mCheckBoxTimmingSend;

	private boolean RecvHex = false;
	private boolean SendHex = false;
	private boolean SendToNet = false;
	private boolean SendToLocal = false;
	private boolean LineFeed = false;
	private boolean TimmingSend = false;
	 public boolean isConnected = false;

	private final String OPEN_UART = "打开串口";
	private final String CLOSE_UART = "关闭串口";

	private Button mButtonClear;
	private Button mButtonConfigOK;
	private Button mButtonSend;
	private Button mButtonConnect;

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
	
	private List<String> dataAtCmdUser = new ArrayList<String>();//动态字符串数组	
	
	private UartConfigMgr mUartConfigMgr = new UartConfigMgr();
	private int mUartFd = -1;
	private ReadThread mReadThread = null;	
	Context mContext;	
	byte[] bufferToShow = new byte[8 * 1024];
	
	InputStream in;
    PrintWriter printWriter = null;
    Socket mSocket = null;
    private MyHandler myHandler;
    Thread receiverThread;
    
    /***********************异步处理机制，子线程处理，通过handler将处理结果（接收到的数据）更新在主线程UI中*************************/
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //收到msg==2的message，执行receiverData；
            receiverData(msg.what);
            if (msg.what == 1) {
                String result = msg.getData().get("msg").toString();
                //将接收到的数据数据转换成字符串显示在接收区，追加在历史数据后面显示
                dataAtCmdUser.add(result);
                listView_at_show.setAdapter(mArrayAdapterCmd);
            }
        }
    }
	/***************************主程序入口*************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		initConfigUartViews();		
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
		mCheckBoxSendToNet = (CheckBox) findViewById(R.id.checkBoxSendToNet);
		mCheckBoxSendToLocal = (CheckBox) findViewById(R.id.checkBoxSendToLocal);
		mCheckBoxLineFeed = (CheckBox) findViewById(R.id.checkBoxLineFeed);
		mCheckBoxTimmingSend = (CheckBox) findViewById(R.id.checkBoxTimmingSend);
		
		mCheckBoxRecvHex.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBoxSendHex.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBoxSendToNet.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBoxSendToLocal.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBoxLineFeed.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBoxTimmingSend.setOnCheckedChangeListener(mOnCheckedChangeListener);
		
		
		mButtonConnect = (Button) findViewById(R.id.btnConnect);
		mButtonConfigOK = (Button) this.findViewById(R.id.buttonUartConfgOK);
		mButtonClear =    (Button) this.findViewById(R.id.buttonClear);		
		mButtonSend = 	  (Button)this.findViewById(R.id.buttonSend);	
		
		mButtonConnect.setOnClickListener(mbuttonOnClickListener);	
		mButtonConfigOK.setOnClickListener(mbuttonOnClickListener);	
		mButtonClear.setOnClickListener(mbuttonOnClickListener);	
		mButtonSend.setOnClickListener(mbuttonOnClickListener);	
		
		edtIP = (EditText) this.findViewById(R.id.ipconfig);
        edtPort = (EditText) this.findViewById(R.id.port_config);
		mEditTextTimming = (EditText)findViewById(R.id.editText_timming);
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
		myHandler = new MyHandler();
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
			case R.id.btnConnect:
				connect();
				break;
			
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
			case R.id.checkBoxSendToNet:
				if (arg1) {
					SendToNet = true;				

				} else {
					SendToNet = false;				
				}
				break;
				
			case R.id.checkBoxSendToLocal:
				if (arg1) {
					SendToLocal = true;				

				} else {
					SendToLocal = false;				
				}
				break;
				
			case R.id.checkBoxLineFeed:
				if (arg1) {
					LineFeed = true;				

				} else {
					LineFeed = false;				
				}
				break;
				
			case R.id.checkBoxTimmingSend:
				if (arg1) {
					TimmingSend = true;				

				} else {
					TimmingSend = false;				
				}
				break;
			
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
					///////////////////////////
					try {
						String hex = new String(retbts, "GB2312");
			            if (printWriter == null || hex == null) {
			                if (printWriter == null) {
			                    showInfo("连接失败!");
			                    return;
			                }
			                if (hex == null) {
			                    showInfo("发送失败!");
			                    return;
			                }
			            }
			            //发送数据
			            printWriter.print(hex);
			            //强制将缓冲区的数据发送出去
			            printWriter.flush();
			       
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					 
					//////////////////////////
					
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
				//Log.e("MainActivity",hex);
				byte sendbytes[] = null;				
				if(SendToLocal){
					if (SendHex) {					
						sendbytes = Converts.stringToBytes(snd);
						hex = Converts.bytesToString(sendbytes);
						
					} else {					
							sendbytes = snd.getBytes();
						
					}
					if (hex.length() == 0) {
						WmToastUtil.show_prompt_msg(ClientActivity.this, "格式输入错误");
						return;
				}
				
				int ret = HardwareControler.write(mUartFd, sendbytes);	
				WmLogUtil.e(TAG,snd);
				}
				if(SendToNet){
					 try {
				            
				            if (printWriter == null || snd == null) {
				                if (printWriter == null) {
				                    showInfo("连接失败!");
				                    return;
				                }
				                if (snd == null) {
				                    showInfo("发送失败!");
				                    return;
				                }
				            }
				            //发送数据
				            printWriter.print(snd);
				            //强制将缓冲区的数据发送出去
				            printWriter.flush();
				        } catch (Exception e) {
				        }
				}
			} else {
				Toast.makeText(ClientActivity.this, "串口没有打开", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(ClientActivity.this, "请输入数据", Toast.LENGTH_SHORT).show();
		}
	}
	

	////////////////////////////////////
	 /*******网络连接********实现connect()方法，定义子线程并启动子线程建立连接****/
    private void connect() {
        if (!isConnected) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    //获取IP地址和端口号，跳转到connectServer()方法
                   // connectServer(edtIP.getText().toString(), edtPort.getText()
                   //         .toString());
                    connectServer("192.168.1.109", "8001"); //便于调试固定了IP地址和端口号
                   
                }
            }).start();
        } else {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mButtonConnect.setText("连接");
            isConnected = false;
        }
    }
    

    /****************连接服务器的方法*************************/
    private void connectServer(String ip, String port) {
        //使用try-catch捕获建立连接中的异常
        try {
            //创建socket连接
            mSocket = new Socket(ip, Integer.parseInt(port));
            System.out.println(ip+port);
            //获取输出流
            OutputStream outputStream = mSocket.getOutputStream();
            printWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(outputStream,
                            Charset.forName("gb2312"))));
            //获取输入流
            in = mSocket.getInputStream();
            //发送消息
            myHandler.sendEmptyMessage(2);
            showInfo("连接成功!");
        } catch (Exception e) {
            isConnected = false;
            showInfo("连接失败!");
        }
    }
    
    /*****数据接收并显示********定义接收子线程,实现数据读取,通过handler返回数据用于显示*******************/
    private void receiverData(int flag) {
        if (flag == 2) {
            //启动子线程
        	new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (isConnected) {
                            if (mSocket != null && mSocket.isConnected()) {
                                //读取输入流数据
                                String result = readFromInputStream(in);
                                System.out.println(result);
                                try {
                                    if (!result.equals("")) {
                                        Message msg = new Message();
                                        msg.what = 1;
                                        Bundle data = new Bundle();
                                        data.putString("msg", result);
                                        msg.setData(data);
                                        myHandler.sendMessage(msg);
                                        
                                        ////////////网络数据转发到本地串口/////////////
                                        if (result.length() > 0) {
                                			if (mUartFd > 0) {
                                				byte sendbytes[] = null;				
                                									
                                							sendbytes = result.getBytes();
                                				
                                				int ret = HardwareControler.write(mUartFd, sendbytes);	
                                				}
                                			} else {
                                				Toast.makeText(ClientActivity.this, "串口没有打开", Toast.LENGTH_SHORT).show();
                                			}
                                		} else {
                                			Toast.makeText(ClientActivity.this, "请输入数据", Toast.LENGTH_SHORT).show();
                                		}
                                        
                                        //////////////////////////////
                                   
                                } catch (Exception e) {
                                }
                            }
                        }
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        	System.out.println(111);
        	mButtonConnect.setText("断开");
        	System.out.println(222);
            isConnected = true;
        }
    }
    
    /************************* 读取输入流数据的方法******************/
    public String readFromInputStream(InputStream in) {
        int count = 0;
        byte[] inDatas = null;
        try {
            while (count == 0) {
                count = in.available();
            }
            inDatas = new byte[count];
            in.read(inDatas);
            return new String(inDatas, "gb2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /************************* 显示Toast消息的方法*********************/
    private void showInfo(String msg) {
    	
        Toast.makeText(ClientActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    
    

}
