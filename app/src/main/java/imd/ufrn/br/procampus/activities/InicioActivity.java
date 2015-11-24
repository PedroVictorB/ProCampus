package imd.ufrn.br.procampus.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import imd.ufrn.br.procampus.R;

public class InicioActivity extends AppCompatActivity {

    Button botaoMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        botaoMapa = (Button) findViewById(R.id.botaoMapa);

        botaoMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InicioActivity.this, MapActivity.class);
                startActivity(i);
            }
        });
    }
}
