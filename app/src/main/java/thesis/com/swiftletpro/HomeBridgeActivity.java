package thesis.com.swiftletpro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeBridgeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private String returnedJson = new String();
    private List<Bridge> bridges = new ArrayList<Bridge>();
    private static String FILENAME = "token-tmp.txt";
    private String TOKEN;
    private static final String GET_BRIDGE_URL = "http://103.236.201.63/api/v1/?/MyBridges";
    private static int REQUEST_CODE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_bridge);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set Side Nav
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.home_bridge_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setLayoutManager(mLayoutManager);
        bridges.clear();
        mAdapter = new HomeBridgeActivity.HomeListAdapter(bridges);
        mRecyclerView.setAdapter(mAdapter);

        setUpToken();
        showBridgesFromAPI();

    }

    public void setUpToken(){
        TOKEN = ((MyApplication) getApplication()).getUserToken();
    }

    public void onClickAddBridge(View v){
        Intent i = new Intent(this, AddBridgeActivity.class);
        startActivityForResult(i,REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_bridge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_logout){
            File cacheDir = new File(getApplicationContext().getCacheDir(),FILENAME);
            cacheDir.delete();
            System.out.println("Cache Deleted");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Get Bridges From API
    public void showBridgesFromAPI(){
        final ProgressDialog populatingData = setLoading();
        populatingData.show();
        String newGetBridgeUrl = GET_BRIDGE_URL.replace("?",TOKEN);
        System.out.println("GET_BRIDGE_URL_tmp : " + newGetBridgeUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, newGetBridgeUrl,
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
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                            }else{
                                JSONArray resArray = res.getJSONArray("data");
                                for(int i=0;i <resArray.length(); i++){
                                    Bridge tmp = new Bridge();
                                    JSONObject chunk = (JSONObject) resArray.get(i);
                                    tmp.setmBridgeID(chunk.getString("serial"));
                                    tmp.setmBridgeLocalIP(chunk.getString("ip"));
                                    tmp.setmBridgeName(chunk.getString("name"));
                                    tmp.setmActuate(chunk.getInt("actuate"));
                                    tmp.setmAutomate(chunk.getInt("automate"));
                                    bridges.add(tmp);
                                }

                                mAdapter.notifyDataSetChanged();
                                System.out.println("Successful");
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
                });
        /*{
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", mBridge.getmBridgeName());
                params.put("serial", mBridge.getmBridgeID());
                params.put("ip", mBridge.getmBridgeLocalIP());
                params.put("token", "a28e83a7e897b4cfee3720599628a902");
                return params;
            }
        };*/

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    //Recycler View Adapter
    public class HomeListAdapter extends RecyclerView.Adapter<HomeBridgeActivity.HomeListAdapter.ViewHolder>{
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
                mTitleTextView = (TextView) v.findViewById(R.id.bridge_home_list_title);
                mSerialTextView = (TextView) v.findViewById(R.id.bridge_home_list_serial);
                mIpTextView = (TextView) v.findViewById(R.id.bridge_home_list_ip);
            }

            public void bindDataToHolder(Bridge b){
                mBridge = b;
            }

            @Override
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(),HomeDeviceActivity.class);
                i.putExtra("BRIDGE_SERIAL",mBridge.getmBridgeID());
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right_to_left,R.anim.slide_out_right_to_left);

            }
        }

        public HomeListAdapter(List<Bridge> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public HomeBridgeActivity.HomeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_list, parent, false);
            return new HomeBridgeActivity.HomeListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(HomeBridgeActivity.HomeListAdapter.ViewHolder holder, int position) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            System.out.println("ON ACTIVITY RESULT CALLED");
            bridges.clear();
           showBridgesFromAPI();
            // deal with the item yourself

        }
    }

    private ProgressDialog setLoading(){
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Populating Data");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        return progress;
    }
}
