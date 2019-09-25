package server.vo;

import com.alibaba.fastjson.annotation.JSONField;

public class DeviceIdInfo {
    private String mac;

    @JSONField(name = "android_id")
    private String androidId;

    @JSONField(name = "serial_no")
    private String serialNo;

    @JSONField(name = "physics_info")
    private long physicsInfoHash;

    @JSONField(name = "dark_physics_info")
    private long darkPhysicsInfoHash;

    @JSONField(name = "install_id")
    private String installId;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public long getPhysicsInfoHash() {
        return physicsInfoHash;
    }

    public void setPhysicsInfoHash(long physicsInfoHash) {
        this.physicsInfoHash = physicsInfoHash;
    }

    public long getDarkPhysicsInfoHash() {
        return darkPhysicsInfoHash;
    }

    public void setDarkPhysicsInfoHash(long darkPhysicsInfoHash) {
        this.darkPhysicsInfoHash = darkPhysicsInfoHash;
    }

    public String getInstallId() {
        return installId;
    }

    public void setInstallId(String installId) {
        this.installId = installId;
    }
}
