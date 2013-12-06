/* Author: 	Joshua Rice
 * Editor:	Benjamin Higgins
 * Date:	Fall 2013
 * File:	MainActivity.java
 * Purpose:	This file is the initial GUI for the HexAtom Android App and is the first screen 
 * 			they will see when starting the app. This activity allows the user to input the 
 * 			IP address and port number where the HexAtom server process is listening.  
 */
package com.example.hexatom;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Method:		connectBtnListener
	//Returns:		nothing
	//Arguments:	View
	//Purpose:		This function handles the click event of the connect button on the main 
	//				activity. This function will attempt to connect to the HexAtom server using
	//				the IP address and port number provided by the user. If a successful connection
	//				is formed, another activity is started. If the connection is not successful, 
	//				this function alerts the user.
	public void connectBtnListener(View view){    	
    	
		//Get the EditText's containing the IP address and port number, and save those objects into
		//local variables. Then pull the IP address and port number out of those EditText objects.
	    EditText IPAddressEditText = (EditText)findViewById(R.id.editText_ipaddress);
	    EditText PortEditText = (EditText)findViewById(R.id.editText_port);
	    
	    //If nothing is entered in the EditText's, display a message to the user to enter IP address
	    //and port number. 
	    if(IPAddressEditText.getText().toString().trim().equals("") ||
	    		PortEditText.getText().toString().trim().equals("")){
	    	new AlertDialog.Builder(this).setTitle("IP/Port Error!").setMessage("Please enter an " +
	    		"IP address and Port number before connecting to HexAtom.").setNeutralButton("Close",
	    		null).show();
	    //Check if the IP address entered by the user is "localhost." If the IP address is "localhost," 
	    //alert the user that they must enter a valid IP address. (localhost is not valid as an IP address
	    //since the HexAtom server process cannot run on an Android device)
	    }else if(IPAddressEditText.getText().toString().trim().equals("localhost")){
	    	new AlertDialog.Builder(this).setTitle("IP Address Error!").setMessage("HexAtom cannot run on " +
	    			"your Android device. Please enter the IP address of the server on which HexAtom is running.")
	    			.setNeutralButton("Close", null).show();
	    }else {
	    	//If there is actually something entered in the EditText's for the IP address and the port
	    	//number, then get the strings from the EditText's and start the command activity.
	    	String ipaddress = IPAddressEditText.getText().toString();
	    	String port = PortEditText.getText().toString();
	    	
	    	//Check to see if the port number is valid
	    	//If it is not valid, make the user enter a valid port number. Otherwise start Command Activity and 
	    	//initialize a connection to HexAtom.
	    	if(Integer.parseInt(port) > 65535){
	    		new AlertDialog.Builder(this).setTitle("Port Number Error!").setMessage("Please enter a valid port " +
	    				"number (0 - 65535)").setNeutralButton("Close", null).show();
	    	}else{
	    		//Start the command activity, which is the screen from which the user can send commands to the server.
	    		Intent HexAtomGame = new Intent(this, CommandActivity.class);
	    		HexAtomGame.putExtra("ipaddress", ipaddress);
	    		HexAtomGame.putExtra("port", port);
	    		startActivity(HexAtomGame);
		    	//drop virtual keyboard
		    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    	imm.hideSoftInputFromWindow(IPAddressEditText.getWindowToken(), 0);
	    		imm.hideSoftInputFromWindow(PortEditText.getWindowToken(), 0);
	    	}
	    }
	}
	
	

}
