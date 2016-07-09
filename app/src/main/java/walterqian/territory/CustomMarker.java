package walterqian.territory;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by walterqian on 7/9/16.
 */
public class CustomMarker {

    public LatLng latlng;
    public Boolean visited;
    public Double rating;
    public String name;
    public Marker marker;

    CustomMarker(Marker flag,LatLng latLng, Boolean visit, Double rate, String title){
        latlng = latLng;
        visited = visit;
        rating = rate;
        name = title;
        marker = flag;
    }

}
