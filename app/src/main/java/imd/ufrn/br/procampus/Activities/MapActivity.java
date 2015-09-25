package imd.ufrn.br.procampus.Activities;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.DateFormat;
import java.util.Date;

import imd.ufrn.br.procampus.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Latitude e longitude do centro da UFRN.
    private static final LatLng latLngUFRN = new LatLng(-5.837523, -35.203309);

    //Área para posição da camera.
    private static final LatLngBounds latLngBoundsUFRN = new LatLngBounds(new LatLng(-5.844020, -35.214237), new LatLng(-5.829642, -35.193234));

    //Poligono para área permitida dos marcadores
    private static final PolygonOptions areaMarcadoresUFRN = MapActivity.polygonLatLngUFRN();

    //Instância do mapa.
    private GoogleMap googleMap;

    //Usado para, no OnCreate, o OnMapReadyCallback  ser chamado.
    private MapFragment mapFragment;

    //Localização Google API
    private GoogleApiClient mGoogleApiClient;

    //Variáveis para o GPS
    private LocationRequest locationRequest;
    private Location minhaLocalizacao;
    private String tempoUltimoUpdate;
    private boolean pedindoLocalizacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !pedindoLocalizacao) {
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngUFRN, 17));//Posição inicial da camera na UFRN com zoom 17.

        //Listener para mudanças de posição da camera.
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom < 17) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                }
                areaPermitida(latLngBoundsUFRN, cameraPosition);
            }
        });

        //Quando o Usuário quiser saber sua localização.
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(minhaLocalizacao.getLatitude(), minhaLocalizacao.getLongitude())));
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("onConnectionSuspended", "Conexão Suspensa.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("onConnectionFailed", "Conexão Falhou!");
    }

    @Override
    public void onLocationChanged(Location location) {
        minhaLocalizacao = location;
        tempoUltimoUpdate = DateFormat.getTimeInstance().format(new Date());
        Log.d("GPS - onLocationChanged", "Data: " + tempoUltimoUpdate + " Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());
    }

    /**
     * @param area
     * @param position
     *
     * Método que muda a posição da camera caso o usuário esteja fora da área da ufrn.
     * O método posiciona a camera na limite do lado da área permitida mais próxima do local que o usuário moveu a camera.
     */
    private void areaPermitida(LatLngBounds area, CameraPosition position) {
        if (area.contains(position.target)) {
            return;
        }

        double x = position.target.longitude;
        double y = position.target.latitude;

        double amaxX = area.northeast.longitude;
        double amaxY = area.northeast.latitude;
        double aminX = area.southwest.longitude;
        double aminY = area.southwest.latitude;

        if (x < aminX) {
            x = aminX;
        }
        if (x > amaxX) {
            x = amaxX;
        }
        if (y < aminY) {
            y = aminY;
        }
        if (y > amaxY) {
            y = amaxY;
        }

        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(y, x), position.zoom));
    }

    /**
     * Constroi a conexão com o google API para uso do GPS.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Começa os updates de localização.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        pedindoLocalizacao = true;
    }


    /**
     * Para os updates de localização.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        pedindoLocalizacao = false;
    }

    /**
     * Cria e configura a chamada de localização do GPS.
     */
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * @return polygon options com as lat e long da UFRN
     */
    public static PolygonOptions polygonLatLngUFRN(){
        return new PolygonOptions().add(
                new LatLng(-5.829542, -35.211024),
                new LatLng(-5.322462, -35.203037),
                new LatLng(-5.836930, -35.197296),
                new LatLng(-5.840077, -35.195152),
                new LatLng(-5.842565, -35.195152),
                new LatLng(-5.844215, -35.196658),
                new LatLng(-5.843530, -35.202476),
                new LatLng(-5.837920, -35.205793),
                new LatLng(-5.838503, -35.210667),
                new LatLng(-5.833147, -35.212351)
        );
    }
}
