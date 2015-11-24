package imd.ufrn.br.procampus.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.fragments.ListFragment;
import imd.ufrn.br.procampus.fragments.MapFragment;
import imd.ufrn.br.procampus.utils.OAuthTokenRequest;
import imd.ufrn.br.procampus.utils.RestClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    MapFragment.OnFragmentInteractionListener,
                    ListFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final String MAP_FRAGMENT_TAG = MapFragment.class.getName();
    private final String LIST_FRAGMENT_TAG = ListFragment.class.getName();

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private TextView username;
    private TextView userEmail;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();

        Intent intent = getIntent();

        if (sharedPreferences.getBoolean("user_logged", false)) {
            changeUserInterface();
        }

        if (intent.getBooleanExtra("login_pressed", false)) {
            getUserInformation();
        }

    }

    private void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Mapa dos Problemas");
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        username = (TextView) findViewById(R.id.nav_profile_username);
        userEmail = (TextView) findViewById(R.id.nav_profile_email);

        sharedPreferences = this.getSharedPreferences(MainActivity.class.getCanonicalName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Start MapFragment
        Fragment fragment = new MapFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_main, fragment, MAP_FRAGMENT_TAG).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        configureSearch(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_toggle_view) {
            toggleProblemView(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            authenticate();
        } else if (id == R.id.nav_problem) {
            Intent intent = new Intent(this, UserProblemActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_assault) {

        } else if (id == R.id.nav_manage) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        outState.putBoolean("nav_login", navigationView.getMenu().findItem(R.id.nav_login).isVisible());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        if (!savedInstanceState.getBoolean("nav_login")) {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_problem).setVisible(true);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void authenticate() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("login_pressed", true);
        OAuthTokenRequest.getInstance().getTokenCredential(this, getString(R.string.sigaa_api_base_url) + "authz-server", getString(R.string.sigaa_client_id), getString(R.string.sigaa_client_secret), intent);
    }

    private void getUserInformation() {
        String url = getString(R.string.sigaa_api_base_url) + "usuario-services/services/usuario/info";

        OAuthTokenRequest.getInstance().resourceRequest(this, Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    editor.putBoolean("user_logged", true);
                    editor.putString("nome", jsonObject.getString("nome"));
                    editor.putString("login", jsonObject.getString("login"));
                    editor.commit();

                    verifyProCampusUser();

                    changeUserInterface();
                } catch (JSONException e) {
                    Log.d(TAG, "getUserInformation JSONException - " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getUserInformation VolleyError - " + error.getMessage());
            }
        });
    }

    private void toggleProblemView(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment mapFragment = fragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        Fragment listFragment = fragmentManager.findFragmentByTag(LIST_FRAGMENT_TAG);
        Fragment fragment = new MapFragment();
        String TAG = MAP_FRAGMENT_TAG;

        if (mapFragment != null && mapFragment.isVisible()) {
            fragment = new ListFragment();
            TAG = LIST_FRAGMENT_TAG;
            item.setIcon(R.drawable.ic_map_white_24dp);
            getSupportActionBar().setTitle("Lista dos Problemas");
        }
        else if (listFragment != null && listFragment.isVisible()) {
            fragment = new MapFragment();
            TAG = MAP_FRAGMENT_TAG;
            item.setIcon(R.drawable.ic_format_list_bulleted_white_24dp);
            getSupportActionBar().setTitle("Mapa dos Problemas");
        }

        fragmentManager.beginTransaction().replace(R.id.content_main, fragment, TAG).commit();
    }

    private void configureSearch(Menu menu) {
        SearchView mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setQueryHint("Pesquisar um problema...");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(MainActivity.this, newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void changeUserInterface() {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_login).setVisible(false);
        menu.findItem(R.id.nav_problem).setVisible(true);

        username.setText(sharedPreferences.getString("nome", ""));
        userEmail.setText(sharedPreferences.getString("login", ""));
    }

    private void verifyProCampusUser() {
        String matricula = sharedPreferences.getString("login", "");
        RestClient.get(getString(R.string.api_url) + "/user/matricula/" + matricula, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("id")) {
                        editor.putString("proCampusUserId", response.getString("id"));
                        editor.putBoolean("hasProCampusUser", true);
                        editor.commit();
                    } else {
                        createProCampusUser();
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "verifyProCampusUser JSONException - " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "verifyProCampusUser Request Error");
            }
        });
    }

    private void createProCampusUser () {
        RequestParams params = new RequestParams();
        params.put("name", sharedPreferences.getString("nome", ""));
        params.put("email", sharedPreferences.getString("login", ""));
        params.put("matricula", sharedPreferences.getString("login", ""));

        RestClient.post(getString(R.string.api_url) + "/user/create", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    editor.putString("proCampusUserId", response.getString("id"));
                    editor.putBoolean("hasProCampusUser", true);
                    editor.commit();
                } catch (JSONException e) {
                    Log.d(TAG, "createProCampusUser JSONException - " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "createProCampusUser Request Error");
            }
        });
    }
}
