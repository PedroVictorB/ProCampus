package imd.ufrn.br.procampus.Activities;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.logging.Logger;

import imd.ufrn.br.procampus.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static LatLng latLngUFRN = new LatLng(-5.837523, -35.203309);
    GoogleMap googleMap;
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MapActivity", "MAPA CRIADO");
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        //Posição inicial da camera na UFRN com zoom 17
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngUFRN,17));

        //Listener para mudanças de posição da camera
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("onCameraChange","Latitude: "+cameraPosition.target.latitude+" Longitude: "+cameraPosition.target.longitude);
                if(cameraPosition.zoom < 17){
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                }
            }
        });
    }
}
