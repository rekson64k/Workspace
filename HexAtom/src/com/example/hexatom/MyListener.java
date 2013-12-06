/* Author: 	Joshua Rice (borrowed from Dr. Dale Parson's Java implementation)
 * Date:	Fall 2013
 * File:	MyListener.java
 * Purpose: This is a helper class which receives messages from the HexAtom server process and
 * 			displays them to the user. 
 */
package com.example.hexatom;
import android.content.Context;
import android.widget.Toast;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;


	//This class was created by Dr. Dale Parson as a helper class for receiving messages from 
	//the HexAtom server process. This class is part of Dr. Parson's Java implementation of OSC.
	public class MyListener implements OSCListener {
		private static CommandActivity parent;
		
		//This constructor gets the context from the previous activity, so that the previous
		//activitie's context may be used within this activity. 
		private static Context context;
		MyListener(Context c){
			context = c;
			parent = (CommandActivity) context;
		}
		
		//Receive an incoming OSC message from the HexAtom server and print in to the user in 
		//the main user interface thread. This is accomplished using the Android runOnUiThread 
		//method.
		public void acceptMessage(java.util.Date time, OSCMessage message) {
			
			String temp = "";
			if (time != null) {
				temp = "Recv OSC at time " + time.toString() + ": ";
			} else {
				temp = "Recv OSC : ";                
			}
			temp.concat(message.getAddress() + " ");
			Object [] args = message.getArguments();
			for (int i = 0 ; i < args.length ; i++) {
				temp = temp.concat(" ("+args[i].getClass().toString()+") "
						+ args[i].toString());
			}
			temp = temp.concat("");
			
			
			//This class is used simply to transfer the time and message data from the received, 
			//incoming OSC packet to the code which actually displays the message in the app to 
			//the user. Since this listener runs on a separate thread and attempts to show a message 
			//to the user, it must run in the main UI thread. To do this you must use the method
			//"runOnUiThread method which accepts a Runnable object as a parameter. This runnable object 
			//is defined below as a class, in order to pass the OSC message data into the Runnable "run"
			//method, which actually runs the code that displays data to the user on the main UI thread.
			//Without this class below, there is no way to get the OSC message data from the acceptMessage()
			//method to runOnUiThread() method.
			class OSCRunnable implements Runnable{
				final String msg;
      		
				OSCRunnable(String temp){
					msg = temp;
				}
				
				//This is the code which actually runs on the main User Interface thread. This simply
				//bundles up the data from the message and displays it to the user via an Android toast.
				public void run(){
					Toast t = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
					t.show();
				}      		
			}
			//The code below allows each OSC message to be displayed on the user interface. 
			//The runOnUiThread() method runs the code in the run() method, within the runnable 
			//object passed to it, on the main thread controlling the user interface. Without 
			//this code, the app will crash since any code that attempts to access the UI
			//must access the UI through the main thread of operation.
			parent.runOnUiThread(new OSCRunnable(temp));  

  }
}