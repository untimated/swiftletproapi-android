package thesis.com.swiftletpro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeDeviceActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private Switch switchActuate;
    private Switch switchAutomate;
    private ProgressDialog loading;

    private String returnedJson = new String();
    private List<Edge> edges = new ArrayList<Edge>();
    private String TOKEN;
    private static final String GET_DEVICE_URL = "http://103.236.201.63/api/v1/?:1/MyBridge/?:2/devices";
    private static final String ACTUATE_URL = "http://103.236.201.63/api/v1/MyBridge/Actuate";
    private static final String AUTOMATE_URL = "http://103.236.201.63/api/v1/MyBridge/Automate";
    private static final String STATUS_URL = "http://103.236.201.63/api/v1/MyBridge/?/status";
    private String bridgeSerial;
    private boolean isIntentBridgeAutomate = false;
    private boolean isIntentBridgeActuate = false;
    private int totalHTTPRequest = 2;
    private static int REQUEST_CODE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.home_device_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HomeDeviceActivity.DeviceListAdapter(edges);
        mRecyclerView.setAdapter(mAdapter);
        edges.clear();

        setUpIntentExtra();
        setUpToken();
        httpRequestQueue();

    }

    public void setUpIntentExtra(){
        Intent i = getIntent();
        bridgeSerial = i.getStringExtra("BRIDGE_SERIAL");
    }

    public void setUpToken(){
        TOKEN = ((MyApplication) getApplication()).getUserToken();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                this.overridePendingTransition(R.anim.slide_in_left_to_right,R.anim.slide_out_left_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpSwitch(){
        switchActuate = (Switch)findViewById(R.id.switch_actuate);
        switchAutomate = (Switch)findViewById(R.id.switch_automate);
        switchActuate.setChecked(isIntentBridgeActuate);
        switchAutomate.setChecked(isIntentBridgeAutomate);

        if(isIntentBridgeActuate) {
            switchAutomate.setEnabled(false);
            switchAutomate.setTextColor(Color.GRAY);
        }
        if(isIntentBridgeAutomate){
            switchActuate.setEnabled(false);
            switchActuate.setTextColor(Color.GRAY);
        }

        switchActuate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    switchActuateIsOn(true);
                }else{
                    switchActuateIsOn(false);

                }
            }
        });
        switchAutomate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    switchAutomateIsOn(true);
                }else{
                    switchAutomateIsOn(false);

                }
            }
        });
    }

    public void onClickAddDevice(View v){
        Intent i = new Intent(this,AddDeviceActivity.class);
        i.putExtra("BRIDGE_SERIAL",bridgeSerial);
        startActivityForResult(i,REQUEST_CODE);
    }

    public void httpRequestQueue(){
        loading = setLoading();
        loading.show();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(checkBridgeStatusFromAPI());
        requestQueue.add(showDevicesFromAPI());
    }
    //Get devices from API
    public StringRequest showDevicesFromAPI(){

        System.out.println("BRIDGE_ID : " + bridgeSerial);
        String newGetDeviceUrl_1 = GET_DEVICE_URL.replace("?:1",TOKEN);
        String newGetDeviceUrl_2 = newGetDeviceUrl_1.replace("?:2",bridgeSerial);
        System.out.println("GET_BRIDGE_URL_tmp : " + newGetDeviceUrl_2);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, newGetDeviceUrl_2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            returnedJson = response;
                            JSONObject res = new JSONObject(response);
                            System.out.println("GET JSONOBJECT : " + res.toString());
                            boolean isError = res.has("error");
                            System.out.println("FINISH GET JSONOBJECT, ERROR : " + isError);

                            if(isError == true){
                                JSONObject resdata = res.getJSONObject("error");
                                String error_code = resdata.getString("code");
                                String error_message = resdata.getString("message");
                                Toast.makeText(getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_LONG).show();

                            }else{
                                JSONArray resArray = res.getJSONArray("data");
                                for(int i=0;i <resArray.length(); i++){

                                    Edge e = new Edge();
                                    JSONObject chunk = (JSONObject) resArray.get(i);
                                    e.setmEdgeId(chunk.getString("serial"));
                                    e.setmEdgeIP(chunk.getString("ip"));
                                    e.setmEdgeName(chunk.getString("name"));
                                    edges.add(e);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                            if(totalHTTPRequest - 1 <= 0){
                                System.out.println("Requests Completed");
                                setUpSwitch();
                                loading.dismiss();
                            }else {
                                totalHTTPRequest--;
                                System.out.println("Pending Request : " + totalHTTPRequest);
                            }
                        }catch(Throwable t){
                            //Toast.makeText(getActivity().getApplicationContext(), "CATCH ERROR", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(), "Registration Fail", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Request.Priority getPriority() {
                return Request.Priority.LOW;
            }
        };

        return stringRequest;
    }
    //Change actuator state to API
    public void setBridgeActuator(boolean s){
        final String sw;
        final String m;
        if(s){sw = "1";m = "On";}else{sw = "0"; m = "Off";}
        final ProgressDialog populatingData = setLoading();
        populatingData.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ACTUATE_URL,
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
                                Toast.makeText(getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_SHORT).show();
                                populatingData.dismiss();
                            }else{
                                showSnackBar("Actuator is " + m);
                                populatingData.dismiss();
                            }
                        }catch(Throwable t){
                            Toast.makeText(getApplicationContext(), "Data Problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(), "Network Problem", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("switch", sw);
                params.put("serial", bridgeSerial);
                params.put("token", TOKEN);
                System.out.println("PARAM : " + params.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    //Change automate state to API
    public void setBridgeAutomation(boolean s){
        final String sw;
        final String m;
        if(s){sw = "1";m = "On";}else{sw = "0"; m = "Off";}
        loading = setLoading();
        loading.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AUTOMATE_URL,
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
                                Toast.makeText(getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }else{
                                showSnackBar("Automation is " + m);
                                loading.dismiss();
                            }
                        }catch(Throwable t){
                            Toast.makeText(getApplicationContext(), "Data Problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(), "Network Problem", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("switch", sw);
                params.put("serial", bridgeSerial);
                params.put("token", TOKEN);
                System.out.println("PARAM : " + params.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private StringRequest checkBridgeStatusFromAPI(){
        String statusUrl = STATUS_URL.replace("?",bridgeSerial);
        System.out.println("status_url_tmp : " + statusUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, statusUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            returnedJson = response;
                            JSONObject res = new JSONObject(response);
                            System.out.println("GET JSONOBJECT : " + res.toString());
                            boolean isError = res.has("error");
                            System.out.println("FINISH GET JSONOBJECT, ERROR : " + isError);

                            if(isError == true){
                                JSONObject resdata = res.getJSONObject("error");
                                String error_code = resdata.getString("code");
                                String error_message = resdata.getString("message");
                                Toast.makeText(getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_SHORT).show();
                            }else{
                                JSONObject resData = res.getJSONObject("data");
                                if(resData.getString("automate") == "1"){
                                    isIntentBridgeAutomate = true;
                                }else isIntentBridgeAutomate = false;

                                if(resData.getString("actuate") == "1"){
                                    isIntentBridgeActuate = true;
                                }else isIntentBridgeActuate = false;
                            }
                            if(totalHTTPRequest - 1 <= 0){
                                System.out.println("Request Completed");
                                setUpSwitch();
                                loading.dismiss();
                            }else {
                                totalHTTPRequest--;
                                System.out.println("Pending Request : " + totalHTTPRequest);
                            }
                        }catch(Throwable t){
                            Toast.makeText(getApplicationContext(), "Data Problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(), "Network Problem", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Request.Priority getPriority() {
                return Request.Priority.HIGH;
            }
        };
        return stringRequest;
    }
    private void switchActuateIsOn(boolean m){
        /*
            If actuate switch is on, disable automation switch and paint its text gray
            then change actuate state to API.
            Else, re-enable the automation switch and paint actuate switch's text to gray
        */
        setBridgeActuator(m);
        if(m){
            switchAutomate.setEnabled(false);
            switchAutomate.setTextColor(Color.GRAY);
            switchActuate.setTextColor(getResources().getColor(R.color.swiftBlue));
        }else{
            switchAutomate.setEnabled(true);
            switchActuate.setTextColor(Color.GRAY);
        }
    }
    private void switchAutomateIsOn(boolean m){
        /*
            If automate switch is on, disable actuate switch and paint its text gray
            then change automate state to API.
            Else, re-enable the actuate switch and paint automate switch's text to gray
        */
        setBridgeAutomation(m);
        if(m){
            switchActuate.setEnabled(false);
            switchActuate.setTextColor(Color.GRAY);
            switchAutomate.setTextColor(getResources().getColor(R.color.swiftBlue));
        }else{
            switchActuate.setEnabled(true);
            switchAutomate.setTextColor(Color.GRAY);
        }
    }

    private void showSnackBar(String m){
        Snackbar notif = Snackbar.make(findViewById(R.id.activity_home_device),m,Snackbar.LENGTH_LONG);
        notif.getView().setBackgroundColor(getResources().getColor(R.color.darkGray));
        TextView textView = (TextView) notif.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.lightBlue));

        notif.show();

    }

    //Recycler View Adapter
    public class DeviceListAdapter extends RecyclerView.Adapter<HomeDeviceActivity.DeviceListAdapter.ViewHolder>{
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
                mTitleTextView = (TextView) v.findViewById(R.id.bridge_home_list_title);
                mSerialTextView = (TextView) v.findViewById(R.id.bridge_home_list_serial);
                mIpTextView = (TextView) v.findViewById(R.id.bridge_home_list_ip);
            }

            public void bindDataToHolder(Edge e){
                mEdge = e;
            }

            @Override
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(),DeviceDetailActivity.class);
                i.putExtra("BRIDGE_SERIAL",bridgeSerial);
                i.putExtra("DEVICE_SERIAL",mEdge.getmEdgeId());
                startActivity(i);
                HomeDeviceActivity.this.overridePendingTransition(R.anim.slide_in_right_to_left,R.anim.slide_out_right_to_left);
            }
        }

        public DeviceListAdapter(List<Edge> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public HomeDeviceActivity.DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_list, parent, false);
            return new HomeDeviceActivity.DeviceListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(HomeDeviceActivity.DeviceListAdapter.ViewHolder holder, int position) {
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
    private ProgressDialog setLoading(){
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Populating Data");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        return progress;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            System.out.println("ON ACTIVITY RESULT CALLED");
            edges.clear();
            httpRequestQueue();
        }
    }

}
