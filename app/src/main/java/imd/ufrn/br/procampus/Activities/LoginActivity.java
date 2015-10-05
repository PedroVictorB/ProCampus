package imd.ufrn.br.procampus.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import imd.ufrn.br.procampus.R;

public class LoginActivity extends AppCompatActivity {

    Button botMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        botMapa = (Button) findViewById(R.id.mapBtn);

        botMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(i);
            }
        });

    }
}
