package server.vo;

import com.alibaba.fastjson.annotation.JSONField;

public class DeviceIdInfo {
    private String mac;

    @JSONField(name = "android_id")
    private String androidId;

    @JSONField(name = "serial_no")
    private String serialNo;

    @JSONField(name = "physics_info")
    private String physicsInfoHash;

    @JSONField(name = "dark_physics_info")
    private String darkPhysicsInfoHash;

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

    public String getPhysicsInfoHash() {
        return physicsInfoHash;
    }

    public void setPhysicsInfoHash(String physicsInfoHash) {
        this.physicsInfoHash = physicsInfoHash;
    }

    public String getDarkPhysicsInfoHash() {
        return darkPhysicsInfoHash;
    }

    public void setDarkPhysicsInfoHash(String darkPhysicsInfoHash) {
        this.darkPhysicsInfoHash = darkPhysicsInfoHash;
    }

    public String getInstallId() {
        return installId;
    }

    public void setInstallId(String installId) {
        this.installId = installId;
    }
}
