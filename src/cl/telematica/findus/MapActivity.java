package cl.telematica.findus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MapActivity extends FragmentActivity implements ConnectionCallbacks,
															 OnConnectionFailedListener,
															 LocationListener, 
															 OnMyLocationButtonClickListener,
															 OnMyLocationChangeListener {

	private int userIcon, foodIcon, drinkIcon, shopIcon, otherIcon;
	
	private GoogleMap theMap;
	private Marker userMarker;
	private Location myLocation;
	
	//4° tutorial
	private Marker [] placeMarkers;
	private final int MAX_PLACES = 20;
	private MarkerOptions [] places;
	
	private Marker [] friendsMarkers;
	private final int MAX_USERS = 20;
	private MarkerOptions [] friends;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		Intent intent = getIntent();
		
		//asociacion de iconos a imagenes
		userIcon = R.drawable.yellow_point;
		foodIcon = R.drawable.red_point;
		drinkIcon = R.drawable.blue_point;
		shopIcon = R.drawable.green_point;
		otherIcon = R.drawable.purple_point;
			
		if(theMap==null){
			//manejar con SupportFragmentMannager
			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.the_map);
			theMap = mapFrag.getMap();
			theMap.setMyLocationEnabled(true);
			theMap.setOnMyLocationChangeListener(this);
		}
		
		if(theMap!=null){
			theMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
		}
		// 4° tutorial
		placeMarkers = new Marker[MAX_PLACES];
		friendsMarkers = new Marker[MAX_USERS];
		
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
	
	@Override
	public void onMyLocationChange(Location location) {
		// TODO Auto-generated method stub
		
		myLocation = location;
		double latitude =  location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		
		if(userMarker != null){
			userMarker.remove();
		}
		
		userMarker = theMap.addMarker(new MarkerOptions()
		.position(latLng)
		.title("You are here")
		.icon(BitmapDescriptorFactory.fromResource(userIcon))
		.snippet("Your Last Recorded Location"));
		
		theMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 3000, null);
		
		//codigo agregado por tutorial 3
		String placesSearchStr;
		try{
			placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"+"json?location="
							+URLEncoder.encode(String.valueOf(latitude), "UTF-8")
							+","
							+URLEncoder.encode(String.valueOf(longitude), "UTF-8")
							+"&radius="
							+URLEncoder.encode("100", "UTF-8")
							+"&sensor="
							+URLEncoder.encode("true", "UTF-8")
							+"&types="
							+URLEncoder.encode("food|bar|store|museum|art_gallery", "UTF-8")
							+"&key="
							+URLEncoder.encode("AIzaSyBvHwt3x7gqx27bU7jWbmivU7vqf39atlk", "UTF-8");
			
			new GetPlaces().execute(placesSearchStr);
			
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		String usersSearchStr;
		usersSearchStr = "http://www.mocky.io/v2/545657cdfdfe958b0e9fe720";
		new GetUsers().execute(usersSearchStr);
		
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	private class GetPlaces extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... placesURL) {
			// TODO Auto-generated method stub
			StringBuilder placesBuilder = new StringBuilder();
			
			for(String placeSearchURL : placesURL){
				HttpClient placesClient = new DefaultHttpClient();
				
				try{
					HttpGet placesGet = new HttpGet(placeSearchURL);
					HttpResponse placesResponse = placesClient.execute(placesGet);
					StatusLine placeSearchStatus = placesResponse.getStatusLine();
					
					if(placeSearchStatus.getStatusCode()==200){
						HttpEntity placesEntity = placesResponse.getEntity();
						InputStream placesContent = placesEntity.getContent();
						InputStreamReader placesInput = new InputStreamReader(placesContent);
						BufferedReader placesReader = new BufferedReader(placesInput);
						String lineIn;
						while((lineIn=placesReader.readLine()) != null){
							placesBuilder.append(lineIn);
						}				
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			return placesBuilder.toString();	
		}
		
		//segundo metodo a implementar
		protected void onPostExecute(String result){
			
			if(placeMarkers != null){
				for(int pm=0; pm<placeMarkers.length; pm++ ){
					if(placeMarkers[pm]!=null) placeMarkers[pm].remove();
				}
			}
			
			try{
				JSONObject resultObject = new JSONObject(result);
				JSONArray placesArray = resultObject.getJSONArray("results");
				places = new MarkerOptions[placesArray.length()];
				
				for(int p=0; p<placesArray.length(); p++){
					boolean missingValue=false;
					LatLng placeLL=null;
					String placeName="";
					String vicinity="";
					int currIcon = otherIcon;
					
					try{
					    //attempt to retrieve place data values
						missingValue=false;
						JSONObject placeObject = placesArray.getJSONObject(p);
						JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
						placeLL = new LatLng(
							    Double.valueOf(loc.getString("lat")),
							    Double.valueOf(loc.getString("lng")));
						JSONArray types = placeObject.getJSONArray("types");
						
						for(int t=0; t<types.length(); t++){
						    //what type is it
							String thisType=types.get(t).toString();
							
							if(thisType.contains("food")){
							    currIcon = foodIcon;
							    break;
							}
							else if(thisType.contains("bar")){
							    currIcon = drinkIcon;
							    break;
							}
							else if(thisType.contains("store")){
							    currIcon = shopIcon;
							    break;
							}
						}
						vicinity = placeObject.getString("vicinity");
						placeName = placeObject.getString("name");
					}
					catch(JSONException jse){
					    missingValue=true;
					    jse.printStackTrace();
					}
					
					if(missingValue)    places[p]=null;
					
					else
					    places[p]=new MarkerOptions()
					    .position(placeLL)
					    .title(placeName)
					    .icon(BitmapDescriptorFactory.fromResource(currIcon))
					    .snippet(vicinity);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}	
			
			if(places!=null && placeMarkers!=null){
			    for(int p=0; p<places.length && p<placeMarkers.length; p++){
			        //will be null if a value was missing
			        if(places[p]!=null)
			            placeMarkers[p]=theMap.addMarker(places[p]);
			    }
			}
		}
		
	}
	
	private class GetUsers extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... usersURL) {
			// TODO Auto-generated method stub
			StringBuilder usersBuilder = new StringBuilder();
			
				for(String userSearchURL : usersURL){
					HttpClient usersClient = new DefaultHttpClient();
				
					try{
						HttpGet usersGet = new HttpGet(userSearchURL);
						HttpResponse usersResponse = usersClient.execute(usersGet);
						StatusLine userSearchStatus = usersResponse.getStatusLine();
					
						if(userSearchStatus.getStatusCode()==200){
							HttpEntity usersEntity = usersResponse.getEntity();
							InputStream usersContent = usersEntity.getContent();
							InputStreamReader usersInput = new InputStreamReader(usersContent);
							BufferedReader usersReader = new BufferedReader(usersInput);
							String lineIn;
							while((lineIn=usersReader.readLine()) != null){
								usersBuilder.append(lineIn);
							}				
						}
					
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			
			return usersBuilder.toString();
		}
		//modificar para usuarios en general
		protected void onPostExecute(String result){
			
				if(friendsMarkers != null){
					for(int pm=0; pm<friendsMarkers.length; pm++ ){
						if(friendsMarkers[pm]!=null) friendsMarkers[pm].remove();
					}
				}
			
				try{
					JSONObject resultObject = new JSONObject(result);
					JSONArray friendsArray = resultObject.getJSONArray("results");
					friends = new MarkerOptions[friendsArray.length()];
				
					for(int p=0; p<friendsArray.length(); p++){
						boolean missingValue=false;
						//revisar de acuerdo al modelo
						LatLng friendLL=null;
						String friendName="";
						String vicinity="";
						int currIcon = otherIcon;
					
						try{
							//attempt to retrieve place data values
							missingValue=false;
							JSONObject friendsObject = friendsArray.getJSONObject(p);
							//revisar de acuerdo al modelo
							JSONObject loc = friendsObject.getJSONObject("geometry").getJSONObject("location");
							friendLL = new LatLng(
									Double.valueOf(loc.getString("lat")),
									Double.valueOf(loc.getString("lng")));
							
							vicinity = friendsObject.getString("vicinity");
							friendName = friendsObject.getString("name");
						}
						catch(JSONException jse){
							missingValue=true;
							jse.printStackTrace();
						}
					
						if(missingValue)    friends[p]=null;
					
						else
							friends[p]=new MarkerOptions()
					    	.position(friendLL)
					    	.title(friendName)
					    	.icon(BitmapDescriptorFactory.fromResource(currIcon))
					    	.snippet(vicinity);
					}
				
				}catch(Exception e){
					e.printStackTrace();
				}	
			
				if(friends!=null && friendsMarkers!=null){
					for(int p=0; p<friends.length && p<friendsMarkers.length; p++){
						//will be null if a value was missing
						if(friends[p]!=null)
							friendsMarkers[p]=theMap.addMarker(friends[p]);
					}
				}
			}
		}	
	

}
