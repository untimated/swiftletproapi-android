package thesis.com.swiftletpro;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.tv.TvContract;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddBridgeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private static Context mContext;
    private static Activity mActivity;

    private static final int MY_UDP_PORT = 5037;
    private static final int TARGET_UDP_PORT = 55056;
    private static final String ADD_BRIDGE_URL = "http://103.236.201.63/api/v1/AddBridge";
    private ProgressDialog p;
    private static String TOKEN;
    private List<Bridge> bridges = new ArrayList<Bridge>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bridge);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.add_bridge_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mContext = this.getApplicationContext();
        mActivity = this;
        Intent i = getIntent();
        System.out.println("INTENT VALUE : " + i.getStringExtra("BRIDGE_SERIAL"));
        setUpToken();
    }
    public void setUpToken(){
        TOKEN = ((MyApplication) getApplication()).getUserToken();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickDiscover(View v) throws SocketException {

        System.out.println("Discover Button Clicked");
        System.out.println("TOKEN : " + TOKEN);

        p = setLoading();
        p.show();
        System.out.println("Start Network Thread");
        //Flush Remaining Data
        bridges.clear();
        SendUDPTask task1 = new SendUDPTask();
        task1.execute();
    }

    // Uses AsyncTask to create a task away from the main UI thread.
    private class SendUDPTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                String s = "Hello Bridge";
                DatagramSocket socket = new DatagramSocket(MY_UDP_PORT);
                System.out.println("Socket Created PORT : " + socket.getLocalPort());
                System.out.println("Socket Address : " + socket.getLocalSocketAddress());
                socket.setBroadcast(true);
                System.out.println("Broadcast Set to True");

                InetAddress local = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(s.getBytes(),
                        s.length(), local, TARGET_UDP_PORT);
                System.out.println("Packet Created, Broadcast Address : " + local.toString());
                socket.send(packet);
                System.out.println("Packet Sent");

                socket.close();
                System.out.println("Socket Closed");
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }

        }
        @Override
        protected void onPostExecute(Integer i) {
            sendUDPTaskIsComplete();
            System.out.println("SendUDP Task Thread Closed");
        }

    }
    private class GetUDPTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids){
            try{
                System.out.println("Receiving");
                MulticastSocket socket = new MulticastSocket(MY_UDP_PORT);
                socket.setSoTimeout(1000);
                System.out.println("Socket Created PORT : " + socket.getLocalPort());
                System.out.println("IP Address : " + getLocalIP());

                //String publicIP = getPublicIP();System.out.println("Public IP : " + publicIP);
                //boolean clientStillSending = true;
                for(int i = 0; i<5; i++) {
                    byte[] message = new byte[1500];
                    DatagramPacket packet = new DatagramPacket(message, message.length);
                    System.out.println("Socket.Receive()");
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException s) {
                        System.out.println("Receive Timeout ");
                        continue;
                    }
                    System.out.println("Socket.Receive() ---- ");
                    String text = new String(message, 0, packet.getLength());
                    System.out.println("MESSAGE RECEIVED FROM " + packet.getAddress().toString() + " :" + text);
                    try {
                        JSONObject res = new JSONObject(text);
                        String sID = res.getString("serial");
                        String sIP = res.getString("ip");
                        String sName = res.getString("name");
                        bridges.add(new Bridge(sID, sIP, sName));
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                socket.close();
                System.out.println("Closed");

            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            printDiscoveredBridges();
            getUDPTaskIsComplete();
            p.dismiss();
            System.out.println("GET UDP Thread Closed");
        }



    }
    public void sendUDPTaskIsComplete(){
        GetUDPTask task2 = new GetUDPTask();
        task2.execute();
    }
    public void getUDPTaskIsComplete(){
        mAdapter = new MyAdapter(bridges);
        mRecyclerView.setAdapter(mAdapter);
    }
    public void printDiscoveredBridges(){
        if(!bridges.isEmpty()) {
            for (Bridge b : bridges) {
                System.out.println(b.getInformation());
            }
        }
    }
    private InetAddress getBroadcastAddress() throws IOException {
        System.out.println("getBroadcastAddress()");
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            Log.d("test", "Could not get dhcp info");
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        System.out.println("Return Broadcast Address");
        return InetAddress.getByAddress(quads);
    }
    private String getLocalIP() throws IOException{
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
    private ProgressDialog setLoading(){
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Discovering Nearest Bridges...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        return progress;
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        private List<Bridge> mDataset;
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case
            private TextView mTitleTextView;
            private TextView mSerialTextView;
            private TextView mIpTextView;
            private Bridge mBridge;

            public ViewHolder(View v) {
                super(v);
                itemView.setOnClickListener(this);
                mTitleTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_title);
                mSerialTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_serial);
                mIpTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_ip);
            }

            public void bindDataToHolder(Bridge b){
                mBridge = b;
            }

            @Override
            public void onClick(View v){
                /*Toast.makeText(getApplicationContext(), "Clicked",
                        Toast.LENGTH_LONG).show();*/
                RegisterBridgeDialogFragment dialog = new RegisterBridgeDialogFragment();
                dialog.setBridge(mBridge);
                dialog.show(getSupportFragmentManager(),"Register Device");
            }
        }

        public MyAdapter(List<Bridge> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bridge_discovery_list, parent, false);
            return new MyAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            Bridge bridge = mDataset.get(position);
            holder.mTitleTextView.setText(bridge.getmBridgeName());
            holder.mSerialTextView.setText(bridge.getmBridgeID());
            holder.mIpTextView.setText(bridge.getmBridgeLocalIP());
            holder.bindDataToHolder(bridge);
        }

        @Override
        public int getItemCount(){
            return mDataset.size();
        }
    }
    public static void onCompleteRequest(){
        System.out.println("Request Finish");
        Intent myIntent = new Intent(mContext, HomeBridgeActivity.class);
        System.out.println("Intent Created");
        mActivity.setResult(Activity.RESULT_OK,myIntent);
        System.out.println("Intent Set Result");
        mActivity.finish();
    }
    public static class RegisterBridgeDialogFragment extends DialogFragment{

        private Bridge mBridge;

        public void setBridge(Bridge b){
            mBridge = b;
        }
        protected String getPublicIP(){
            String url = "http://whatismyipaddress.com";
            try {
                Document document = Jsoup.connect(url).get();
                Elements links = document.getElementsByTag("a");
                String publicIP = links.get(9).text();
                return publicIP;
            }catch(IOException e){
                return "Not Found";
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Register This Device ?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //On Click Yes, Register device to api using POST method

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_BRIDGE_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            try {
                                                JSONObject res = new JSONObject(response);
                                                System.out.println("GET JSONOBJECT : " + res.toString());
                                                boolean isError = res.has("error");
                                                System.out.println("FINISH GET JSONOBJECT " + isError);
                                                if(isError == true){
                                                    JSONObject resdata = res.getJSONObject("error");
                                                    String error_code = resdata.getString("code");
                                                    String error_message = resdata.getString("message");
                                                    Toast.makeText(getActivity().getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_LONG).show();
                                                }else{
                                                    onCompleteRequest();
                                                }
                                            }catch(Throwable t){
                                                //Toast.makeText(getActivity().getApplicationContext(), "CATCH ERROR", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener(){
                                        @Override
                                        public void onErrorResponse(VolleyError error){
                                            Toast.makeText(getActivity().getApplicationContext(), "Network Problem", Toast.LENGTH_LONG).show();
                                        }
                                    })
                            {
                                @Override
                                protected Map<String,String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("name", mBridge.getmBridgeName());
                                    params.put("serial", mBridge.getmBridgeID());
                                    params.put("ip", getPublicIP());
                                    params.put("token", TOKEN);
                                    return params;
                                }
                            };

                            RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
                            requestQueue.add(stringRequest);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            return builder.create();
        }
    }
}
