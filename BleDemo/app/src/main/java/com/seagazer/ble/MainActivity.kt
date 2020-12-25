package com.seagazer.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothScanner: BluetoothLeScanner
    private lateinit var output: OutputStream
    private var bondDevice: BluetoothDevice? = null
    private var message = StringBuilder()
    private var clientSocket: BluetoothSocket? = null

    private val uuid = "db764ac8-4b08-7f25-aafe-59d03c27bae3"

    private val permissions: Array<String> = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, 0x111)
            Logger.d("--> check permission")
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "该设备不支持蓝牙BLE", Toast.LENGTH_SHORT).show()
        }
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 0x120)
            Logger.d("--> adapter enable")
        } else {
            bluetoothScanner = bluetoothAdapter.bluetoothLeScanner
        }
        Thread(Listener()).start()
    }

    fun send(view: View) {
        try {
            if (bondDevice == null) {
                val bondedDevices = bluetoothAdapter.bondedDevices
                if (bondedDevices.size == 1) {
                    bondDevice = bondedDevices.elementAt(0)
                    Logger.d("已经配对设备: [name=${bondDevice!!.name}, mac=${bondDevice!!.address}, bondState=${bondDevice!!.bondState}]")
                    Toast.makeText(
                            this,
                            "已经配对设备: [name=${bondDevice!!.name}, mac=${bondDevice!!.address}, bondState=${bondDevice!!.bondState}]",
                            Toast.LENGTH_SHORT
                    ).show()
//            it.connectGatt(this@MainActivity, false, gattCallback)
                    val remoteDevice = bluetoothAdapter.getRemoteDevice(bondDevice!!.address)
                    clientSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
                    clientSocket!!.connect()
                    output = clientSocket!!.outputStream
                    message.append(ed_text.text.toString() + "\n")
                    output.write(message.toString().toByteArray())
                } else {
                    Toast.makeText(this, "测试只支持单设备连接", Toast.LENGTH_SHORT).show()
                }
            } else {
                message.append(ed_text.text.toString() + "\n")
                output.write(message.toString().toByteArray())
            }
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            ex.printStackTrace()
            clientSocket?.close()
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            result.text = msg.obj as String
        }
    }

    private var isDestroy = false

    override fun onDestroy() {
        isDestroy = true
        clientSocket?.close()
        super.onDestroy()
    }

    inner class Listener : Runnable {
        private lateinit var serverSocket: BluetoothServerSocket
        private lateinit var socket: BluetoothSocket
        private lateinit var inputStream: InputStream
        private var outputStream: OutputStream? = null

        init {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "test_socket",
                        UUID.fromString(uuid)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                socket = serverSocket.accept()
                inputStream = socket.inputStream
                outputStream = socket.outputStream
                while (!isDestroy) {
                    val buffer = ByteArray(1024)
                    inputStream.read(buffer)
                    val msg = handler.obtainMessage()
                    msg.obj = String(buffer)
                    // 发送数据
                    handler.sendMessage(msg)
                }
                serverSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                serverSocket.close()
            }
        }

    }

    fun scan(view: View) {
        stopScan()
        startScan()
    }


    val targetDevice = "4C:4F:EE:B4:19:45"// onePlus
//    val targetDevice = "D4:38:9C:9C:BF:F1"// sony

    private fun startScan() {
        Logger.d("--> start scan")
        bluetoothScanner.startScan(scanCallback)
    }

    private fun stopScan() {
        Logger.d("--> stop scan")
        bluetoothScanner.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val scanRecord = it.scanRecord
                val device = it.device
                if (device.address == targetDevice) {
                    Toast.makeText(this@MainActivity, "找到了设备:$targetDevice", Toast.LENGTH_SHORT).show()
                    Logger.d("找到了设备:$device")
                    Logger.d("scanRecord= $scanRecord")
                    // gatt连接设备
                    device.connectGatt(this@MainActivity, false, gattCallback)
                    stopScan()
                }

            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { result ->
                Logger.d("->$result")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Logger.d("->$errorCode")
        }
    }


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            Logger.d("gatt= $gatt, status= $status, txPhy= $txPhy, rxPhy= $rxPhy")
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            Logger.d("gatt= $gatt, status= $status, txPhy= $txPhy, rxPhy= $rxPhy")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // 连接后回调
            //  int STATE_CONNECTED = 2;
            //  int STATE_CONNECTING = 1;
            //  int STATE_DISCONNECTED = 0;
            //  int STATE_DISCONNECTING = 3;
            Logger.d("gatt= $gatt, status= $status, newState= $newState")
            if (newState == 0) {
                gatt?.discoverServices()

                val uuid = UUID.fromString(uuid)
                // 注册数据监听
                val bluetoothGattCharacteristic = BluetoothGattCharacteristic(uuid, 0, 0)
                gatt?.setCharacteristicNotification(bluetoothGattCharacteristic, false)
                // 写入数据
                gatt?.writeDescriptor(bluetoothGattCharacteristic.getDescriptor(uuid).apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                })
                // 远程设备上的特征发生更改，会回调onCharacteristicChanged
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            // 发现服务回调
            Logger.d("gatt= $gatt, status= $status")
            gatt?.let {
                // 获取所有服务
                val services = it.services
                services.forEach { service ->
                    // 遍历服务获取服务下面的所有特征值
                    val characteristics = service.characteristics
                    Logger.d("gattService= [uuid=${service.uuid}]")
                    // 遍历特征值，获取需要使用的特征值
                    characteristics.forEach { character ->
                        Logger.d("characteristic= $character")
                    }
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Logger.d("gatt= $gatt, status= $status, characteristic= $characteristic")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Logger.d("gatt= $gatt, status= $status, characteristic= $characteristic")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Logger.d("gatt= $gatt, characteristic= $characteristic")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Logger.d("gatt= $gatt, status= $status, descriptor= $descriptor")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Logger.d("gatt= $gatt, status= $status, descriptor= $descriptor")

        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            Logger.d("gatt= $gatt, status= $status")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Logger.d("gatt= $gatt, status= $status, rssi= $rssi")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Logger.d("gatt= $gatt, status= $status, mtu= $mtu")
        }
    }

}