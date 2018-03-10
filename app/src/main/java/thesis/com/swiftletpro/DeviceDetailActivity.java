package thesis.com.swiftletpro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailActivity extends AppCompatActivity {

    private TextView mHumidityValue;
    private TextView mDateValue;
    private TextView mDeviceName;
    private LineChart mChart;
    private LineDataSet mDataSet;
    private LineData mLineData;
    private String bridgeSerial;
    private String deviceSerial;
    private String TOKEN;
    private static final String GET_REPORT_URL = "http://103.236.201.63/api/v1/?:1/MyBridge/?:2/devices/?:3";
    private List<ReportData> reports = new ArrayList<ReportData>();
    private List<Entry> entries = new ArrayList<Entry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHumidityValue = (TextView) findViewById(R.id.humidity_value_txt);
        mDateValue = (TextView) findViewById(R.id.date_value_txt);
        mDeviceName = (TextView) findViewById(R.id.device_value_txt);

        seedEntries();//Seed Random Number, to display something if nothing to display
        mChart = (LineChart) findViewById(R.id.chart);
        mDataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        mDataSet.setColor(getResources().getColor(R.color.lightBlue));
        mDataSet.setValueTextColor(getResources().getColor(R.color.lightBlue));
        mDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mLineData = new LineData(mDataSet);
        mChart.setGridBackgroundColor(getResources().getColor(R.color.lightBlue));
        mChart.setData(mLineData);
        mChart.invalidate();

        Intent i = getIntent();
        bridgeSerial = i.getStringExtra("BRIDGE_SERIAL");
        deviceSerial = i.getStringExtra("DEVICE_SERIAL");
        reports.clear();

        setUpToken();
        getDataFromAPI();

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

    public void setUpToken(){
        TOKEN = ((MyApplication) getApplication()).getUserToken();
    }

    public void seedEntries(){
        for(int i=0; i<10;i++){
            entries.add(new Entry(i,i));
        }
    }

    public void setEntry(){
        System.out.println("Breakpoint ENTRY");
        entries.clear();
        mLineData.removeDataSet(mDataSet);
        Long reference_timestamp = reports.get(0).getDate().getTime();
        //System.out.println("Reference Time : " + reference_timestamp + " toDate : " + reports.get(0).getDate() + " getDate() : " + getDate(reference_timestamp));
        for(ReportData data : reports){
            Long xNew = data.getDate().getTime() - reference_timestamp;
            entries.add(new Entry(xNew, data.getmHumidity()));
        }

        IAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(reference_timestamp);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(xAxisFormatter);
        LineDataSet mDataSet = new LineDataSet(entries,"Humidity Past 60m");
        mLineData.addDataSet(mDataSet);
        mDataSet.setColor(getResources().getColor(R.color.lightBlue));
        mDataSet.setValueTextColor(getResources().getColor(R.color.colorPrimary));
        mDataSet.setDrawFilled(true);
        mDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setDrawGridLines(false);

        mChart.setDescription(null);

        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisRight().setDrawAxisLine(false);

        mChart.getXAxis().setTextColor(getResources().getColor(R.color.colorPrimary));
        mChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorPrimary));
        mChart.getAxisRight().setDrawLabels(false);
        mChart.notifyDataSetChanged();
        mChart.invalidate();

        mHumidityValue.setText("" + reports.get(reports.size()-1).getmHumidity());
        mDateValue.setText(reports.get(reports.size()-1).getDate().toString());
        mDeviceName.setText("Edge ID : "  + deviceSerial);
    }

    public void getDataFromAPI(){
        final ProgressDialog populatingData = setLoading();
        populatingData.show();

        System.out.println("BRIDGE_ID : " + bridgeSerial);
        String newGetDataUrl_1 = GET_REPORT_URL.replace("?:1",TOKEN);
        String newGetDataUrl_2 = newGetDataUrl_1.replace("?:2",bridgeSerial);
        String newGetDataUrl = newGetDataUrl_2.replace("?:3",deviceSerial);
        System.out.println("GET_BRIDGE_URL_tmp : " + newGetDataUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, newGetDataUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            System.out.println("GET JSONOBJECT : " + res.toString());
                            boolean isError = res.has("error");
                            System.out.println("FINISH GET JSONOBJECT, ERROR : " + isError);
                            if(isError == true){
                                JSONObject resdata = res.getJSONObject("error");
                                String error_code = resdata.getString("code");
                                String error_message = resdata.getString("message");
                                Toast.makeText(getApplicationContext(), "Error " + error_code + " " + error_message, Toast.LENGTH_SHORT).show();
                                populatingData.dismiss();
                            }else{
                                JSONArray resArray = res.getJSONArray("data");
                                if(resArray.length()==0){
                                    System.out.println("Data Length 0");
                                    populatingData.dismiss();
                                    noDataSnackbar();
                                }else{
                                    for(int i=0;i <resArray.length(); i++){
                                        JSONObject chunk = resArray.getJSONObject(i);
                                        ReportData e = new ReportData(chunk.getString("device_id"),
                                                chunk.getInt("humidity"),chunk.getInt("temperature"),chunk.getString("created_at"));
                                        reports.add(e);
                                    }
                                    setEntry();
                                    populatingData.dismiss();
                                }

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
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void noDataSnackbar(){
        Snackbar notif = Snackbar.make(findViewById(R.id.activity_device_detail),"No Data Available From Sensor",Snackbar.LENGTH_LONG);
        notif.getView().setBackgroundColor(getResources().getColor(R.color.darkGray));
        TextView textView = (TextView) notif.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.lightBlue));

        notif.show();

    }

    private ProgressDialog setLoading(){
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Populating Data");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        return progress;
    }
}
