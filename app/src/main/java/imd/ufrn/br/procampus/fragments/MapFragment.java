package imd.ufrn.br.procampus.fragments;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.utils.RestClient;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MapView mapView;

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int ACTION_UPDATE_MAP = 1;
    private static final int ACTION_REGISTER_PROBLEM = 2;


    //Latitude e longitude do centro da UFRN.
    private static final LatLng latLngUFRN = new LatLng(-5.837523, -35.203309);

    //Área para posição da camera.
    private static final LatLngBounds latLngBoundsUFRN = new LatLngBounds(new LatLng(-5.844020, -35.214237), new LatLng(-5.829642, -35.193234));

    //Poligono para área permitida dos marcadores
    private static final PolygonOptions areaMarcadoresUFRN = MapFragment.polygonLatLngUFRN();

    //Instância do mapa.
    private GoogleMap googleMap;

    //Usado para, no OnCreate, o OnMapReadyCallback  ser chamado.
    private com.google.android.gms.maps.MapFragment mapFragment;

    //Localização Google API
    private GoogleApiClient mGoogleApiClient;

    //Variáveis para o GPS
    private LocationRequest locationRequest;
    private Location minhaLocalizacao;
    private String tempoUltimoUpdate;
    private boolean pedindoLocalizacao = false;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        buildGoogleApiClient();
        createLocationRequest();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (mGoogleApiClient.isConnected() && !pedindoLocalizacao) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

        loadMarkers();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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

    /**
     * Constroi a conexão com o google API para uso do GPS.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

    private void loadMarkers() {
        RestClient.get(getString(R.string.api_url) + "problem/readAll", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("problems");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProblem = jsonArray.getJSONObject(i);
                        String latitude = jsonProblem.getString("latitude");
                        String longitude = jsonProblem.getString("longitude");
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                .title(jsonProblem.getString("title")));
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "loadUserProblems JSONException - " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "loadUserProblems Request Error (http " + statusCode + ")");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "loadUserProblems Request Error (http " + statusCode + "): " + responseString);
            }
        });
    }
}
