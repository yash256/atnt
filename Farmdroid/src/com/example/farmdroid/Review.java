package com.example.farmdroid;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 
public class Review extends Activity {
 
    EditText etuser;
    EditText etreview;
    EditText etrating;
    String marketid;
    Button b;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getString("marketid") != null) {
			marketid = extras.getString("marketid");
		} else {
			marketid = "0";
		}
        setContentView(R.layout.activity_review);
        etuser = (EditText) findViewById(R.id.editText1);
        etreview = (EditText) findViewById(R.id.editText2);
        etrating = (EditText) findViewById(R.id.editText3);
        b= (Button) findViewById(R.id.button1);

        b.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
 
                new HttpAsyncTask().execute("http://72.231.223.67:48000/service.php");
 
            }
        });
 
}
 
      private class HttpAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls) {
 
                JSONObject jsonobj = new JSONObject();
                try {
                    jsonobj.put("user", etuser.getText().toString());
                    jsonobj.put("text", etreview.getText().toString());
                    jsonobj.put("rating", etreview.getText().toString());
                    jsonobj.put("marketid", marketid);
                } catch (JSONException e) {
 
                    e.printStackTrace();
                }
 
                sendPostData(urls[0],jsonobj);
                return null;
            }
 
            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
           }
        }
 
      public void sendPostData(String url, JSONObject jsonobj)
      {
 
            try {
 
                String json = jsonobj.toString();
                StringEntity se = new StringEntity(json);
 
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                httppost.setEntity(se);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream inputstream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                String line="";
                StringBuilder sb = new StringBuilder();
 
                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
 
                String jsonString = sb.toString();
                String result = null;
                try {
                    JSONObject jobj = new JSONObject(jsonString);
                    result = jobj.getString("success");
                    Log.d("RESPONSE", result);
 
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
 
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
 
      }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
 
        //getMenuInflater().inflate(R.menu.send_json3, menu);
        return true;
    }
 
}