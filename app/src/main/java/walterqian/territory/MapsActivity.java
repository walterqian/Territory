package walterqian.territory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient  mGoogleApiClient;
    private Location mLastLocation;
    private LatLng currentLoc;
    private ArrayList<Marker> markerArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc,12));
            makeFlag(new LatLng(0.0,0.0));
        }
    }

    public void makeFlag(LatLng latLng){
        LatLng fbHQ = new LatLng(37.484556,-122.147845);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(fbHQ)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeFlagIcon(60,60))));
        markerArrayList.add(marker);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        for (int k=0; k< markerArrayList.size(); k++) {
            if (cameraPosition.zoom < 8)
                markerArrayList.get(k).setVisible(false);
        }
    }

    public Bitmap resizeFlagIcon(int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.blue_flag);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == markerArrayList.get(0)){
            return true;
        }
        return false;
    }
}
