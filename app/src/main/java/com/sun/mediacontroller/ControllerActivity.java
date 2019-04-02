package com.sun.mediacontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import java.util.Locale;


/**
 * @author Sun
 * @date 2019/1/29 13:37
 * @desc 控制器
 */
public class ControllerActivity extends Activity implements View.OnClickListener {
    private static final String url = "https://xueyuan-media.yitong.com/x-multimedia/2018/12/29/adf94469-4e94-4e28-9098-0151a980702a/94d8017f-72a3-428d-a8d5-492ab84a1bd9-960w.m3u8";
    private static final long total = 322;

    // 视频传输服务
    public static final String AV_TRANSPORT = "AVTransport";
    // DMR 设备的控制服务
    public static final String RENDERING_CONTROL = "RenderingControl";

    private boolean isMute;

    private Service avtService;
    private Service rcService;
    private UnsignedIntegerFourBytes instanceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.resume).setOnClickListener(this);
        findViewById(R.id.mute).setOnClickListener(this);

        initSeekBar();
        initParams();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCast();
    }

    private void initSeekBar() {
        SeekBar volumeSeekBar = findViewById(R.id.seek_bar_volume);
        SeekBar progressSeekBar = findViewById(R.id.seek_bar_progress);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setVolumeCast(seekBar.getProgress());
            }
        });

        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int time = (int) (seekBar.getProgress() * 1.0f / seekBar.getMax() * total);
                seekCast(stringForTime(time));
            }
        });
    }

    private String stringForTime(int timeMs) {
        int seconds = timeMs % 60;
        int minutes = timeMs / 60 % 60;
        int hours = timeMs / 3600;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
    }

    private void initParams() {
        avtService = findServiceFromDevice(AV_TRANSPORT);
        rcService = findServiceFromDevice(RENDERING_CONTROL);
        instanceId = new UnsignedIntegerFourBytes("0");
    }

    /**
     * 通过指定服务类型，搜索当前选择的设备的服务
     *
     * @param type 需要的服务类型
     */
    public Service findServiceFromDevice(String type) {
        UDAServiceType serviceType = new UDAServiceType(type);
        Device device = ClingService.getInstance().getSelectDevice();
        if (device == null) {
            return null;
        }
        return device.findService(serviceType);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                playCast();
                break;
            case R.id.stop:
                stopCast();
                break;
            case R.id.pause:
                pauseCast();
                break;
            case R.id.resume:
                resumeCast();
                break;
            case R.id.mute:
                isMute = !isMute;
                setMuteCast(isMute);
                break;
        }
    }

    /**
     * 检查视频传输服务是否存在
     */
    private boolean checkAVTService() {
        if (avtService == null) {
            avtService = findServiceFromDevice(AV_TRANSPORT);
        }
        return avtService == null;
    }

    /**
     * 检查视频播放控制服务是否存在
     */
    private boolean checkRCService() {
        if (rcService == null) {
            rcService = findServiceFromDevice(RENDERING_CONTROL);
        }
        return rcService == null;
    }

    // 开始播放
    private void playCast() {
        if (checkAVTService()) {
            ToastUtils.showShort("视频传输服务不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new Stop(instanceId, avtService) {// 1. 停止上一个播放
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("stopCast success");
                String metadata = ClingUtil.getItemMetadata(url);

                LogUtils.d("setAVTransportURI metadata:" + metadata);
                controlPoint.execute(new SetAVTransportURI(instanceId, avtService, url, metadata) {// 2. 设置传输流数据
                    @Override
                    public void success(ActionInvocation invocation) {
                        LogUtils.d("setAVTransportURI success");

                        resumeCast();// 3. 开始播放
                    }

                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                        LogUtils.d("setAVTransportURI failure msg:" + msg);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("stopCast failure msg:" + msg);
            }
        });
    }

    // 停止播放
    private void stopCast() {
        if (checkAVTService()) {
            ToastUtils.showShort("视频传输服务不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new Stop(instanceId, avtService) {// 1. 停止上一个播放
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("stopCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("stopCast failure msg:" + msg);
            }
        });
    }

    // 暂停播放
    private void pauseCast() {
        if (checkAVTService()) {
            ToastUtils.showShort("视频传输服务不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new Pause(instanceId, avtService) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("pauseCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("pauseCast failure msg:" + msg);
            }
        });
    }

    // 继续播放
    private void resumeCast() {
        if (checkAVTService()) {
            ToastUtils.showShort("视频传输服务不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new Play(instanceId, avtService) {// 开始播放
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("playCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("playCast failure msg:" + msg);
            }
        });
    }

    // 设置音量
    private void setVolumeCast(int volume) {
        if (checkRCService()) {
            ToastUtils.showShort("视频播放控制器不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new SetVolume(instanceId, rcService, volume) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("setVolumeCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("setVolumeCast failure msg:" + msg);
            }
        });
    }

    // 设置进度
    private void seekCast(String progress) {
        if (checkAVTService()) {
            ToastUtils.showShort("视频传输服务不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new Seek(instanceId, avtService, progress) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("seekCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("seekCast failure msg:" + msg);
            }
        });
    }

    // 静音投屏
    private void setMuteCast(boolean mute) {
        if (checkRCService()) {
            ToastUtils.showShort("视频播放控制器不存在");
            return;
        }

        ControlPoint controlPoint = ClingService.getInstance().getUpnpService().getControlPoint();
        controlPoint.execute(new SetMute(instanceId, rcService, mute) {
            @Override
            public void success(ActionInvocation invocation) {
                LogUtils.d("muteCast success");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String msg) {
                LogUtils.d("muteCast failure msg:" + msg);
            }
        });
    }
}
