/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.witsi.bluetooth.three;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.witsi.bluetooth.WtBtDeviceListener;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class Blue3toothChatService implements WtBtDeviceListener {
    // Debugging
    private static final String TAG = "Blue3toothChatService";
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
    	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
    	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final BloothConnectListener connectLister;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BT_STATE mState;
    private BluetoothDevice mDevice;
    
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public Blue3toothChatService(Context context, BloothConnectListener listener) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BT_STATE.STATE_NONE;
        connectLister = listener;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(BT_STATE state) {
    	
    	if(mState == state)
    		return;
    	
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        connectLister.onStateChange(state);
       
    }

    /**
     * Return the current connection state. */
    public synchronized boolean getConnectState(){
    	return  mState== BT_STATE.STATE_CONNECTED;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(BT_STATE.STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized int connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == BT_STATE.STATE_CONNECTING) {
            if (mConnectThread != null) {
            	mConnectThread.cancel();
            	mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }
        
        setState(BT_STATE.STATE_NONE);
        // Start the thread to connect with the given device
        mDevice = device;
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(BT_STATE.STATE_CONNECTING);
        return 1;
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, final String socketType) {
    	Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mConnectedThread = new ConnectedThread(socket, socketType);
//        mConnectedThread.start();
        setState(BT_STATE.STATE_CONNECTED);
  
    }

    /**
     * Stop all threads
     */
    public void close() {
    	Log.d(TAG, "close");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        
        setState(BT_STATE.STATE_DISCONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BT_STATE.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        if(r==null)
        	return;
        r.write(out);
    }

    public byte[] read() {
   	 // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread      
        synchronized (this) {
            if (mState != BT_STATE.STATE_CONNECTED)return null;
            r = mConnectedThread;
        }
        if(r==null)
        	return null;
        // Perform the write unsynchronized     
        return r.read();
    }
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    int count = 0;
    private void connectionFailed() {
        // Send a failure message back to the Activity
    	Log.e(TAG, "Unable to connect device count = " + count);
//        Blue3toothChatService.this.start();
        
        close();
        
//        if(count < 1)
//        {
//    		try {
//    			Thread.sleep(1000);
//    		} catch (InterruptedException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}        	
//    		connect( mDevice, true);
//        }else if(count == 1)
//        {
//        	try {
//    			Thread.sleep(2000);
//    		} catch (InterruptedException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}       
//        	connect( mDevice, true);
//        }else
//        {
//        	count = 0;
//        	return;
//        }
//    	count++;

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
    	Log.e(TAG, "Device connection was lost");
    	close();

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        @SuppressLint("NewApi")
		public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,  //��Ҫpasskey
                        MY_UUID_SECURE);
                }else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(	    //����Ҫpasskey
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            }catch (IOException e) {
            	Log.d(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
        	
        	Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != BT_STATE.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                	Log.d(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (Blue3toothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket,
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } 
                            catch (IOException e) {
                            	Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
        	Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            	Log.d(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private String mSocketType;

        @SuppressLint("NewApi")
		public ConnectThread(BluetoothDevice device, boolean secure) {
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
            	Log.d(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }

            mmSocket = tmp;
        }

        public void run() {
        	Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            Log.i(TAG, "Start cancelDiscovery()");
            if (mAdapter != null && mAdapter.isDiscovering()) 
            {
            	 Log.i(TAG, "On cancelDiscovery()");
            	 mAdapter.cancelDiscovery();
            }
        	Log.i(TAG, "End cancelDiscovery()");

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                
            } catch (IOException e) {
            	
            	Log.e(TAG, "ConnectThread:", e);
            	
                // Close the socket
                try {
                	
                    mmSocket.close();
                    
                } catch (IOException e2) {
                	Log.d(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Blue3toothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected( mmSocket, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            	Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        ByteArrayOutputStream RecvBuff = new ByteArrayOutputStream(); 
        private byte[] buff = new byte [1024];
        private boolean flag = true;
        
        public ConnectedThread(BluetoothSocket socket, String socketType) {
        	Log.d(TAG, "create ConnectedThread: " + socketType);
        	
        	
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            	Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public byte[] read() {

	      	synchronized (this) 
	   		{
	            int bytes = -1;
	            // Keep listening to the InputStream while connected
	            try {

	            	int available = mmInStream.available();
	            	 
	                if(available > 0 && mConnectedThread!=null)
	                {
	                	bytes = mmInStream.read(buff);
	                }
	                
	            } catch (IOException e) {
	            	Log.e(TAG, "disconnected", e);
	                connectionLost();
	            }
	            
	            RecvBuff.reset();
	            if(bytes > 0)
	            {
	                RecvBuff.write(buff, 0, bytes);
	                return RecvBuff.toByteArray();
	            }
	            return null;
	   		}
        }
        
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            	
            	if(mmOutStream !=null)
            	{
            	    try {
            	        mmOutStream.flush();
            	    } catch (IOException e1) {
            	        // log here ...
            	        e1.printStackTrace();
            	    }
            	    try {
            	    	mmOutStream.write(buffer);
                        // Share the sent message back to the UI Activity
//                        mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            	    } catch (IOException e) {
            	    	Log.e(TAG, "Exception during write", e);
                        connectionLost();
            	    }
            	}
                
        }

        public void cancel() {
            try {
            	flag = false;
                mmSocket.close();
            } catch (IOException e) {
            	Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        

    }

	@Override
	public void disconnect() {
		close();
	}
}