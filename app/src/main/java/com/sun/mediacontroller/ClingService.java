package com.sun.mediacontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;

/**
 * @author Sun
 * @date 2019/1/29 13:42
 * @desc
 */
public class ClingService {
    private static ClingService mInstance;

    private Device selectDevice;

    private IBrowserView browserView;
    private AndroidUpnpService upnpService;
    private BrowseRegistryListener registryListener;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            browserView.onServiceConnected();

            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    private ClingService(IBrowserView browserView) {
        this.browserView = browserView;
    }

    public static ClingService getInstance() {
        if (mInstance == null) {
            mInstance = new ClingService(null);
        }
        return mInstance;
    }

    public static ClingService getInstance(IBrowserView browserView) {
        if (mInstance == null) {
            mInstance = new ClingService(browserView);
        }
        return mInstance;
    }

    public void bindService() {
        registryListener = new BrowseRegistryListener(browserView);

        // This will start the UPnP service if it wasn't already started
        browserView.getContext().bindService(new Intent(browserView.getContext(), AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        browserView.getContext().unbindService(serviceConnection);
    }

    public AndroidUpnpService getUpnpService() {
        return upnpService;
    }

    public void setSelectDevice(Device selectDevice) {
        this.selectDevice = selectDevice;
    }

    public Device getSelectDevice() {
        return selectDevice;
    }
}
