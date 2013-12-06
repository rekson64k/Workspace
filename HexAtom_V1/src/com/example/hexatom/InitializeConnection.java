/* Author: 	Joshua Rice
 * Date:	Fall 2013
 * File:	InitializeConnection.java
 * Purpose: This is a helper class which creates an initial connection to the HexAtom server
 * 			process. This class utilizes OSC running on UDP to connect to the server, and 
 * 			uses Dr. Parson's implementation of OSC called OSCParson. 
 * 			This class also initializes an OSC listener (implemented in MyListener.java) 
 * 			which listens for an incoming UDP/OSC packet.
 *  
 * 			***Since this class uses UDP and creates a stateless connection, the app will not 
 * 			receive packets from the server unless the app and the server are on a Local Area 
 * 			Network. Additionally, Android does not support Ad Hoc networks, so this app does 
 * 			not currently support Ad Hoc networks either. If the app and server are utilizing a
 * 			Wide Area Network, the app will successfully send UDP packets to the server, but it 
 * 			will NOT receive any packets from the server. Thus this app must utilize either a LAN 
 * 			or WAN, but will only receive replies from the server on a LAN. 
 * 			*** 
 */
package com.example.hexatom;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCPortIn;

import android.app.AlertDialog;
import android.os.AsyncTask;;

//This is a helper class which connects to HexAtom on the server. This class was created
//so that connection to HexAtom could be done on a separate thread. This is forced upon
//any Internet connection for Android versions 4.3 and newer.
//This is the InitializeConnection class, which only sends the first packet of connection 
//information to HexAtom. This will only run once at the start of the command activity. 
public class InitializeConnection extends AsyncTask<String, Void, String>{
	
	//Initialize a variable to hold the Command Activity instance
	private CommandActivity CmdActParent;
	
	public InitializeConnection(CommandActivity parent){
		CmdActParent = parent;
	}
			
	@Override
	protected String doInBackground(String...info) {
			//Get the port numbers and IP addresses from the calling method.
			final String InPort = info[0];
			
			 //The code below allows an event listener to be created on the main thread that will 
			 //listen for OSC events (incoming OSC packets) from the main UI thread. 
			 //The runOnUiThread() method runs the code in the run() method, within the runnable 
			 //object passed to it, on the main thread controlling the user interface. Without 
			 //this code, the app will crash since any code that attempts to access the UI
			 //must access the UI through the main thread of operation.
			 CmdActParent.runOnUiThread(new Runnable() {
				public void run(){
					try{
						//Initialize variables for use in establishing the OSC connection
						if(CmdActParent.receiver != null){
							CmdActParent.receiver.close();
						}
						CmdActParent.receiver = new OSCPortIn(Integer.parseInt(InPort));
						OSCListener listener = new MyListener(CmdActParent);
						 
						//Register the receiver to listen for incoming packets
						CmdActParent.receiver.addListener("/interpret", listener);
						CmdActParent.receiver.startListening();
					} catch (final java.net.SocketException sx) {
						 CmdActParent.runOnUiThread(new Runnable() {
							  public void run() {
								  new AlertDialog.Builder(CmdActParent).setTitle("Connection Error!").
								     setMessage("Socket Exception while Initializing Connection: " + sx.getMessage()).
								     setNeutralButton("Close", null).show();
							  }
							});
					 } catch (final Exception e) {
						 CmdActParent.runOnUiThread(new Runnable() {
							  public void run() {
								  new AlertDialog.Builder(CmdActParent).setTitle("Connection Error!").
								     setMessage("General Exception: " + e.getMessage()).
								     setNeutralButton("Close", null).show();
							  }
						   });
					 	}
					}
			 	});
			 
		return "done";
		}	
			
		protected void onPostExecute(String result){

		}
	}