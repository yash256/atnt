package com.example.farmdroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class MarketsDB {
	private final static String TAG="MarketsDB";
	protected static SQLiteDatabase db;
	
	public MarketsDB(Context ctx){
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
		} 
		
		
/*
        Cursor c = db.rawQuery("SELECT * FROM sqlite_master", null);
        if(c.moveToFirst()) {
        	
        	int ccount = c.getColumnCount();
        	String s = "";
        	int i = 4;
        	//for(int i = 0; i < ccount; i++) 
        		s += c.getColumnName(i) + c.getString(i) + "___";
        	
        	Log.d("Farmdroid", s);
        	while(c.moveToNext()){
        		s = "";
            	//for(int i = 0; i< ccount; i++) 
            		s += c.getColumnName(i) + c.getString(i) + "********";
            	
            	Log.d("Farmdroid", s);
        	}
        }
        c = db.rawQuery("SELECT day_name FROM day_of_week", null);
        if (c.moveToFirst()) {
        	Log.d("Dayofweek", c.getString(0));
        	while (c.moveToNext())
        		Log.d("Dayofweek", c.getString(0));
        }
*/
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

}
