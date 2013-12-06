/* Author: 	Joshua Rice
 * Date:	Fall 2013
 * File:	SendCommand.java
 * Purpose: This is a helper class which sends commands to the HexAtom server process. This 
 * 			class utilizes OSC/UDP communication and uses Dr. Parson's implementation of 
 * 			OSC, called OSCParson. This class essentially takes a HexAtom command from the 
 * 			user, bundles it up into an OSC packet, and sends that packet to the HexAtom server
 * 			process. 
 */
package com.example.hexatom;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import android.os.AsyncTask;
import android.widget.EditText;
import android.app.AlertDialog;

//This is a helper class which connects to HexAtom on the server. This class was created
//so that connection to HexAtom could be done on a separate thread. This is forced upon
//any Internet connection for Android versions 4.3 and newer.
//This class ONLY sends commands to HexAtom on the server, it does NOT send the initial 
//packet.
public class SendCommand extends AsyncTask<String, Void, String>{

	//Initialize a variable to hold the Command Activity instance and 
	//a variable to hold a command string (if the calling activity is sending a string)
	private CommandActivity CmdActParent;
	private String command;		
	
	public SendCommand(CommandActivity parent, String cmdstring){
		CmdActParent = parent;
		command = cmdstring;
	}
	
	@Override
	protected String doInBackground(String...info) {
		try{
			 //Get the port numbers and IP addresses from the calling method.
			 String InIP = info[0];
			 String InPort = info[1];
			 String OutIP = info[2];
			 String OutPort = info[3];
			 String seqNum = info[4];
				 
			 //Set up the Object array that will be used
			 //to bundle up the information into a packet to be sent to HexAtom.
		     Object [] oscargs = new Object [4];
			
		     //If the calling Command Activity did not send a command (called via the gamespaceClick 
		     //function), then get the command the user entered in the EditText.
		     if(command == ""){
		    	 //Get the command entered by the user and send it to the sendCommand function.
		    	 EditText editText_command = (EditText)CmdActParent.findViewById(R.id.editText_command);
			 	 command = editText_command.getText().toString();
		     }
		     
			 //Bundle up the incoming IP address, incoming port number, sequence number, and command
			 oscargs[0] = InIP;
			 oscargs[1] = Integer.parseInt(InPort);
			 oscargs[2] = Integer.parseInt(seqNum);
			 oscargs[3] = command;
					 
			 //Initialize the OSC object that will actually send the OSC packet to HexAtom
			 OSCPortOut sender = null;
					 
			 //Set the IP address and port of the server to which packets will be sent
			 if (OutIP != "" && OutPort != "") {
				 InetAddress otherIP = InetAddress.getByName(OutIP);
				 sender = new OSCPortOut(otherIP, Integer.parseInt(OutPort));
			 }
						 
			 //Send the bundled information to HexAtom
			 sender.send(new OSCMessage("/interpret", oscargs));
					 
			 }catch (final UnknownHostException ux) {
				 //The code below allows the error message to be displayed on the user interface. 
				 //The runOnUiThread() method runs the code in the run() method, within the runnable 
				 //object passed to it, on the main thread controlling the user interface. Without 
				 //this code, the app will crash since any code that attempts to access the UI
				 //must access the UI through the main thread of operation. 
				 CmdActParent.runOnUiThread(new Runnable() {
					  public void run() {
						  new AlertDialog.Builder(CmdActParent).setTitle("Connection Error!").
						     setMessage("Unknown Host Exception: " + ux.getMessage()).
						     setNeutralButton("Close", null).show();
					  }
					});
			 } catch (final java.net.SocketException sx) {
				 CmdActParent.runOnUiThread(new Runnable() {
					  public void run() {
						  new AlertDialog.Builder(CmdActParent).setTitle("Connection Error!").
						     setMessage("Socket Exception: " + sx.getMessage()).
						     setNeutralButton("Close", null).show();
					  }
				 });
			 } catch (final java.io.IOException iox) {
				 CmdActParent.runOnUiThread(new Runnable() {
					  public void run() {
						  new AlertDialog.Builder(CmdActParent).setTitle("Connection Error!").
						     setMessage("I/O Exception: " + iox.getMessage()).
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
		return "done";
	}
	
	protected void onPostExecute(String result){
		
	}
			
}