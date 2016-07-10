package walterqian.territory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener {

    private String TAG = "MapActivity";
    private GoogleMap mMap;
    private GoogleApiClient  mGoogleApiClient;
    private Location mLastLocation;
    private LatLng currentLoc;
    private ArrayList<CustomMarker> markerArrayList = new ArrayList<>();
    private HashMap markerMap = new HashMap();
    private String name, email;
    private LoginButton loginButton;
    private ListView mDrawerList;

    public static final String Name = "nameKey";
    public static final String Email = "emailKey";
    SharedPreferences sharedpreferences;
    private TerritoryMarkFragment item_display;
    private View backView;
    private boolean disableClicks;
    private boolean fragmentVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        backView = (View) findViewById(R.id.back_view);

        backView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (fragmentVisible)
                    removeFragment();
                return disableClicks;
            }
        });
        FragmentManager manager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        item_display = new TerritoryMarkFragment();
        createItemFragment();
        //showItemFragment();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        populateUserInfo();
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            }
        });

        PostTask post = new PostTask();
        post.execute();

        createDrawerList();
    }

    public void createDrawerList(){
        String[] items = {"Home","Profile","Timeline", "Logout"};

        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        /* Creating an ArrayAdapter to add items to mDrawerList */
        ArrayAdapter adapter = new ArrayAdapter(this,
                R.layout.drawer_list_item, items);

    /* Setting the adapter to mDrawerList */
        mDrawerList.setAdapter(adapter);
    }

    public void populateUserInfo(){
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        name = sharedpreferences.getString("name","Walter Qian");
        email = sharedpreferences.getString("email","failed");

        Log.d("MapsActivity","Retreived: " + email);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        updateCurrentLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        while(mLastLocation == null)
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        Log.d("MapsActivity", mLastLocation.toString());
        updateCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void updateCurrentLocation(){
        if (mLastLocation != null && mMap != null) {
            LatLng currentLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 12));

            LatLng latLng = new LatLng(37.3861,-122.0839);
            makeFlag(latLng,false,3.0,"FBHQ");
        }
    }

    public void makeFlag(LatLng latLng, Boolean visit, Double rate, String title){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeFlagIcon(rate))));

        CustomMarker custom = new CustomMarker(marker,latLng,visit,rate,title);
        markerArrayList.add(custom);
        markerMap.put(title,custom);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        for (int k=0; k< markerArrayList.size(); k++) {
            if (cameraPosition.zoom < 8)
                markerArrayList.get(k).marker.setVisible(false);
        }
    }

    public Bitmap resizeFlagIcon(Double rating){
        int size = (int) (rating * 6 + 90);
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.blue_flag);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, size, size, false);
        return resizedBitmap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showItemFragment();
        return true;
    }

    public void createItemFragment(){
        Log.d(TAG,"startitemFragment");
        getFragmentManager().beginTransaction()
                .add(R.id.search_item_container, item_display)
                //    .addToBackStack(null)
                .commit();
        getFragmentManager().beginTransaction()
                .hide(item_display)
                .commit();
    }

    public void hideItemFragment(){
        getFragmentManager().beginTransaction()
                .hide(item_display)
                .commit();
    }

    public void showItemFragment(){

//        if (!item_display.isAdded()) {
//            Log.d(TAG,"createFragment: not added");
//            getFragmentManager().beginTransaction()
//                    .add(R.id.search_item_container, item_display)
//                    //    .addToBackStack(null)
//                    .commit();
//        }
//        else {
           // Log.d(TAG,"createFragment: fragment already created");
            getFragmentManager().beginTransaction()
                    .show(item_display)
                    // .addToBackStack(null)
                    .commit();
        //}

        fragmentVisible = true;
        disableClicks = true;
        turnDark(true);
    }

    private void removeFragment() {
        fragmentVisible = false;
        disableClicks = false;
        turnDark(false);
        hideItemFragment();
    }

    public void turnDark(boolean darkScreen){
        if (darkScreen)
            backView.setVisibility(View.VISIBLE);
        else
            backView.setVisibility(View.INVISIBLE);
    }

    private class PostTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... data) {
            // Create a new HttpClient and Post Header
//            String url = data[0];


            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://52.40.56.30/login");
            HttpResponse response;

            JSONObject json = new JSONObject();
            try {
                //add data
                json.put("email", email);
                json.put("name", name);
                StringEntity se = new StringEntity( json.toString());
                Log.d("MapsActivity","JSONObject: " + json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);
                //execute http post
                response = httpclient.execute(httppost);

                InputStream ips  = response.getEntity().getContent();
                BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String s;
                while(true )
                {
                    s = buf.readLine();
                    if(s==null || s.length()==0)
                        break;
                    sb.append(s);

                }
                buf.close();
                ips.close();

                Log.d("MapsActivity","Sent Post Request");
                Log.d("MapsActivity","Response: " + sb.toString());
                return "";
            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            } catch (JSONException e){

            }


            return "Success";
        }
    }
}
