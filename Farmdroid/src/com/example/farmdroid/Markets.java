package com.example.farmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class Markets extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markets);
        
        SQLiteDatabase db;
        db = openOrCreateDatabase("MarketsDB", MODE_PRIVATE, null);
        db.execSQL("	DROP TABLE IF EXISTS `day_of_week`;");
        db.execSQL("    CREATE TABLE IF NOT EXISTS `day_of_week` (`day_id` int(11) NOT NULL PRIMARY KEY, `day_name` varchar(20) NOT NULL);	");
        db.execSQL("	INSERT INTO `day_of_week` (`day_id`, `day_name`) VALUES (1, 'Sunday'), (2, 'Monday'), (3, 'Tuesday'), (4, 'Wednesday'), (5, 'Thursday'), (6, 'Friday'), (7, 'Saturday');	");
        db.execSQL("	CREATE TABLE IF NOT EXISTS `location_city` ( `location_id` int(11) NOT NULL PRIMARY KEY,   `name` varchar(30) NOT NULL ) ;	");
        db.execSQL("	CREATE TABLE IF NOT EXISTS `market` (`market_id` int(11) NOT NULL PRIMARY KEY, `Name` varchar(100) NOT NULL DEFAULT '',  `location_id` int(11) NOT NULL, `address` VARCHAR(100) NOT NULL, `from_time` VARCHAR(5) DEFAULT NULL, `to_time` VARCHAR(5) DEFAULT NULL, `from_date` int(11) DEFAULT NULL, `to_date` int(11) DEFAULT NULL, `day_of_operation` int(11) DEFAULT NULL )   ;	");
        db.execSQL("	CREATE TABLE IF NOT EXISTS `review` (`review_id` int(11) NOT NULL PRIMARY KEY, `market_id` int(11) NOT NULL, `user_id` int(11) NOT NULL, `rating` int(11) NOT NULL, `review_text` varchar(1024) NOT NULL, `review_date` date NOT NULL ) ;	");
        db.execSQL("	CREATE TABLE IF NOT EXISTS `user` ( `user_id` varchar(20) NOT NULL PRIMARY KEY, `user_name` varchar(100) NOT NULL, `user_email` varchar(100) NOT NULL );	");
        
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.markets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
