package imd.ufrn.br.procampus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.msebera.android.httpclient.Header;
import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.entities.Problem;
import imd.ufrn.br.procampus.entities.User;
import imd.ufrn.br.procampus.utils.RestClient;

public class ProblemDetailsActivity extends AppCompatActivity {

    private static final String TAG = ProblemDetailsActivity.class.getSimpleName();

    private View viewGroup;

    private TextView viewUsername;
    private TextView viewTitle;
    private TextView viewPostDate;
    private TextView viewDescription;
    private TextView viewNumberOfComments;
    private ImageView viewImage;

    private ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_details);

        initComponents();

        Intent intent = getIntent();

        loadProblemDetails(intent);
    }

    private void initComponents() {

        viewGroup = findViewById(R.id.problemDetailsViewGroup);

        viewUsername = (TextView) findViewById(R.id.detailsUsername);
        viewTitle = (TextView) findViewById(R.id.detailsProblemTitle);
        viewPostDate = (TextView) findViewById(R.id.detailsPostDate);
        viewDescription = (TextView) findViewById(R.id.detailsProblemDescription);
        viewNumberOfComments = (TextView) findViewById(R.id.detailsNumberOfComments);
        viewImage = (ImageView) findViewById(R.id.detailsProblemImage);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Carregando...");
    }

    private void loadProblemDetails(final Intent intent) {
        prgDialog.show();

        int problemId = intent.getIntExtra("problemId", -1);
        RestClient.get(getString(R.string.api_url) + "/problem/read/" + problemId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    viewUsername.setText(intent.getStringExtra("user"));
                    viewTitle.setText(intent.getStringExtra("problemTitle"));

                    /*
                    String data = response.getString("date");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    java.sql.Date date = new Date(formatter.parse(data).getTime());
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String finalDate = dateFormat.format(date);
                    */
                    viewPostDate.setText(intent.getStringExtra("problemPostDate"));

                    viewDescription.setText(intent.getStringExtra("problemDescription"));
                    viewNumberOfComments.setText("0");

                    String image64 = response.getString("image");
                    byte[] imageAsBytes = Base64.decode(image64.getBytes(), Base64.DEFAULT);
                    viewImage.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                    viewGroup.setVisibility(View.VISIBLE);
                    prgDialog.hide();
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

    private void loadComments() {

    }
}
