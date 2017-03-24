package com.friendlyarm.AndroidSDK;

public class UartConfigMgr {

	 String devName;
	 long baud;
	 int dataBits;
	 int stopBits ;
	 
	 public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public long getBaud() {
		return baud;
	}

	public void setBaud(long baud) {
		this.baud = baud;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	
	 //static private UartConfigMgr mUartConfigMgr = new UartConfigMgr();
	 public UartConfigMgr()
	 {
		 devName = "/dev/ttySAC3";
		 baud = 115200;
		 dataBits = 8;
		 stopBits = 1;
	 };
	 
	 public  void SaveConfig(String idevName, long ibaud, int idataBits, int istopBits
			) {
		
		devName = idevName;
		baud = ibaud;
		dataBits = idataBits;
		stopBits = istopBits;

	}

//	 public  UartConfigMgr GetConfig()
//	{
//		
//		return mUartConfigMgr;
//	}
}
