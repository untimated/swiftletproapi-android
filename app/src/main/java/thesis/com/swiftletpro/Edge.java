package thesis.com.swiftletpro;

/**
 * Created by michaelmr on 12/30/16.
 */

public class Edge {
    String mEdgeId;
    String mEdgeIP;
    String mEdgeName;

    public Edge(){};
    public Edge(String id, String ip, String name){
        mEdgeId = id;
        mEdgeIP = ip;
        mEdgeName = name;
    }

    public String getmEdgeId() {
        return mEdgeId;
    }

    public void setmEdgeId(String mEdgeId) {
        this.mEdgeId = mEdgeId;
    }

    public String getmEdgeIP() {
        return mEdgeIP;
    }

    public void setmEdgeIP(String mEdgeIP) {
        this.mEdgeIP = mEdgeIP;
    }

    public String getmEdgeName() {
        return mEdgeName;
    }

    public void setmEdgeName(String mEdgeName) {
        this.mEdgeName = mEdgeName;
    }
    public String getInformation(){
        String i = "Serial : " + mEdgeId + ", IP : " + mEdgeIP + ", Name: " + mEdgeName;
        return i;
    }
}
