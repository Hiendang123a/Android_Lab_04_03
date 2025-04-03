package com.example.bluetoothsocket;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BlueControl extends AppCompatActivity {
    //public static final int REQUEST_BLUETOOTH = 1;
    ImageButton btnTb1, btnTb2, btnDis;
    TextView txt1, txtMAC;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    Set<BluetoothDevice> pairedDevices1;
    String address = null;
    private ProgressDialog progress;
    int flaglamp1;
    int flaglamp2;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blue_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);
        btnTb1 = (ImageButton) findViewById(R.id.btnTb1);
        btnTb2 = (ImageButton) findViewById(R.id.btnTb2);
        txt1 = (TextView) findViewById(R.id.textV1);
        txtMAC = (TextView) findViewById(R.id.textViewMAC);
        btnDis = (ImageButton) findViewById(R.id.btnDisc);
        new ConnectBT().execute();//Call the class to connect
        btnTb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BlueControl", "bấm nút tb1");
                thietTbi1();
            }
        });

        btnTb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BlueControl", "bấm nút tb2");
                thietTbi7();
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
    }

    private void thietTbi1() {
        if (btSocket == null || !isBtConnected) {
            Log.d("BlueControl", "btSocket: " + btSocket + ", isBtConnected: " + isBtConnected);
            msg("Chưa kết nối Bluetooth.");
            return;
        }
        if (btSocket != null) {
            try {
                if (this.flaglamp1 == 0) {
                    this.flaglamp1 = 1;
                    this.btnTb1.setBackgroundResource(R.drawable.tb1on);
                    btSocket.getOutputStream().write("1".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang bật");
                    Log.d("BlueControl", "Thiết bị số 1 đang tắt");
                } else {
                    if (this.flaglamp1 != 1)
                        return;
                    {
                        this.flaglamp1 = 0;
                        this.btnTb1.setBackgroundResource(R.drawable.tb1off);
                        btSocket.getOutputStream().write("A".toString().getBytes());
                        txt1.setText("Thiết bị số 1 đang tắt");
                        Log.d("BlueControl", "Thiết bị số 1 đang tắt");
                    }
                }
            } catch (IOException e) {
                Log.e("BlueControl", "Lỗi khi gửi tín hiệu đến thiết bị số 1", e);
                msg("Lỗi");
            }
        }
    }

    private void thietTbi7() {
        if (btSocket != null) {
            try {
                if (this.flaglamp2 == 0) {
                    this.flaglamp2 = 1;
                    this.btnTb2.setBackgroundResource(R.drawable.tb7off);
                    btSocket.getOutputStream().write("7".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang bật");
                    Log.d("BlueControl", "Thiết bị số 7 đang bật");
                } else {
                    if (this.flaglamp2 != 1)
                        return;
                    {
                        this.flaglamp2 = 0;
                        this.btnTb2.setBackgroundResource(R.drawable.tb1off);
                        btSocket.getOutputStream().write("G".toString().getBytes());
                        txt1.setText("Thiết bị số 7 đang tắt");
                        Log.d("BlueControl", "Thiết bị số 7 đang tắt");
                    }
                }
            } catch (IOException e) {
                Log.e("BlueControl", "Lỗi khi gửi tín hiệu đến thiết bị số 7", e);
                msg("Lỗi");
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BlueControl.this, "Dang ket noi ... ", "Xin vui long doi!!!");
            Log.d("BlueControl", "Đang kết nối Bluetooth...");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    Log.d("BlueControl", "Bắt đầu kết nối đến " + address);

                    // Lấy đối tượng BluetoothAdapter (thiết bị Bluetooth của điện thoại)
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();

                    if (myBluetooth == null) {
                        Log.e("BlueControl", "Không tìm thấy Bluetooth trên thiết bị này");
                        return null;
                    }

                    if (ActivityCompat.checkSelfPermission(BlueControl.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(BlueControl.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(BlueControl.this, new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, 1);
                        return null; // Thoát ra nếu chưa có quyền
                    }

                    // Tạo kết nối RFCOMM đến thiết bị Bluetooth từ địa chỉ
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);

                    // Hủy bỏ các tiến trình quét Bluetooth đang chạy
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                    // Tiến hành kết nối
                    btSocket.connect();
                    Log.d("BlueControl", "Đang kết nối đến " + address);
                    isBtConnected = true; // Đặt kết nối thành công sau khi kết nối thành công
                    // Sau khi kết nối thành công, bạn có thể thêm log này để kiểm tra:
                    if (btSocket.isConnected()) {
                        Log.d("BlueControl", "Kết nối thành công!");
                    } else {
                        Log.d("BlueControl", "Kết nối không thành công.");
                    }
                }
            } catch (IOException e) {
                ConnectSuccess = false; // Nếu kết nối thất bại, gán ConnectSuccess là false
                Log.e("BlueControl", "Kết nối thất bại", e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Kết nối thất bại ! Kiểm tra thiết bị.");
                Log.d("BlueControl", "Kết nối thất bại");
                finish();
            } else {
                msg("Kết nối thành cong.");
                isBtConnected = true;
                pairedDevicesList1();
                Log.d("BlueControl", "Kết nối thành công");
            }
            progress.dismiss();
        }
    }

    private void pairedDevicesList1() {

        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            pairedDevices1 = myBluetooth.getBondedDevices();

            if (pairedDevices1.size() > 0) {
                for (BluetoothDevice bt : pairedDevices1) {
                    txtMAC.setText(bt.getName() + " - " + bt.getAddress());
                }
            } else {
                msg("Không tìm thấy thiết bị kết nối.");
            }
        }
    }
    private void Disconnect() {
        if (btSocket != null)
        {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
        finish();
    }
}
