package thesis.com.swiftletpro;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Splash extends AppCompatActivity {

    private static final String CACHE_SIGN_IN_URL = "http://103.236.201.63/api/v1/SignInUsingCache";
    private static String FILENAME = "token-tmp.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readUserCache();
    }

    public void readUserCache(){
        File cacheDir = new File(getApplicationContext().getCacheDir(),FILENAME);

        if(cacheDir.exists()){
            System.out.println("cache exist - go to home");
            try {
                FileReader reader = new FileReader(cacheDir);
                BufferedReader br = new BufferedReader(reader);
                final String username = br.readLine();
                final String password = br.readLine();
                final String token = br.readLine();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, CACHE_SIGN_IN_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject res = new JSONObject(response);
                                    System.out.println("GET JSONOBJECT : " + res.toString());
                                    boolean isError = res.has("error");
                                    System.out.println("FINISH GET JSONOBJECT " + isError);
                                    if(isError == true){
                                        JSONObject error = res.getJSONObject("error");
                                        String errorMessage = error.getString("message");
                                    }else{
                                        ((MyApplication) getApplication()).setUserToken(token);
                                        System.out.println("usertoken : " + ((MyApplication) getApplication()).getUserToken());
                                        Intent in = new Intent(getApplicationContext(),HomeBridgeActivity.class);
                                        startActivity(in);
                                        finish();
                                    }
                                }catch(Throwable t){

                                    Toast.makeText(Splash.this, "Parse Error SPlash", Toast.LENGTH_LONG).show();
                                }
                                //Toast.makeText(MainActivity.this, "Sign In Succesfully", Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                Toast.makeText(Splash.this, "Network Problem", Toast.LENGTH_LONG).show();
                            }
                        })
                {
                    @Override
                    protected Map<String,String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("username", username);
                        params.put("password", password);
                        params.put("token",token);
                        return params;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(stringRequest);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("cache not exist - go to login page");
            Intent in = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(in);
            finish();
        }
    }

}
