package com.example.andylab.blueterm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView txv;
    EditText edt;
    //=========BLUETOOTH===================
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;//控制手機藍芽裝置功能，取得手機系統上的藍芽配接器
    private BluetoothSocket btSocket = null;//網路傳輸介面
    private OutputStream outStream = null;//資料傳輸
    // Well known SPP UUID
    private static final UUID MY_UUID =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //UUID:通用唯一識別碼，表示特定服務類別。
    // Insert your server's MAC address
    private static String address = "20:15:01:30:04:27";//20:13:12:09:51:37
    //Set<BluetoothDevice> pairedDevices ;
    //ArrayAdapter<String> listAdapter = null;
    //BluetoothDevice device;
    ArrayAdapter<String> bondedDevices;
    Button btn,connect_btn,send_btn,scan_btn;
    Spinner s1;
    //int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bondedDevices = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        txv = (TextView) findViewById(R.id.textview);
        edt = (EditText) findViewById(R.id.editText);
        btn = (Button)findViewById(R.id.button);
        connect_btn = (Button)findViewById(R.id.button2);
        send_btn = (Button)findViewById(R.id.button3);
        //scan_btn = (Button)findViewById(R.id.button4);
        s1 = (Spinner) findViewById(R.id.spinner);

        //listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        //控制手機藍芽裝置功能，取得手機系統上的藍芽配接器
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //btAdapter.startDiscovery();
        if (btAdapter == null) {
            //errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            //如果藍芽沒有打開則啟動藍芽
            Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //showPairedDevices();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scanForDevices();
                showPairedDevices();
            }
        });
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //flag = 1;
                connection();
            }
        });
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //flag = 1;
                //connection();
                sendData(edt.getText().toString().getBytes());
                txv.setText(edt.getText().toString());
            }
        });
        /*scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });*/


        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //txv.setText(s1.getSelectedItem().toString().substring(s1.getSelectedItem().toString().length()-17));
                address = s1.getSelectedItem().toString().substring(s1.getSelectedItem().toString().length()-17);
                txv.setText(s1.getSelectedItem().toString()+"\n"+address);
                //flag = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void showPairedDevices(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                bondedDevices.add(device.getName() + "\n" + device.getAddress());
                Log.d("debug", device.getName() + "\n" + device.getAddress());
            }
        }

        bondedDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(bondedDevices);
        s1.showContextMenu();

    }
    public void connection(){

        if (btAdapter.isEnabled()) {

            //flag = 0;
            // Set up a pointer to the remote node using it's address.
            BluetoothDevice device = btAdapter.getRemoteDevice(address);//��o���ݸ˸m��������T

            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }


            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            //Log.d(TAG, "...Connecting to Remote...");

            try {
                btSocket.connect();
                //Log.d(TAG, "...Connection established and data link opened...");
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
            // Create a data stream so we can talk to server.
            //Log.d(TAG, "...Creating Socket...");
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                //errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }
    }

    public void scanForDevices(){
        btAdapter.startDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                bondedDevices.add(device.getName() + "\n" + device.getAddress());
                bondedDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s1.setAdapter(bondedDevices);
                s1.showContextMenu();
                //refreshNewDevices();
            }
        }
    };

    /*@Override
    public void onResume() {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);


        //GetPairedDevices();

        //BT
        //Log.d(TAG, "...In onResume - Attempting client connect...");
        // Set up a pointer to the remote node using it's address.


        if (btAdapter.isEnabled()&&flag==1) {
            //flag = 0;
            // Set up a pointer to the remote node using it's address.
            BluetoothDevice device = btAdapter.getRemoteDevice(address);//��o���ݸ˸m��������T

            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.

            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }


            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            //Log.d(TAG, "...Connecting to Remote...");

            try {
                btSocket.connect();
                //Log.d(TAG, "...Connection established and data link opened...");
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
            // Create a data stream so we can talk to server.
            //Log.d(TAG, "...Creating Socket...");
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                //errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }

    }*/

    @Override
    public void onPause() {
        super.onPause();

        //BT

        if (btAdapter.isEnabled()) {

            if (outStream != null) {
                try {
                    outStream.flush();
                } catch (IOException e) {
                    //errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
                }


                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendData(byte[] message) {
        //byte[] msgBuffer = message.getBytes();

        //Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(message);
        } catch (IOException e) {

        }
    }

    /*public void GetPairedDevices(){

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        List<String> conditionArrayAdapter = null;
        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                conditionArrayAdapter.add(device.getName());
            }
        }
        else{
            String noDevices = getResources().getText(R.string.no_paired_devices).toString();
            conditionArrayAdapter.add(noDevices);
        }
        //txv.setText((CharSequence) conditionArrayAdapter);

    }*/

    /*private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice[] pairedDevices = new BluetoothDevice[0];
            for(BluetoothDevice btdevice:pairedDevices){
                if(btdevice.getName().equals(((TextView) v).getText().toString())){
                    BTDevice = btdevice;
                }
            }
            CreatConnetction(BTDevice);
        }
    };*/
}
