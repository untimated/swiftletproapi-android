package thesis.com.swiftletpro;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText vUsername;
    private EditText vPassword;
    private static final String SIGN_IN_URL = "http://103.236.201.63/api/v1/SignIn";
    private static final String CACHE_SIGN_IN_URL = "http://103.236.201.63/api/v1/SignInUsingCache";
    private static String FILENAME = "token-tmp.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        vUsername = (EditText) findViewById(R.id.username_txt);
        vPassword = (EditText) findViewById(R.id.password_txt);
    }

    public void onClickRegister(View v){
        Intent in = new Intent(this,RegisterActivity.class);
        startActivity(in);
    }

    public void onClickSignIn(View v){
        final String username = vUsername.getText().toString();
        final String password = vPassword.getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_IN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject res = new JSONObject(response);
                            System.out.println("GET JSONOBJECT : " + res.toString());
                            boolean isError = res.has("error");
                            System.out.println("FINISH GET JSONOBJECT " + isError);
                            if(isError == true){
                                Toast.makeText(MainActivity.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                            }else{
                                JSONArray data = res.getJSONArray("data");
                                JSONObject userdata = data.getJSONObject(0);
                                String usertoken = userdata.getString("token");
                                System.out.println("User Token : " + userdata.getString("token"));

                                Log.i("Info","Begin write to file");
                                File cacheDir = new File(getApplicationContext().getCacheDir(),FILENAME);
                                FileWriter writer = new FileWriter(cacheDir);
                                try{
                                    String content = "" + username + "\n" + password + "\n" + usertoken;
                                    writer.write(content);
                                    writer.close();
                                    Log.i("Info","End write to file");
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                Intent in = new Intent(getApplicationContext(),HomeBridgeActivity.class);
                                startActivity(in);
                                finish();

                            }
                        }catch(Throwable t){
                            Toast.makeText(MainActivity.this, "CATCH ERROR", Toast.LENGTH_LONG).show();
                        }
                        //Toast.makeText(MainActivity.this, "Sign In Succesfully", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(MainActivity.this, "Network Problem", Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }



    public void onClickSignIn2(View v){
        //Intent in = new Intent(this,HomeActivity.class);
        Intent in = new Intent(this,HomeBridgeActivity.class);
        startActivity(in);
        finish();
    }

}
