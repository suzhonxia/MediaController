package com.sun.mediacontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;


/**
 * @author Sun
 * @date 2019/1/29 11:55
 * @desc
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, IBrowserView, AdapterView.OnItemClickListener {
    private ClingService clingService;

    private ArrayAdapter<DeviceDisplay> listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.router).setOnClickListener(this);
        findViewById(R.id.debug).setOnClickListener(this);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        // Fix the logging integration between java.util.logging and Android internal logging
        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new FixedAndroidLogHandler()
        );

        clingService = ClingService.getInstance(this);
        clingService.bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clingService != null) {
            clingService.unbindService();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                if (clingService == null)
                    break;
                Toast.makeText(this, R.string.searchingLAN, Toast.LENGTH_SHORT).show();
                clingService.getUpnpService().getRegistry().removeAllRemoteDevices();
                clingService.getUpnpService().getControlPoint().search();
                break;
            // DOC:OPTIONAL
            case R.id.router:
                if (clingService != null) {
                    Router router = clingService.getUpnpService().get().getRouter();
                    try {
                        if (router.isEnabled()) {
                            Toast.makeText(this, R.string.disablingRouter, Toast.LENGTH_SHORT).show();
                            router.disable();
                        } else {
                            Toast.makeText(this, R.string.enablingRouter, Toast.LENGTH_SHORT).show();
                            router.enable();
                        }
                    } catch (RouterException ex) {
                        Toast.makeText(this, getText(R.string.errorSwitchingRouter) + ex.toString(), Toast.LENGTH_LONG).show();
                        ex.printStackTrace(System.err);
                    }
                }
                break;
            case R.id.debug:
                Logger logger = Logger.getLogger("org.fourthline.cling");
                if (logger.getLevel() != null && !logger.getLevel().equals(Level.INFO)) {
                    Toast.makeText(this, R.string.disablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, R.string.enablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }
                break;
            // DOC:OPTIONAL
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        clingService.setSelectDevice(((DeviceDisplay) parent.getItemAtPosition(position)).device);
        startActivity(new Intent(this, ControllerActivity.class));
    }

    @Override
    public Activity getContext() {
        return this;
    }

    @Override
    public void onServiceConnected() {
        listAdapter.clear();
    }

    @Override
    public void deviceAdded(final Device device) {
        runOnUiThread(new Runnable() {
            public void run() {
                DeviceDisplay d = new DeviceDisplay(device);
                int position = listAdapter.getPosition(d);
                if (position >= 0) {
                    // Device already in the list, re-set new value at same position
                    listAdapter.remove(d);
                    listAdapter.insert(d, position);
                } else {
                    listAdapter.add(d);
                }
            }
        });
    }

    @Override
    public void deviceRemoved(final Device device) {
        runOnUiThread(new Runnable() {
            public void run() {
                listAdapter.remove(new DeviceDisplay(device));
            }
        });
    }
}
