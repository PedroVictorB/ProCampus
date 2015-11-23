package imd.ufrn.br.procampus.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.utils.OAuthTokenRequest;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    private static final int ACTION_UPDATE_MAP = 1;
    private static final int ACTION_REGISTER_PROBLEM = 2;

    public static final String EXTRA_LATITUDE = MapActivity.class.getSimpleName() + ".EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = MapActivity.class.getSimpleName() + ".EXTRA_LONGITUDE";
    public static final String EXTRA_ADDRESS = MapActivity.class.getSimpleName() + ".EXTRA_ADDRESS";


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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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

        //Desmarcar o comentário abaixo para ver a área que os marcadores podem ser colocados.
        //googleMap.addPolygon(areaMarcadoresUFRN).setFillColor(Color.BLUE);

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

        //Adicionar marcador quando o usúario clica no mapa
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (PolyUtil.containsLocation(latLng, areaMarcadoresUFRN.getPoints(), true)) {
                    //salvarEmArquivo("Localização do marcador: \n" + "Latitude: "+latLng.latitude+"\nLongitude: "+latLng.longitude+"\n\n");
                    /*
                    googleMap.addMarker(new MarkerOptions().title("TESTE").position(latLng));
                    Intent i = new Intent(MapActivity.this, CriarProActivity.class);
                    i.putExtra("position", latLng);
                    startActivityForResult(i, 1);
                    */
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent i = new Intent(MapActivity.this, InfoActivity.class);
                startActivity(i);
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                double latitude = latLng.latitude;
                double longitude = latLng.longitude;
                String address = "Erro ao selecionar local";

                try {
                    Geocoder geo = new Geocoder(MapActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        address = addresses.get(0).getFeatureName();
                        //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    //e.printStackTrace(); // getFromLocation() may sometimes fail
                }


                Intent intent = new Intent();
                intent.putExtra(EXTRA_LATITUDE, latitude);
                intent.putExtra(EXTRA_LONGITUDE, longitude);
                intent.putExtra(EXTRA_ADDRESS, address);
                setResult(RESULT_OK,intent);
                finish();

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
        //Log.d("GPS - onLocationChanged", "Data: " + tempoUltimoUpdate + " Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());
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

    private void salvarEmArquivo(String texto){
        String filename = "Marcadores";
        String string = texto;
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
            Log.d("Arquivo","SALVO");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                new LatLng(-5.832642, -35.203037),
                new LatLng(-5.836930, -35.197296),
                new LatLng(-5.840077, -35.195152),
                new LatLng(-5.842565, -35.195152),
                new LatLng(-5.844215, -35.196658),
                new LatLng(-5.843530, -35.202476),
                new LatLng(-5.837920, -35.205793),
                new LatLng(-5.838503, -35.210667),
                new LatLng(-5.833147, -35.212351),
                new LatLng(-5.829542, -35.211024)
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == ACTION_UPDATE_MAP){
                //update map
            }
            else if (requestCode == ACTION_REGISTER_PROBLEM) {
                String message = data.getStringExtra(CriarProActivity.EXTRA_MESSAGE_REGISTER);
                if (!message.isEmpty()) {
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMap);
                    Snackbar.make(coordinatorLayout,message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
    }

    private void authenticate() {
        OAuthTokenRequest.getInstance().getTokenCredential(this,"http://apitestes.info.ufrn.br/authz-server","pro-campus-id", "procampus");
    }
}
