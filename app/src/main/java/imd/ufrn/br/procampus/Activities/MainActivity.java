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
import android.widget.Toast;

import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.fragments.ListFragment;
import imd.ufrn.br.procampus.fragments.MapFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    MapFragment.OnFragmentInteractionListener,
                    ListFragment.OnFragmentInteractionListener {

    private final String MAP_FRAGMENT_TAG = MapFragment.class.getName();
    private final String LIST_FRAGMENT_TAG = ListFragment.class.getName();
    private final String TAG = MainActivity.class.getName();

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Brunno", "onCreate");
        setContentView(R.layout.activity_main);
        initComponents();

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if ( !sharedPreferences.getBoolean("nav_login", true) ) {
            Log.d("Brunno", "Entrou aqui");
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_problem).setVisible(true);
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
        Log.d("Brunno", "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);

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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            item.setVisible(false);
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_problem).setVisible(true);
            editor.putBoolean("nav_login", false);
            editor.commit();
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
        Log.d("Brunno", "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d("Brunno", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d("Brunno", "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("Brunno", "onStop");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("Brunno", "onSaveInstanceState");
        outState.putBoolean("nav_login", navigationView.getMenu().findItem(R.id.nav_login).isVisible());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("Brunno", "onRestoreInstanceState");
        if (!savedInstanceState.getBoolean("nav_login")) {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_problem).setVisible(true);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
