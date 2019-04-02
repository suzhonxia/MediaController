package com.sun.mediacontroller;

import android.widget.Toast;
import com.blankj.utilcode.util.LogUtils;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

/**
 * @author Sun
 * @date 2019/1/29 12:04
 * @desc
 */
public class BrowseRegistryListener extends DefaultRegistryListener {
    private IBrowserView browserView;

    public BrowseRegistryListener(IBrowserView browserView) {
        this.browserView = browserView;
    }

    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        browserView.getContext().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                        browserView.getContext(),
                        "Discovery failed of '" + device.getDisplayString() + "': "
                                + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
        deviceRemoved(device);
    }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    public void deviceAdded(final Device device) {
        LogUtils.d("deviceAdded device:" + device);
        browserView.deviceAdded(device);
    }

    public void deviceRemoved(final Device device) {
        LogUtils.d("deviceRemoved device:" + device);
        browserView.deviceRemoved(device);
    }
}
