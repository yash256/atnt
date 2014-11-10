package com.example.farmdroid;


import android.app.ActionBar.LayoutParams;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RadialGradient;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Details extends ListActivity {
	int marketID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle intentData=getIntent().getExtras();
		marketID=intentData.getInt("marketID");
		SQLiteDatabase tempdb=MarketsDB.db;
		Cursor marketInfo=MarketsDB.getMarketByID(marketID);
		marketInfo.moveToFirst();
		String market_name=marketInfo.getString(marketInfo.getColumnIndex("Name"));
		String market_address=marketInfo.getString(marketInfo.getColumnIndex("address"));
		setContentView(R.layout.market_details);
		TextView marketNameTV=(TextView) findViewById(R.id.market_name);
		TextView marketAddressTV=(TextView) findViewById(R.id.market_address);
		marketNameTV.setText(market_name);
		marketAddressTV.setText(market_address);
		RatingBar rating=(RatingBar) findViewById(R.id.ratingBar1);
		rating.setRating((float) 4.00);		//TODO add actual rating
		rating.setEnabled(false);
		Cursor reviews=MarketsDB.getReviews(marketID);
		@SuppressWarnings("deprecation")
		ListAdapter adapter=new SimpleCursorAdapter(this, R.layout.listview_reviews, reviews, new String[]{"user_id", "review_text"}, new int[]{R.id.user_name, R.id.user_text});
        setListAdapter(adapter);
        
        Cursor items=MarketsDB.getItemsByMarketId(marketID);
		Log.d("DETAILS","size of cursor in populateItems "+items.getCount());
		LinearLayout topsItemsLayout = (LinearLayout) findViewById(R.id.top_items);
        @SuppressWarnings("deprecation")
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        while(items.moveToNext()){
        	
        	String item=items.getString(items.getColumnIndex("item_name"));
        	Log.d("DETAILS","cursor item="+item);
        	int item_count=items.getInt(items.getColumnIndex("item_count"));
        	final Button b =new Button(this);
        	b.setText(item+" "+item_count);
        	b.setLayoutParams(params);
        	b.setOnClickListener(new OnClickListener() {
        		 
                @Override
                public void onClick(View arg0) {
     
                    Log.d("DETAILS","items,count"+b.getText());
     
                }
            });
        	topsItemsLayout.addView(b);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	/*public void populateItems(Context ctx){

		Cursor items=MarketsDB.getItemsByMarketId(marketID);
		Log.d("DETAILS","size of cursor in populateItems "+items.getCount());
		LinearLayout layout = (LinearLayout) findViewById(R.id.top_items);
        @SuppressWarnings("deprecation")
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        while(items.moveToNext()){
        	
        	String item=items.getString(items.getColumnIndex("item_name"));
        	Log.d("DETAILS","cursor item="+item);
        	int item_count=items.getInt(items.getColumnIndex("item_count"));
        	TextView b =new TextView(ctx);
        	b.setLayoutParams(params);
        	b.setText(item+" "+item_count);
     
        }
	}*/
}
