package com.sun.mediacontroller;

import android.app.Activity;
import org.fourthline.cling.model.meta.Device;

/**
 * @author Sun
 * @date 2019/1/29 12:06
 * @desc
 */
public interface IBrowserView {
    Activity getContext();

    void onServiceConnected();

    void deviceAdded(Device device);

    void deviceRemoved(Device device);
}
