/* Author: 	Joshua Rice
 * Editor:	Benjamin Higgins
 * Date:	Fall 2013
 * File:	CommandActivity.java
 * Purpose:	This file is the second GUI screen with which the user will interact. 
 * 			Here the user actually interacts with the HexAtom server process, sending
 * 			commands and displaying atoms on the planetarium dome. The user is also able
 * 			to disconnect from the current HexAtom server, connect to another HexAtom server,
 * 			and get help on how to use the app. This activity utilizes several classes created
 * 			exclusively for this project including InitializeConnection.java, SendCommand.java, 
 * 			and MyListener.java.  
 */
package com.example.hexatom;

import java.util.Random;

import com.illposed.osc.OSCPortIn;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class CommandActivity extends Activity {
	protected @interface override {

	}
	//Initialize several global variables for use within later code
	//seqNum is the sequence number of the OSC packets
	//InIP is the Android devices IP Address
	//parent holds the current Command Activity instance
	//r is a random number generator (used in gamespaceClick) 
	private Integer seqNum = 1;
	private String InIP = "";
	private String InPort = "";
	private String OutIP = "";
	private String OutPort = ""; 
	private CommandActivity parent;
	private Random r;
	
	//OSCPortIn is made public to allow InitializeConnection to access it.
	public OSCPortIn receiver = null;
	DialogInterface.OnClickListener dialogClickListener;
	
	//This method defines the onClick event for the Fire button, which is what sends 
	//commands to HexAtom. This event handler will bundle up any commands a user enters
	//with the required information (outgoing IP address, incoming port number, sequence
	//number, and /interpret address pattern) and then send the commands to HexAtom.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_command);
		
		//Set variable "parent" to point to the current activity
		parent = CommandActivity.this;
		
		//Create a random number generator to use to generate random numbers for the 
		//onClick event for the gamespace
		r = new Random();
		
		//Get the outgoing IP address and port number from the previous activity
		//This are the IP address and port number of HexAtom on the server, describes
		//where we will send the OSC packets.
		Intent i = getIntent();
		OutIP = i.getStringExtra("ipaddress");
		OutPort = i.getStringExtra("port");
		
		//This is the IP address and port number of the app.
		//This is where HexAtom on the server will send its replies.
		InPort = "5000";
		try {
			//Get the IP address of the Android device running this app
			InIP = getLocalIpAddress();
			
			//Initialize the connection, set up the OSC receiver and event listener
			new InitializeConnection(parent).execute(InPort);
			
		} catch (Exception e){
			new AlertDialog.Builder(CommandActivity.this).setTitle("Connection Error!").
		  	setMessage("General Exception: " + e.getMessage()).
		   	setNeutralButton("Close", null).show();
		}
			
		//Create an event handler for the fire button. This event handler will assemble and 
		//send the bundled information (/interpret address pattern, outgoing IP address,
		//incoming port number, packet sequence number, HexAtom command) to HexAtom.
		Button FireButton = (Button) findViewById(R.id.button_fire);
		FireButton.setOnClickListener(new View.OnClickListener() {
				
			 @Override
			 public void onClick(View v) {
				 try { 
					 
				 //Send a command to the HexAtom server using an AsyncTask implementation
				 new SendCommand(parent, "").execute(InIP, InPort, OutIP, OutPort, seqNum.toString());
				 seqNum++;
				 } catch (Exception e) {
					 new AlertDialog.Builder(CommandActivity.this).setTitle("Error!").
				     setMessage("General Exception: " + e.getMessage()).
				     setNeutralButton("Close", null).show();
				 }	
			     //drop virtual keyboard
				 EditText editText_command = (EditText)findViewById(R.id.editText_command);
			     InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    	 imm.hideSoftInputFromWindow(editText_command.getWindowToken(), 0);
			 } 
		});       	
		
		//Set up a Dialog Interface which will handle yes/no responses from the user in regard to 
		//clicking on the action bar items. This will ask the user if they really want to connect to
		//another server or disconnect from the current server.
		dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            break;
		        }
		    }
		};
	}

	//This method will initialize the action bar at the top of the app
	//The action bar on this activity will contain the connect, disconnect, and settings buttons
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.command, menu);
		return true;
	}
	
	//This method handles click events on the action bar items.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.connect:
	        	AlertDialog.Builder connectBuilder = new AlertDialog.Builder(this);
	        	connectBuilder.setMessage("Are you sure you want to connect to another HexAtom server?")
	        		.setTitle("Connect")
	        		.setPositiveButton("Yes", dialogClickListener)
	        	    .setNegativeButton("No", dialogClickListener).show();
	        	return true;
	        case R.id.disconnect:
	        	AlertDialog.Builder disconnectBuilder = new AlertDialog.Builder(this);
	        	disconnectBuilder.setMessage("Are you sure you want to disconnect from HexAtom?")
	        		.setTitle("Disconnect")
	        		.setPositiveButton("Yes", dialogClickListener)
	        	    .setNegativeButton("No", dialogClickListener).show();
	        	return true;
	        case R.id.action_settings:
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	 //Method:		getLocalIpAddress
	 //Returns:		String
	 //Arguments:	None
	 //Purpose:		This function gets the IP address of the Android device running this app, using
	 //				WIFI services, and then returns the IP address it found. This function code used courtesy 
	 //				of Krishnaraj Varma (http://www.devlper.com/2010/07/getting-ip-address-of-the-device-in-android/) 
	 //				and Kevin McDonagh (http://www.androidsnippets.com/obtain-ip-address-of-current-device).
	 public String getLocalIpAddress()
	  {
	          try {
	        	  WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	        	  WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	        	  int ipAddress = wifiInfo.getIpAddress();
	        	  String ip = intToIp(ipAddress);
	        	  return ip;
	          } catch (Exception ex) {
	        	  new AlertDialog.Builder(CommandActivity.this).setTitle("Error!").
				     setMessage("Problem in getting Android IP address: " + ex.getMessage()).
				     setNeutralButton("Close", null).show();
	          }
	          return null;
	  }
	 
	 //Method:		intToIp
	 //Returns:		String
	 //Arguments:	int
	 //Purpose:		This function takes an integer in the form of an IP address and converts it
	 //				into a String. This code used courtesy of Krishnaraj Varma 
	 //				(http://www.devlper.com/2010/07/getting-ip-address-of-the-device-in-android/) 
	 //				and Kevin McDonagh (http://www.androidsnippets.com/obtain-ip-address-of-current-device).
	 public String intToIp(int i) {
		 
		 return (( i & 0xFF) + "." + 
				 ((i >> 8 ) & 0xFF) + "." +
				 ((i >> 16 ) & 0xFF) + "." +
				 ((i >> 24 ) & 0xFF ));
            
	  }
	 
	  //Method:		helpClick
	  //Returns:	none
	  //Arguments:	View
	  //Purpose:	This function handles the click event on the help button of the Command Activity 
	  //			user interface. This tells the user how to use the app and what commands are valid
	  // 			to enter.
      public void helpClick(View view) {
	   	 new AlertDialog.Builder(CommandActivity.this).setTitle("Help Menu").
	     setMessage("Enter a command in the textbox to the left or tap the circle to interact with" +
	       		"the game. Valid commands are as follows:\n\n" +
	    		"d<number>\n t<number>\n f<number>\n q\n <probability><atomic number>=<value>\n" +
	     		"a<atomic number><direction><delay> @<location>\n").
 	     setNeutralButton("Back to the Game", null).show();
 	  }	 
	  
      //Method:		gamespaceClick
 	  //Returns:	none
 	  //Arguments:	View
 	  //Purpose:	This function handles the click event on the gamespace of the Command Activity user interface. 
      //			Clicking on the gamespace will fire an atom onto the planetarium dome.
	  public void gamespaceClick (View view) {
		  //Get the direction from the tag attribute of each button image
		  String direction = (String) view.getTag();
		  
		  //Generate a random atom to fire to HexAtom
		  String tempCmd = "a";
		  tempCmd = tempCmd.concat(String.valueOf(r.nextInt(11)));
		  tempCmd = tempCmd.concat(direction);
		  tempCmd = tempCmd.concat(String.valueOf(r.nextInt(2)));
		  
		  try { 
			 //Send a command to the HexAtom server using an AsyncTask implementation
			 new SendCommand(parent, tempCmd).execute(InIP, InPort, OutIP, OutPort, seqNum.toString());
			 seqNum++;
		  } catch (Exception e) {
		 	 new AlertDialog.Builder(CommandActivity.this).setTitle("Error!").
		     setMessage("General Exception: " + e.getMessage()).
		     setNeutralButton("Close", null).show();
		  }
	  }	

      //Method:		Fast Fotward Click
 	  //Returns:	none
 	  //Arguments:	View
	  public void FFClick (View view) {		  
		  //Generate a string to speed up time
		  String tempCmd = "a";
		  
		  try { 
			 //Send a command to the HexAtom server using an AsyncTask implementation
			 new SendCommand(parent, tempCmd).execute(InIP, InPort, OutIP, OutPort, seqNum.toString());
			 seqNum++;
		  } catch (Exception e) {
		 	 new AlertDialog.Builder(CommandActivity.this).setTitle("Error!").
		     setMessage("General Exception: " + e.getMessage()).
		     setNeutralButton("Close", null).show();
		  }
	  }    
	    
	  //Free up the OSCPortIn to allow the game to reconnect after closing.
	  @override
	  protected void onStop(){
		  super.onStop();
		  receiver.close();
	  }
}


