package com.motoduino.snapshot;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class snapshot extends Activity {

	private static final String TAG = "Snapshot";
    private static final boolean D = true;
    public static String bluetooth_address;
	private static final String DEVICE_ADDRESS = "00:13:03:13:85:55";  //Arduino Bluetooth Address
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	ImageButton button_left, button_right, button_forward, button_back;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private int mCurrentCar = 1;
    private RelativeLayout btnControlLayout;

    // Name of the connected device
    private String mConnectedDeviceName_1 = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the bluetooth services
    private BTService mBTService_1 = null;
    public static BTService mCurrentBTService = null;
    ImageButton btn_BTDiscovery;    
	final int DELAY = 150;    
	RelativeLayout RL_control;
	ImageButton button_shoot, button_exit;
	public ImageView 	CamImageView;

	public static boolean bFileSaved=false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(D) Log.e(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.main);
//        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);        

        // my button control
        button_shoot = (ImageButton)findViewById(R.id.Shoot); 
        
        button_shoot.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "TakeAPicture!");
            	TakeAPicture();
            }
        });        

        button_exit = (ImageButton)findViewById(R.id.Exit); 
        
        button_exit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            	finish();
            }
        });        

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            if(D) Log.e(TAG, "startActivityForResult: REQUEST_ENABLE_BT");
        	
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            mBTService_1 = new BTService(this, mHandler);
        }
    }
        
        
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
     
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
        if (mBTService_1 != null) mBTService_1.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");

	}

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        if (mBTService_1 != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService_1.getState() == BTService.STATE_NONE) {
              // Start the Bluetooth chat services
              mBTService_1.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    public void TakeAPicture()
    {
        sendMessage(mCurrentBTService, "a");
        
     }
 
    public void DisplayImage()
    {
    	Log.i(TAG, " DisplayImage");

        CamImageView = (ImageView)findViewById(R.id.ImageView); 
        
        String myJpgPath = "/sdcard/motoduino.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();	        
        options.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile(myJpgPath, options);
        Bitmap scaledBMP = zoomBitmap(bm);
        CamImageView.setImageBitmap(scaledBMP);
        
    }

       public Bitmap zoomBitmap(Bitmap target)
        {
    		ImageView pic=(ImageView)findViewById(R.id.ImageView); 
            int width = target.getWidth();
            int height = target.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float)pic.getWidth())/ width;
            float scaleHeight = scaleWidth; //((float)pic.getHeight())/ height;
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap result = Bitmap.createBitmap(target, 0, 0, width,   
            				height, matrix, true);   
            return result;
        }	    

    private boolean checkDeviceConnection()
    {
    	if((mCurrentBTService == null) || (mCurrentBTService.getState() != BTService.STATE_CONNECTED))
    	{
            Toast.makeText(this, R.string.title_not_connected, Toast.LENGTH_SHORT).show();
            return false;
    	}
    	return true;
    	
    }
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(BTService btService, String message) {
        // Check that we're actually connected before trying anything
//    	BTService BTRemoteService = btService;
//    	Log.i(TAG, "btService.getState() = "+mCurrentBTService.getState());
    	
//        if (mCurrentBTService.getState() != BTService.STATE_CONNECTED) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
//            return;
//        }
    	if (!checkDeviceConnection())
    		return;

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BTService to write
        	
            byte[] send = message.getBytes();
            
            btService.write(send);
            
        }
    }
	
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BTService.STATE_CONNECTED:
			                        	if(D) Log.d(TAG, " Set Car 1 active!");
			                        	btnControlLayout = (RelativeLayout)findViewById(R.id.duinocam);
			                        	btnControlLayout.setBackgroundColor(android.graphics.Color.GREEN);
			                        	
                     break;
                case BTService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                    Toast.makeText(getApplicationContext(), " Device is connecting! ", Toast.LENGTH_SHORT).show();
                    break;
                case BTService.STATE_LISTEN:
                		break;
                case BTService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
//                    Toast.makeText(getApplicationContext(), " Devices disconnected ! ", Toast.LENGTH_SHORT).show();

                	break;
                }
                break; 
            case MESSAGE_WRITE:
                break;
            case MESSAGE_READ:
            	DisplayImage();
                break;
            case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName_1 = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + mConnectedDeviceName_1, Toast.LENGTH_SHORT).show();
          	  break;
            case MESSAGE_TOAST:
                if(D) Log.d(TAG, "MESSAGE_TOAST !");
            	if(msg.getData().getString(TOAST) == null)
            		break;
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if(D) Log.d(TAG, "onRestoreInstanceState!");
    	
    }

    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        if(D) Log.d(TAG, "onSaveInstanceState!");
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.connect:
            Log.i(TAG, "Connect Device"+DEVICE_ADDRESS);
            // Get local Bluetooth adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null) {
                Toast.makeText(snapshot.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                return true;
            }
            
            // If BT is not on, request that it be enabled.
            if (!mBluetoothAdapter.isEnabled()) {
                if(D) Log.e(TAG, "startActivityForResult: REQUEST_ENABLE_BT");
            	
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                return true;
            }
            mBTService_1 = new BTService(snapshot.this, mHandler);
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            // Attempt to connect to the device
            mCurrentBTService =  mBTService_1;   
        	mCurrentBTService.connect(device);
            return true;
        case R.id.About:
            // About author
            Toast.makeText(this, R.string.motoduino_about, Toast.LENGTH_LONG).show();
            return true;
        case R.id.Exit:
            // Sensor Control
        	finish();
        	return true;
        }
        return false;
    }
    

}
