package thesis.com.swiftletpro;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDeviceActivity extends AppCompatActivity {

    private static RecyclerView mRecyclerView;
    private static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;

    //5037 for Bridge Discovery, 5038 for Device Discovery
    private static final int MY_UDP_PORT = 5038;
    private static final int TARGET_UDP_PORT = 55056;
    private static final String ADD_DEVICE_URL = "http://103.236.201.63/api/v1/AddDevice";
    private static String TOKEN;
    private ProgressDialog p;
    private static List<Edge> edges = new ArrayList<Edge>();
    private static String bridgeSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.add_device_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Intent i = getIntent();
        bridgeSerial = i.getStringExtra("BRIDGE_SERIAL");
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
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void finish(){
        Intent i = new Intent(this,HomeDeviceActivity.class);
        setResult(Activity.RESULT_OK,i);
        super.finish();
    }

    public void onClickDiscoverDevice(View v) throws SocketException {
        System.out.println("Discover Button Clicked");
        System.out.println("TOKEN : " + TOKEN);
        p = setLoading();
        p.show();
        System.out.println("Start Network Thread");
        edges.clear();
        AddDeviceActivity.SendUDPTask task1 = new AddDeviceActivity.SendUDPTask();
        task1.execute();
    }
    private class SendUDPTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                JSONObject sendMessage = new JSONObject();
                try {
                    sendMessage.put("serial","B001H");
                    sendMessage.put("message","find-bridge");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String s = sendMessage.toString();
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
                DatagramSocket socket = new DatagramSocket(MY_UDP_PORT);
                socket.setSoTimeout(1000);
                System.out.println("Socket Created PORT : " + socket.getLocalPort());

                for(int i = 0; i<10; i++) {
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
                        JSONArray resDataArray = res.getJSONArray("data");
                        for(int j = 0; j<resDataArray.length();j++){
                            JSONObject dataHolder = resDataArray.getJSONObject(j);
                            String sID =  dataHolder.getString("serial");
                            String sName = dataHolder.getString("name");
                            String sIP = dataHolder.getString("ip");
                            edges.add(new Edge(sID,sIP,sName));
                        }
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
            getUDPTaskIsComplete();
            System.out.println("GET UDP Thread Closed");
        }



    }
    private ProgressDialog setLoading(){
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Discovering Nearest Bridges...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        return progress;
    }
    public void printDiscoveredEdges(){
        if(!edges.isEmpty()) {
            for (Edge e : edges) {
                System.out.println(e.getInformation());
            }
        }
    }
    public void sendUDPTaskIsComplete(){
        GetUDPTask task2 = new GetUDPTask();
        task2.execute();
    }
    public void getUDPTaskIsComplete(){
        p.dismiss();
        printDiscoveredEdges();
        mAdapter = new AddDeviceActivity.MyAdapter(edges);
        mRecyclerView.setAdapter(mAdapter);
    }

    public class MyAdapter extends RecyclerView.Adapter<AddDeviceActivity.MyAdapter.ViewHolder>{
        private List<Edge> mDataset;
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case
            private TextView mTitleTextView;
            private TextView mSerialTextView;
            private TextView mIpTextView;
            private Edge mEdge;

            public ViewHolder(View v) {
                super(v);
                itemView.setOnClickListener(this);
                mTitleTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_title);
                mSerialTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_serial);
                mIpTextView = (TextView) v.findViewById(R.id.bridge_discovery_list_ip);
            }

            public void bindDataToHolder(Edge e){
                mEdge = e;
            }

            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_LONG).show();
                AddDeviceActivity.RegisterEdgeDialogFragment dialog = new AddDeviceActivity.RegisterEdgeDialogFragment();
                dialog.setEdge(mEdge);
                dialog.show(getSupportFragmentManager(),"Register Device");
            }
        }

        public MyAdapter(List<Edge> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public AddDeviceActivity.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bridge_discovery_list, parent, false);
            return new AddDeviceActivity.MyAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AddDeviceActivity.MyAdapter.ViewHolder holder, int position) {
            Edge e = mDataset.get(position);
            holder.mTitleTextView.setText(e.getmEdgeName());
            holder.mSerialTextView.setText(e.getmEdgeId());
            holder.mIpTextView.setText(e.getmEdgeIP());
            holder.bindDataToHolder(e);
        }

        @Override
        public int getItemCount(){
            return mDataset.size();
        }
    }

    public static class RegisterEdgeDialogFragment extends DialogFragment {

        private Edge mEdge;
        private boolean isSuccessfulRequest = false;
        public void setEdge(Edge e){
            mEdge = e;
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
            builder.setMessage("Register This Edge Device ?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //On Click Yes, Register device to api using POST method
                    System.out.println("isSuccessfulRequest : " + isSuccessfulRequest);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_DEVICE_URL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject res = new JSONObject(response);
                                        System.out.println("GET JSONOBJECT : " + res.toString());
                                        boolean isError = res.has("error");
                                        System.out.println("FINISH GET JSONOBJECT " + isError);
                                        if(isError == true){
                                            Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                                        }else{
                                            //Toast.makeText(getActivity().getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();
                                            isSuccessfulRequest = true;
                                            System.out.println("isSuccessfulRequest : " + isSuccessfulRequest);
                                            if(isSuccessfulRequest){
                                                System.out.println("isSuccessfulRequest");
                                                int removeIndex = edges.indexOf(mEdge);
                                                edges.remove(mEdge);
                                                mRecyclerView.removeViewAt(removeIndex);
                                                mAdapter.notifyItemRemoved(removeIndex);
                                                mAdapter.notifyItemRangeChanged(removeIndex,edges.size());
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }catch(Throwable t){
                                        //Toast.makeText(getActivity().getApplicationContext(), "CATCH ERROR", Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Response.ErrorListener(){
                                @Override
                                public void onErrorResponse(VolleyError error){
                                    //Toast.makeText(getActivity().getApplicationContext(), "Registration Fail", Toast.LENGTH_LONG).show();
                                }
                            })
                    {
                        @Override
                        protected Map<String,String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("name", mEdge.getmEdgeName());
                            params.put("serial", mEdge.getmEdgeId());
                            params.put("ip", mEdge.getmEdgeIP());
                            params.put("bridge_serial", bridgeSerial);
                            params.put("token", TOKEN);
                            return params;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
                    requestQueue.add(stringRequest);


                    //Intent myIntent = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
                    //startActivity(myIntent);

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
