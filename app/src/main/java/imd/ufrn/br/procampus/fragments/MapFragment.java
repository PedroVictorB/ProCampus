package imd.ufrn.br.procampus.fragments;

import android.app.Activity;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
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

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLngUFRN));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngUFRN, 17));//Posição inicial da camera na UFRN com zoom 17.

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

        mapMask();

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
        RestClient.get(getString(R.string.api_url) + "problem/readAllNoImg", null, new JsonHttpResponseHandler() {
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

    private void mapMask() {
        final ArrayList<LatLng> vertices = new ArrayList<LatLng>();
        vertices.add(0, new LatLng(-7.832417, -36.758935));
        vertices.add(1, new LatLng(-4.725264, -36.741253));
        vertices.add(2, new LatLng(-4.746049, -33.744411));
        vertices.add(3, new LatLng(-7.868978, -33.728403));

        final ArrayList<LatLng> verticesHole = new ArrayList<LatLng>();

        verticesHole.add(0, new LatLng(-5.843603, -35.202717));
        verticesHole.add(0, new LatLng(-5.843696, -35.202474));
        verticesHole.add(0, new LatLng(-5.843788, -35.202177));
        verticesHole.add(0, new LatLng(-5.843785, -35.201951));
        verticesHole.add(0, new LatLng(-5.843782, -35.201767));
        verticesHole.add(0, new LatLng(-5.843812, -35.201511));
        verticesHole.add(0, new LatLng(-5.843850, -35.201319));
        verticesHole.add(0, new LatLng(-5.843854, -35.201064));
        verticesHole.add(0, new LatLng(-5.843868, -35.200764));
        verticesHole.add(0, new LatLng(-5.843882, -35.200520));
        verticesHole.add(0, new LatLng(-5.843904, -35.200329));
        verticesHole.add(0, new LatLng(-5.843918, -35.200207));
        verticesHole.add(0, new LatLng(-5.843955, -35.200057));
        verticesHole.add(0, new LatLng(-5.843986, -35.199825));
        verticesHole.add(0, new LatLng(-5.844042, -35.199597));
        verticesHole.add(0, new LatLng(-5.844063, -35.199429));
        verticesHole.add(0, new LatLng(-5.844076, -35.199223));
        verticesHole.add(0, new LatLng(-5.844102, -35.198940));
        verticesHole.add(0, new LatLng(-5.844163, -35.198200));
        verticesHole.add(0, new LatLng(-5.844257, -35.197470));
        verticesHole.add(0, new LatLng(-5.844282, -35.197127));
        verticesHole.add(0, new LatLng(-5.844243, -35.196741));
        verticesHole.add(0, new LatLng(-5.844273, -35.196556));
        verticesHole.add(0, new LatLng(-5.844246, -35.196395));
        verticesHole.add(0, new LatLng(-5.844205, -35.196259));
        verticesHole.add(0, new LatLng(-5.844111, -35.196110));
        verticesHole.add(0, new LatLng(-5.844055, -35.195972));
        verticesHole.add(0, new LatLng(-5.843923, -35.195775));
        verticesHole.add(0, new LatLng(-5.843862, -35.195652));
        verticesHole.add(0, new LatLng(-5.843763, -35.195496));
        verticesHole.add(0, new LatLng(-5.843639, -35.195391));
        verticesHole.add(0, new LatLng(-5.843562, -35.195320));
        verticesHole.add(0, new LatLng(-5.843432, -35.195222));
        verticesHole.add(0, new LatLng(-5.843248, -35.195118));
        verticesHole.add(0, new LatLng(-5.843133, -35.195052));
        verticesHole.add(0, new LatLng(-5.842973, -35.194949));
        verticesHole.add(0, new LatLng(-5.842730, -35.194887));
        verticesHole.add(0, new LatLng(-5.842530, -35.194835));
        verticesHole.add(0, new LatLng(-5.842397, -35.194804));
        verticesHole.add(0, new LatLng(-5.842196, -35.194754));
        verticesHole.add(0, new LatLng(-5.842084, -35.194718));
        verticesHole.add(0, new LatLng(-5.841919, -35.194682));
        verticesHole.add(0, new LatLng(-5.841725, -35.194689));
        verticesHole.add(0, new LatLng(-5.841510, -35.194675));
        verticesHole.add(0, new LatLng(-5.841384, -35.194663));
        verticesHole.add(0, new LatLng(-5.841228, -35.194646));
        verticesHole.add(0, new LatLng(-5.841015, -35.194660));
        verticesHole.add(0, new LatLng(-5.840741, -35.194674));
        verticesHole.add(0, new LatLng(-5.840531, -35.194705));
        verticesHole.add(0, new LatLng(-5.840408, -35.194725));
        verticesHole.add(0, new LatLng(-5.840232, -35.194746));
        verticesHole.add(0, new LatLng(-5.840023, -35.194843));
        verticesHole.add(0, new LatLng(-5.839915, -35.194903));
        verticesHole.add(0, new LatLng(-5.839739, -35.194989));
        verticesHole.add(0, new LatLng(-5.839548, -35.195122));
        verticesHole.add(0, new LatLng(-5.839410, -35.195223));
        verticesHole.add(0, new LatLng(-5.839224, -35.195361));
        verticesHole.add(0, new LatLng(-5.839058, -35.195450));
        verticesHole.add(0, new LatLng(-5.838779, -35.195593));
        verticesHole.add(0, new LatLng(-5.838616, -35.195736));
        verticesHole.add(0, new LatLng(-5.838377, -35.195933));
        verticesHole.add(0, new LatLng(-5.838188, -35.196054));
        verticesHole.add(0, new LatLng(-5.838001, -35.196205));
        verticesHole.add(0, new LatLng(-5.837836, -35.196287));
        verticesHole.add(0, new LatLng(-5.837695, -35.196394));
        verticesHole.add(0, new LatLng(-5.837505, -35.196518));
        verticesHole.add(0, new LatLng(-5.837263, -35.196706));
        verticesHole.add(0, new LatLng(-5.837067, -35.196878));
        verticesHole.add(0, new LatLng(-5.836886, -35.196996));
        verticesHole.add(0, new LatLng(-5.836675, -35.197146));
        verticesHole.add(0, new LatLng(-5.836449, -35.197385));
        verticesHole.add(0, new LatLng(-5.836192, -35.197672));
        verticesHole.add(0, new LatLng(-5.835998, -35.197855));
        verticesHole.add(0, new LatLng(-5.835780, -35.198113));
        verticesHole.add(0, new LatLng(-5.835607, -35.198362));
        verticesHole.add(0, new LatLng(-5.835487, -35.198525));
        verticesHole.add(0, new LatLng(-5.835314, -35.198735));
        verticesHole.add(0, new LatLng(-5.835084, -35.199052));
        verticesHole.add(0, new LatLng(-5.834885, -35.199310));
        verticesHole.add(0, new LatLng(-5.834761, -35.199495));
        verticesHole.add(0, new LatLng(-5.834570, -35.199740));
        verticesHole.add(0, new LatLng(-5.834419, -35.199922));
        verticesHole.add(0, new LatLng(-5.834201, -35.200180));
        verticesHole.add(0, new LatLng(-5.834055, -35.200360));
        verticesHole.add(0, new LatLng(-5.833870, -35.200594));
        verticesHole.add(0, new LatLng(-5.833745, -35.200801));
        verticesHole.add(0, new LatLng(-5.833582, -35.201072));
        verticesHole.add(0, new LatLng(-5.833457, -35.201268));
        verticesHole.add(0, new LatLng(-5.833272, -35.201524));
        verticesHole.add(0, new LatLng(-5.833116, -35.201714));
        verticesHole.add(0, new LatLng(-5.832930, -35.201943));
        verticesHole.add(0, new LatLng(-5.832757, -35.202150));
        verticesHole.add(0, new LatLng(-5.832546, -35.202415));
        verticesHole.add(0, new LatLng(-5.832438, -35.202646));
        verticesHole.add(0, new LatLng(-5.832368, -35.202780));
        verticesHole.add(0, new LatLng(-5.832295, -35.202963));
        verticesHole.add(0, new LatLng(-5.832191, -35.203210));
        verticesHole.add(0, new LatLng(-5.832116, -35.203381));
        verticesHole.add(0, new LatLng(-5.832003, -35.203660));
        verticesHole.add(0, new LatLng(-5.831913, -35.203848));
        verticesHole.add(0, new LatLng(-5.831793, -35.204090));
        verticesHole.add(0, new LatLng(-5.831702, -35.204250));
        verticesHole.add(0, new LatLng(-5.831608, -35.204468));
        verticesHole.add(0, new LatLng(-5.831545, -35.204640));
        verticesHole.add(0, new LatLng(-5.831470, -35.204862));
        verticesHole.add(0, new LatLng(-5.831390, -35.205055));
        verticesHole.add(0, new LatLng(-5.831287, -35.205313));
        verticesHole.add(0, new LatLng(-5.831233, -35.205785));
        verticesHole.add(0, new LatLng(-5.831640, -35.205864));
        verticesHole.add(0, new LatLng(-5.832047, -35.205943));
        verticesHole.add(0, new LatLng(-5.832464, -35.205979));
        verticesHole.add(0, new LatLng(-5.832882, -35.205908));
        verticesHole.add(0, new LatLng(-5.833232, -35.205830));
        verticesHole.add(0, new LatLng(-5.833454, -35.205687));
        verticesHole.add(0, new LatLng(-5.833804, -35.205544));
        verticesHole.add(0, new LatLng(-5.834048, -35.205230));
        verticesHole.add(0, new LatLng(-5.834331, -35.205297));
        verticesHole.add(0, new LatLng(-5.834538, -35.205526));
        verticesHole.add(0, new LatLng(-5.834920, -35.205634));
        verticesHole.add(0, new LatLng(-5.835166, -35.205581));
        verticesHole.add(0, new LatLng(-5.835389, -35.205659));
        verticesHole.add(0, new LatLng(-5.835687, -35.205769));
        verticesHole.add(0, new LatLng(-5.835888, -35.205818));
        verticesHole.add(0, new LatLng(-5.836135, -35.205867));
        verticesHole.add(0, new LatLng(-5.836334, -35.206164));
        verticesHole.add(0, new LatLng(-5.836246, -35.206606));
        verticesHole.add(0, new LatLng(-5.836095, -35.207004));
        verticesHole.add(0, new LatLng(-5.835965, -35.207414));
        verticesHole.add(0, new LatLng(-5.835834, -35.207995));
        verticesHole.add(0, new LatLng(-5.835638, -35.208782));
        verticesHole.add(0, new LatLng(-5.835433, -35.209218));
        verticesHole.add(0, new LatLng(-5.835357, -35.209826));
        verticesHole.add(0, new LatLng(-5.834878, -35.209750));
        verticesHole.add(0, new LatLng(-5.834315, -35.209652));
        verticesHole.add(0, new LatLng(-5.834065, -35.210210));
        verticesHole.add(0, new LatLng(-5.833879, -35.210789));
        verticesHole.add(0, new LatLng(-5.833728, -35.211185));
        verticesHole.add(0, new LatLng(-5.833619, -35.211475));
        verticesHole.add(0, new LatLng(-5.833489, -35.211796));
        verticesHole.add(0, new LatLng(-5.833465, -35.212118));
        verticesHole.add(0, new LatLng(-5.834249, -35.211974));
        verticesHole.add(0, new LatLng(-5.834799, -35.211916));
        verticesHole.add(0, new LatLng(-5.835391, -35.211793));
        verticesHole.add(0, new LatLng(-5.835962, -35.211606));
        verticesHole.add(0, new LatLng(-5.836548, -35.211428));
        verticesHole.add(0, new LatLng(-5.837112, -35.211308));
        verticesHole.add(0, new LatLng(-5.837677, -35.211102));
        verticesHole.add(0, new LatLng(-5.838188, -35.210843));
        verticesHole.add(0, new LatLng(-5.838550, -35.210670));
        verticesHole.add(0, new LatLng(-5.838699, -35.210369));
        verticesHole.add(0, new LatLng(-5.838646, -35.209759));
        verticesHole.add(0, new LatLng(-5.838571, -35.209085));
        verticesHole.add(0, new LatLng(-5.838444, -35.208156));
        verticesHole.add(0, new LatLng(-5.838316, -35.207348));
        verticesHole.add(0, new LatLng(-5.838231, -35.206873));
        verticesHole.add(0, new LatLng(-5.838113, -35.206280));
        verticesHole.add(0, new LatLng(-5.838060, -35.205901));
        verticesHole.add(0, new LatLng(-5.838595, -35.205641));
        verticesHole.add(0, new LatLng(-5.838926, -35.205414));
        verticesHole.add(0, new LatLng(-5.839176, -35.205300));
        verticesHole.add(0, new LatLng(-5.839381, -35.205224));
        verticesHole.add(0, new LatLng(-5.839571, -35.205073));
        verticesHole.add(0, new LatLng(-5.839707, -35.204970));
        verticesHole.add(0, new LatLng(-5.839971, -35.204803));
        verticesHole.add(0, new LatLng(-5.840182, -35.204663));
        verticesHole.add(0, new LatLng(-5.840455, -35.204509));
        verticesHole.add(0, new LatLng(-5.840668, -35.204388));
        verticesHole.add(0, new LatLng(-5.840908, -35.204282));
        verticesHole.add(0, new LatLng(-5.841132, -35.204177));
        verticesHole.add(0, new LatLng(-5.841418, -35.204026));
        verticesHole.add(0, new LatLng(-5.841628, -35.203902));
        verticesHole.add(0, new LatLng(-5.841910, -35.203757));
        verticesHole.add(0, new LatLng(-5.842162, -35.203622));
        verticesHole.add(0, new LatLng(-5.842454, -35.203468));
        verticesHole.add(0, new LatLng(-5.842685, -35.203342));
        verticesHole.add(0, new LatLng(-5.842891, -35.203228));
        verticesHole.add(0, new LatLng(-5.843165, -35.203104));
        verticesHole.add(0, new LatLng(-5.843301, -35.202972));
        verticesHole.add(0, new LatLng(-5.843407, -35.202884));

        Polygon polygon = googleMap.addPolygon(new PolygonOptions()
                .add(vertices.get(0), vertices.get(1), vertices.get(2), vertices.get(3))
                .addHole(verticesHole)
                .strokeColor(Color.RED)
                .fillColor(Color.GRAY));
    }
}
