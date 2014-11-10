package com.example.farmdroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class MarketsDB {
	private final static String TAG="MarketsDB";
	protected static SQLiteDatabase db;
	protected static String user_uid = "0";
	protected Context context;
	public MarketsDB(Context ctx){
		context = ctx;
		boolean dbExists = false;
		try {
			FileInputStream fis = ctx.openFileInput("databaseExists");
			dbExists = true;
			fis.close();
		} catch (FileNotFoundException e) {
			dbExists = false;
		} catch (IOException e) {
		}
		Log.d(TAG, "DbExists" + dbExists);
		db = ctx.openOrCreateDatabase("MarketsDB", Markets.MODE_PRIVATE, null);
		try {
			FileOutputStream fos = ctx.openFileOutput("databaseExists", Context.MODE_PRIVATE);
			Log.d(TAG,"Opened File");
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		if (!dbExists) {
			List<String> sqls = new ArrayList<String>();
	        AssetManager assetManager = ctx.getAssets();
	        try {
				String[] files = assetManager.list("db");
				if (files.length > 0) {
					Log.d(TAG,"If");
					InputStream one = assetManager.open("db/marketsDatabase.txt");
						InputStreamReader two = new InputStreamReader(one);
						BufferedReader dbfile = new BufferedReader(two);
						String line = null;
						int i = 0;
						while((line = dbfile.readLine()) != null) {
							sqls.add(i++, line);
						}
				}
			} catch (IOException e) {
				Log.d(TAG,"db folder not found in assetmanager");
			}
			//sqls = {"	DROP TABLE IF EXISTS `day_of_week`; "," CREATE TABLE IF NOT EXISTS `day_of_week` (`day_id` int(11) NOT NULL PRIMARY KEY, `day_name` varchar(20) NOT NULL);	" , "INSERT INTO `day_of_week` (`day_id`, `day_name`) VALUES (1, 'Sunday'), (2, 'Monday'), (3, 'Tuesday'), (4, 'Wednesday'), (5, 'Thursday'), (6, 'Friday'), (7, 'Saturday'); "," CREATE TABLE IF NOT EXISTS `location_city` ( `location_id` int(11) NOT NULL PRIMARY KEY,   `name` varchar(30) NOT NULL ) ; ", "	CREATE TABLE IF NOT EXISTS `market` (`market_id` int(11) NOT NULL PRIMARY KEY, `Name` varchar(100) NOT NULL DEFAULT '',  `location_id` int(11) NOT NULL, `address` VARCHAR(100) NOT NULL, `from_time` VARCHAR(5) DEFAULT NULL, `to_time` VARCHAR(5) DEFAULT NULL, `from_date` int(11) DEFAULT NULL, `to_date` int(11) DEFAULT NULL, `day_of_operation` int(11) DEFAULT NULL )   ; "," CREATE TABLE IF NOT EXISTS `review` (`review_id` int(11) NOT NULL PRIMARY KEY, `market_id` int(11) NOT NULL, `user_id` int(11) NOT NULL, `rating` int(11) NOT NULL, `review_text` varchar(1024) NOT NULL, `review_date` date NOT NULL ) ;  ", " CREATE TABLE IF NOT EXISTS `user` ( `user_id` varchar(20) NOT NULL PRIMARY KEY, `user_name` varchar(100) NOT NULL, `user_email` varchar(100) NOT NULL );	"};

			for(String sql :sqls) {
//				Log.d(TAG,sql);
				db.execSQL(sql);
			}
			FileOutputStream fos;
			try {
				fos = ctx.openFileOutput("lastupdate", Context.MODE_PRIVATE);
				Timestamp lastupdate = new Timestamp(2000);
				//Timestamp lastupdate = new Timestamp((Calendar.getInstance()).getTime().getTime());
	    		OutputStreamWriter osw = new OutputStreamWriter(fos);
	    		osw.write(lastupdate.toString());
	    		osw.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "lastupdate not found");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "lastupdate exception");
				e.printStackTrace();
			}

		} 
		updateDatabase();
	}
	public static String getUserUid() {
		return user_uid;
	}
	public Cursor getMarkets(){
		Cursor c=db.rawQuery("SELECT rowid _id, * FROM market",null);
		Log.d("TAG","no of markets="+c.getCount());
		return c;
	}
	
	public static Cursor getMarketByID(int id){
		Cursor c=db.rawQuery("SELECT Name, address from market where market_id=" + id + ";",null);
		return c;
	}
	
	public static Cursor getReviews(int id){
		Cursor c=db.rawQuery("SELECT rowid _id,user_id, rating, review_text from review where market_id="+id+";",null);
		
		return c;
		
	}
	
	public static Cursor getItemsByMarketId(int id){
		String itemsQuery="SELECT item_name, count(item_name) as item_count from item where market_id="+id+ " group by item_name;";
		Cursor c=db.rawQuery(itemsQuery, null);
		Log.d(TAG,itemsQuery);
		Log.d(TAG, ""+c.getCount());
		return c;
	}
	
	public static double getRatingByMarketId(int id){
		Cursor c=db.rawQuery("SELECT rating from reviews where market_id="+id+";", null);
		double rating= 0.0;
		while(c.moveToNext()){
			double rate=c.getFloat(c.getColumnIndex("rating"));
			rating+=rate;
		}
		return rating/c.getCount();
	}
	
	public static boolean userRecommendedItem(int id, String item_name){
		Cursor c=db.rawQuery("SELECT * from item where market_id="+id+", item_name= "+item_name+", user_uid="+MarketsDB.user_uid+";",null);
		if(c.getCount()==0)
			return false;
		else
			return true;
	}
	
	public void updateDatabase() {
		Log.d(TAG, "Update Database");
		new HttpAsyncTask().execute("http://72.231.223.67:48000/fetchDbUpdate.php");
	}
	
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
        	String lastupdate = new Timestamp(0).toString();
        	String user_uid = "0";
        	try {
				FileInputStream fis = context.openFileInput("lastupdate");
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader reader = new BufferedReader(isr);
                lastupdate = reader.readLine();
                user_uid = reader.readLine();
            } catch (FileNotFoundException e1) {
				Log.e(TAG,"lastupdate not found" + e1);
			} catch (IOException e) {
				Log.e(TAG,"lastupdate IOException" + e);
				}
        	if (user_uid == null || user_uid.equals("")) {
        		user_uid = "0";
        	}
        	Cursor markets = db.rawQuery("SELECT MAX(market_id) AS market_id FROM market;", null);
        	String marketid = markets.getString(markets.getColumnIndex("market_id"));
        	JSONObject jsonobj = new JSONObject();
            try {
            	
            	jsonobj.put("user_uid", user_uid);
                jsonobj.put("lastupdate", lastupdate);
                jsonobj.put("market_id", marketid);
            } catch (JSONException e) {

                e.printStackTrace();
            }
            Log.d(TAG, jsonobj.toString() + "HERE JsonObject");

            sendPostData(urls[0],jsonobj);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, "Data Sent!", Toast.LENGTH_LONG).show();
       }
    }
    
  public void sendPostData(String url, JSONObject jsonobj)
  {
	  String useruid = "";

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
            Log.d(TAG,"Fetchupdate"+sb.toString() );
            String jsonString = sb.toString();
            
            try {
                JSONObject jobj = new JSONObject(jsonString);
                
                JSONArray reviewsObj = (JSONArray) jobj.get("reviews");
                int noOfReviews = reviewsObj.length();
                Log.d(TAG,"Reviews " + noOfReviews);
                for (int i = 0; i < noOfReviews; i++) {
                	JSONObject review = reviewsObj.getJSONObject(i);
                    Log.d(TAG,"Review " + review.toString());
                    
                    ContentValues rv = new ContentValues();
                    rv.put("review_id", review.getInt("review_id"));
                    rv.put("market_id", review.getInt("market_id"));
                    rv.put("user_id", review.getString("user_id"));
                    rv.put("rating", review.getInt("rating"));
                    rv.put("review_text", review.getString("review_text"));
                    rv.put("review_date", review.getString("review_date"));
                    if (db.insertWithOnConflict("review", null, rv, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
                    	Log.e(TAG, "Error occured while inserting " + i + "th row "+ review.toString());
                    	return;
                    } else {
                    	Log.d(TAG,"Inserted " + rv.get("user_id") + ":" + rv.get("review_text"));
                    }
                }
                JSONArray itemsObj = (JSONArray) jobj.get("items");
                int noOfItems = itemsObj.length();
                Log.d(TAG,"Items " + noOfItems);
                for (int i = 0; i < noOfItems; i++) {
                	JSONObject item = itemsObj.getJSONObject(i);
                    Log.d(TAG,"Item " + i + "   " + item.toString());
                    
                    ContentValues iv = new ContentValues();
                    iv.put("item_id", item.getInt("itemid"));
                    iv.put("market_id", item.getInt("marketid"));
                    iv.put("item_name", item.getString("name"));
                    iv.put("user_uid", item.getInt("user_uid"));
                    iv.put("item_rating", item.getInt("item_rating"));
                    iv.put("last_updated", item.getString("last_updated"));
                    try {
	                    if (db.insertWithOnConflict("item", null, iv, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
	                    	Log.e(TAG, "Error occured while inserting " + i + "th row " + item.toString());
	                    	return;
	                    } else {
	                    	Log.d(TAG,"Inserted " + iv.get("user_uid") + ":" + iv.get("item_name"));
	                    }
                    } catch (SQLiteConstraintException e) {
                    	
                    }
                }
                JSONArray marketsObj = (JSONArray) jobj.get("markets");
                int noOfMarkets = marketsObj.length();
                Log.d(TAG,"Markets " + noOfMarkets);
                for (int i = 0; i < noOfMarkets; i++) {
                	JSONObject market = marketsObj.getJSONObject(i);
                    Log.d(TAG,"Market " + i + "   " + market.toString());
                    
                    ContentValues iv = new ContentValues();
                    iv.put("market_id", market.getInt("itemid"));
                    iv.put("Name", market.getInt("Name"));
                    iv.put("location_id", market.getString("location_id"));
                    iv.put("address", market.getInt("address"));
                    iv.put("from_date", market.getInt("from_date"));
                    iv.put("to_date", market.getString("to_date"));
                    iv.put("from_time", market.getString("from_time"));
                    iv.put("to_time", market.getString("to_time"));
                    iv.put("day_of_operation", market.getInt("day_of_operation"));
                    try {
	                    if (db.insertWithOnConflict("market", null, iv, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
	                    	Log.e(TAG, "Error occured while inserting " + i + "th row " + market.toString());
	                    	return;
	                    } else {
	                    	Log.d(TAG,"Inserted " + iv.get("market_id") + ":" + iv.get("Name"));
	                    }
                    } catch (SQLiteConstraintException e) {
                    	
                    }
                }
                useruid = jobj.getString("user_uid");
            } catch (JSONException e) {
                Log.d(TAG,"JSONEx" + e);
            }
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("lastupdate",  Context.MODE_PRIVATE));
			Timestamp lastupdate = new Timestamp((Calendar.getInstance()).getTime().getTime());
    		osw.write(lastupdate.toString() + "\n");
    		osw.write(useruid);
    		user_uid = useruid;
    		osw.close();
    		Log.d(TAG,"Last updated" + lastupdate.toString());
        } catch (ClientProtocolException e) {
        	Log.d(TAG,"Client protocol exception" + e);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "lastupdate not found" + e);
		} catch (IOException e) {
			Log.d(TAG, "lastupdate exception" + e);
        }

  }

}
