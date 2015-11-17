package imd.ufrn.br.procampus.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import imd.ufrn.br.procampus.R;

public class CriarProActivity extends AppCompatActivity {

    private EditText fieldTitle;
    private EditText fieldCategory;
    private EditText fieldDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_pro);

        this.initComponents();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("Registrar um problema");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fieldTitle = (EditText) findViewById(R.id.fieldTitle);
        fieldCategory = (EditText) findViewById(R.id.fieldCategory);
        fieldDescription = (EditText) findViewById(R.id.fieldDescription);
    }

    public void onClickHandler(View view) {
        int viewId = view.getId();

        if (viewId == R.id.fab_confirm) {
            Snackbar.make(view, "Erro ao criar problema", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            this.registerProblem();
        }
    }

    private void registerProblem () {
        fieldTitle.setError("Invalid Name");
        fieldCategory.setError("Invalid Category");
        fieldDescription.setError("Inválid Description");
    }
}

