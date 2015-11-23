package imd.ufrn.br.procampus.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.sql.Date;
import java.util.ArrayList;

import imd.ufrn.br.procampus.R;
import imd.ufrn.br.procampus.adapters.UserProblemAdapter;
import imd.ufrn.br.procampus.entities.Problem;
import imd.ufrn.br.procampus.entities.User;

public class UserProblemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_problem);
        initComponents();
    }

    private void initComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Minhas Postagens");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.userProblemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ArrayList<Problem> mDataset = new ArrayList<>();
        for (int i = 0; i < 5; i++) {

            Problem problem = new Problem();
            problem.setTitle("Poste apagado na parada de C&T");

            long time = System.currentTimeMillis();
            problem.setPostDate(new Date(time));

            problem.setDescription("Um poste apagado está inibindo a permanencia de alunos no local");

            mDataset.add(problem);
        }

        recyclerView.setAdapter(new UserProblemAdapter(mDataset));
    }

}
