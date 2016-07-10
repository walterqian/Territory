package walterqian.territory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.FacebookSdk;
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
import org.json.JSONArray;
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
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

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

        FacebookSdk.sdkInitialize(getApplicationContext());
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

        getSupportActionBar().setTitle("Home");
        createDrawerList();
    }

    public void createDrawerList(){
        String[] items = {"Home","Profile","Timeline", "Logout"};

        mDrawerList = (ListView) findViewById(R.id.drawer_list);

        /* Creating an ArrayAdapter to add items to mDrawerList */
        ArrayAdapter adapter = new ArrayAdapter(this,
                R.layout.drawer_list_item, items);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    /* Setting the adapter to mDrawerList */
        mDrawerList.setAdapter(adapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.blue_flag,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.drawer_close);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.drawer_open);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void populateUserInfo(){
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        name = sharedpreferences.getString("name","Walter Qian");
        email = sharedpreferences.getString("email","failed");

        Log.d("MapsActivity", "Retreived: t" + email);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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
            currentLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 12));

            sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("Lat",String.valueOf(currentLoc.latitude));
            editor.putString("Lng", String.valueOf(currentLoc.longitude));
            editor.commit();

            Log.d("pref: latlng", sharedpreferences.getString("Lat","test") + sharedpreferences.getString("Lng","test"));
            String[] array = {"http://52.40.56.30/getFlags"};
            PostTask post = new PostTask();
            post.execute(array);


        }
    }

    public void makeFlag(LatLng latLng, Boolean visit, Double rate, String title, String url, String snippet){
        Marker marker;
        if (!visit) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeIcon(rate, R.drawable.unvisited_icon))));
        }
        else {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeIcon(rate, R.drawable.visited_icon))));
        }

        CustomMarker custom = new CustomMarker(marker,latLng,visit,rate,title,url,snippet);
        markerArrayList.add(custom);
        markerMap.put(marker,custom);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        for (int k=0; k< markerArrayList.size(); k++) {
            if (cameraPosition.zoom < 8)
                markerArrayList.get(k).marker.setVisible(false);
        }
    }

    public Bitmap resizeIcon(Double rating, int k){
        int size = (int) (rating * 15 + 10);
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),k);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, size, size, false);
        return resizedBitmap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedpreferences.edit();

        CustomMarker customMarker = (CustomMarker) markerMap.get(marker);
        editor.putString("url", customMarker.url);
        editor.putString("snippet", customMarker.snippet);
        editor.putString("title", customMarker.name);
        editor.commit();

        Log.d("markerclick:", sharedpreferences.getString("url", "test"));
        Log.d("markerclick:", sharedpreferences.getString("snippet","test"));

        item_display.update();
        showItemFragment();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
        private String mode = "";

        @Override
        protected String doInBackground(String... data) {
            // Create a new HttpClient and Post Header
            String url = data[0];

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            HttpResponse response;

            JSONObject json = new JSONObject();
            Log.d("MapsActivity: Url",url);

            try {
                //add data
                switch (url){
                    case "http://52.40.56.30/login":
                        json.put("email",email);
                        json.put("name",name);
                        mode = "login";
                        break;
                    case "http://52.40.56.30/getFlags":
                        json.put("email",email);
                        json.put("lat",currentLoc.latitude);
                        json.put("long",currentLoc.longitude);
                        mode = "getflags";
                        break;
                }
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
                return sb.toString();
            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            } catch (JSONException e){

            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {

            result = result.replaceAll("u'", "'");
            result = result.replaceAll("u\"","\"");
            Log.d("MapsActivity onpost: ",result);
            if (mode.equals("getflags")){

                Log.d("got here","");
                try {
                    Log.d("got here","");
                    JSONObject jsnobject = new JSONObject(result);
                    Log.d("got here","");
                    JSONArray jsonArray = jsnobject.getJSONArray("res");
                    Log.d("Maps: Onpostexecute","jsonarray:" + result );
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Log.d("Maps: Onpostexecute","object:" );
                        createFlagFromObject(object);
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        public void createFlagFromObject(JSONObject object){
            try {
                Double rating = Double.parseDouble(object.getString("rating"));
                String name = object.getString("name");
                String url = object.getString("url");
                Double lat = Double.parseDouble(object.getString("lat"));
                Double lon = Double.parseDouble(object.getString("long"));
                LatLng latlng = new LatLng(lat,lon);
                String snippet = object.getString("snippet");

                makeFlag(latlng,false,rating,name,url,snippet);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        //logout
        if (position == 3){
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        }

        // Create a new fragment and specify the planet to show based on position
        /*
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        */
    }

}
