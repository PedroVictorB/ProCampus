package imd.ufrn.br.procampus.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.adapters.UserProblemAdapter;
import imd.ufrn.br.procampus.entities.Problem;
import imd.ufrn.br.procampus.entities.User;
import imd.ufrn.br.procampus.utils.RestClient;

public class UserProblemActivity extends AppCompatActivity {

    private static final String TAG = UserProblemActivity.class.getSimpleName();

    private static final int ACTION_REGISTER_PROBLEM = 1;

    private RecyclerView recyclerView;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_user_problem);
        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        loadUserProblems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Minhas Postagens");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.userProblemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        sharedPreferences = this.getSharedPreferences(MainActivity.class.getCanonicalName(), Context.MODE_PRIVATE);
    }

    public void onClickHandler(View view) {
        int id = view.getId();

        if (id == R.id.fab_add_problem) {
            Intent intent = new Intent(this, CriarProActivity.class);
            startActivityForResult(intent, ACTION_REGISTER_PROBLEM);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if (requestCode == ACTION_REGISTER_PROBLEM) {
                String message = data.getStringExtra(CriarProActivity.EXTRA_MESSAGE_REGISTER);
                if (!message.isEmpty()) {
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.userProblemCoordinatorLayout);
                    Snackbar.make(coordinatorLayout,message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
    }

    private void loadUserProblems () {
        String userId = sharedPreferences.getString("proCampusUserId", "");
        RestClient.get(getString(R.string.api_url) + "/problem/user/" + userId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Problem> mDataset = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("problems");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProblem = jsonArray.getJSONObject(i);

                        Problem problem = new Problem();
                        problem.setTitle(jsonProblem.getString("title"));

                        String data = jsonProblem.getString("date");
                        SimpleDateFormat  formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        problem.setPostDate(new Date(formatter.parse(data).getTime()));

                        problem.setDescription(jsonProblem.getString("description"));

                        mDataset.add(problem);
                    }
                    recyclerView.setAdapter(new UserProblemAdapter(mDataset));
                } catch (JSONException e) {
                    Log.d(TAG, "loadUserProblems JSONException - " + e.getMessage());
                } catch (ParseException e) {
                    Log.d(TAG, "loadUserProblems ParseException - " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "loadUserProblems Request Error");
            }
        });
    }
}