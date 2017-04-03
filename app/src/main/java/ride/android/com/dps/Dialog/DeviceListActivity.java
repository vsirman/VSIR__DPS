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

package ride.android.com.dps.Dialog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import es.dmoral.toasty.Toasty;
import ride.android.com.dps.R;



public class DeviceListActivity extends AppCompatActivity
{

	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;


	public  static String  EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		setContentView(R.layout.device_list);


		setResult(RESULT_CANCELED);


		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});


		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);


		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);


		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);


		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);


		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);


		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();


		SharedPreferences sharedata1 = getSharedPreferences("Add", 0);
		String stringName = sharedata1.getString(String.valueOf(0), null);
		String stringAdd = sharedata1.getString(String.valueOf(1), null);


        if (pairedDevices.size() > 0) {
        	pairedListView.setVisibility(View.VISIBLE);

            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            	if(stringName!=null && stringAdd!=null)
        		{
            		if(device.getName().equals(stringName) && device.getAddress().equals(stringAdd))  //�����,�Զ�����
    				{
    					Toasty.warning(this, "正在连接...", Toast.LENGTH_SHORT).show();
    					ReturnData(device.getAddress());
    				}	
        		}
                
            }
        } else {
            String noDevices = "No devices have been paired";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();


		if (mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}


		this.unregisterReceiver(mReceiver);
	}


	private void doDiscovery()
	{
		if (D) Log.d(TAG, "doDiscovery()");


		setTitle(R.string.scanning);


		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);


		if (mBtAdapter.isDiscovering())
		{
			mBtAdapter.cancelDiscovery();
		}


		mBtAdapter.startDiscovery();
	}


	private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
		{
			mBtAdapter.cancelDiscovery();

			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			ReturnData(address);
		}
	};
    private void ReturnData(String address) {

		Intent intent = new Intent();
		intent.putExtra(EXTRA_DEVICE_ADDRESS, address);


		setResult(Activity.RESULT_OK, intent);
		finish();
	}
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();


			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


				if (device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					mNewDevicesArrayAdapter.add(device.getName() + "\n"+"\t"
							+ device.getAddress());
				}

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0)
				{
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

}
