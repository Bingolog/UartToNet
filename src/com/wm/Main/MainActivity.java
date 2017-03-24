package com.wm.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.wm.Client.ClientActivity;
import com.wm.Server.ServerActivity;
import com.wm.uart2net.R;

public class MainActivity extends Activity {
	private Button ButtonClient;
    private Button ButtonServer;
    
	@Override	
	public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.client_server_select);
        
        ButtonClient = (Button)findViewById(R.id.btn_client);
        ButtonServer = (Button)findViewById(R.id.btn_server);
        
        ButtonClient.setOnClickListener(new OnClickListener(){
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ClientActivity.class);
    			startActivity(intent);    
			}	
		});	
        ButtonServer.setOnClickListener(new OnClickListener(){
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ServerActivity.class);
    			startActivity(intent); 
			}	
		});	
	}

}
