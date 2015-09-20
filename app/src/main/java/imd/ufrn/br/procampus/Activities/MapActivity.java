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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.logging.Logger;

import imd.ufrn.br.procampus.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{

    //Latitude e longitude do centro da UFRN.
    private static LatLng latLngUFRN = new LatLng(-5.837523, -35.203309);
    //Área para posição da camera.
    private static LatLngBounds latLngBoundsUFRN = new LatLngBounds(new LatLng(-5.844020, -35.214237),new LatLng(-5.829642, -35.193234));
    //Instância do mapa.
    GoogleMap googleMap;
    //Usado para no OnCreate o OnMapReadyCallback  ser chamado.
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
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        //Posição inicial da camera na UFRN com zoom 17.
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngUFRN,17));

        //Listener para mudanças de posição da camera.
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //Log.d("onCameraChange","Latitude: "+cameraPosition.target.latitude+" Longitude: "+cameraPosition.target.longitude);
                if(cameraPosition.zoom < 17){
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                }
                areaPermitida(latLngBoundsUFRN,cameraPosition);
            }
        });
    }


    /**
     * @param area
     * @param position
     *
     * Método que muda a posição da camera caso o usuário esteja fora da área da ufrn.
     * O método posiciona a camera na limite do lado da área permitida mais próxima do local que o usuário moveu a camera.
     */
    private void areaPermitida(LatLngBounds area, CameraPosition position){
        if(area.contains(position.target)){
            return;
        }

        double x = position.target.longitude;
        double y = position.target.latitude;

        double amaxX = area.northeast.longitude;
        double amaxY = area.northeast.latitude;
        double aminX = area.southwest.longitude;
        double aminY = area.southwest.latitude;

        if (x < aminX) {x = aminX;}
        if (x > amaxX) {x = amaxX;}
        if (y < aminY) {y = aminY;}
        if (y > amaxY) {y = amaxY;}

        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(y,x),position.zoom));
    }
}
