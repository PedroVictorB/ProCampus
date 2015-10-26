package imd.ufrn.br.procampus.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import imd.ufrn.br.procampus.R;

public class LoginActivity extends AppCompatActivity {

    Button botMapa;
    Button botLogin;
    EditText login;
    EditText senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        botMapa = (Button) findViewById(R.id.mapBtn);
        botLogin = (Button) findViewById(R.id.loginBtn);
        login = (EditText) findViewById(R.id.usernameText);
        senha = (EditText) findViewById(R.id.passwordText);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        login.setText(prefs.getString("login",""));
        senha.setText(prefs.getString("senha",""));

        botMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(i);
            }
        });

        botLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

                editor.putString("login", login.getText().toString());
                editor.putString("senha", senha.getText().toString());
                editor.apply();

                Intent i = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(i);
            }
        });

    }
}
