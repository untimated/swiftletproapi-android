package thesis.com.swiftletpro;

/**
 * Created by michaelmr on 12/21/16.
 */

public class Bridge {

    String mBridgeID;
    String mBridgeLocalIP;
    String mBridgeName;
    int mAutomate;
    int mActuate;

    public Bridge(){}

    public Bridge(String id, String ip, String n){
        mBridgeID = id;
        mBridgeLocalIP = ip;
        mBridgeName = n;
    }

    public String getmBridgeID() {
        return mBridgeID;
    }

    public void setmBridgeID(String mBridgeID) {
        this.mBridgeID = mBridgeID;
    }

    public String getmBridgeName() {
        return mBridgeName;
    }

    public void setmBridgeName(String mBridgeName) {
        this.mBridgeName = mBridgeName;
    }

    public String getmBridgeLocalIP() {
        return mBridgeLocalIP;
    }

    public void setmBridgeLocalIP(String mBridgeLocalIP) {
        this.mBridgeLocalIP = mBridgeLocalIP;
    }

    public String getInformation(){
        String i = "Serial : " + mBridgeID + ", IP : " + mBridgeLocalIP + ", Name: " + mBridgeName;
        return i;
    }

    public int ismAutomate() {
        return mAutomate;
    }

    public void setmAutomate(int mAutomate) {
        this.mAutomate = mAutomate;
    }

    public int ismActuate() {
        return mActuate;
    }

    public void setmActuate(int mActuate) {
        this.mActuate = mActuate;
    }
}
