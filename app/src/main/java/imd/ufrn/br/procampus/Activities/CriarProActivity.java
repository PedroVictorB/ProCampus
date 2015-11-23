package imd.ufrn.br.procampus.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import imd.ufrn.br.procampus.R;

public class CriarProActivity extends AppCompatActivity {

    private static final String TAG = CriarProActivity.class.getSimpleName();

    private static final int ACTION_REQUEST_CAMERA = 0;
    private static final int ACTION_SELECT_FILE = 1;
    private static final int ACTION_REQUEST_LOCAL = 2;

    public static final String EXTRA_MESSAGE_REGISTER = CriarProActivity.class.getSimpleName() + ".EXTRA_MESSAGE_REGISTER";

    private CoordinatorLayout coordinatorLayout;

    private EditText fieldTitle;
    private EditText fieldCategory;
    private EditText fieldDescription;
    private EditText fieldRegisterLocation;

    private TextInputLayout fieldTitleLayout;
    private TextInputLayout fieldCategoryLayout;
    private TextInputLayout fieldDescriptionLayout;

    private ProgressDialog prgDialog;

    private double latitude;
    private double longitude;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.problemRegisterToolbar);

        toolbar.setTitle("Registrar um problema");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.criarProCoordinatorLayout);

        fieldTitle = (EditText) findViewById(R.id.fieldRegisterTitle);
        fieldCategory = (EditText) findViewById(R.id.fieldRegisterCategory);
        fieldDescription = (EditText) findViewById(R.id.fieldRegisterDescription);
        fieldRegisterLocation = (EditText) findViewById(R.id.fieldRegisterLocation);

        fieldTitleLayout = (TextInputLayout) findViewById(R.id.fieldRegisterTitleLayout);
        fieldCategoryLayout = (TextInputLayout) findViewById(R.id.fieldRegisterCategoryLayout);
        fieldDescriptionLayout = (TextInputLayout) findViewById(R.id.fieldRegisterDescriptionLayout);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Registrand problema...");
    }

    public void onClickHandler(View view) {
        int viewId = view.getId();

        if (viewId == R.id.fabConfirmProblemRegister) {
            this.registerProblem();
        }
        else if (viewId == R.id.btnAddImage) {
            ImageDialogFragment imageDialogFragment = new ImageDialogFragment();
            imageDialogFragment.show(getSupportFragmentManager(), TAG);
        }
        else if (viewId == R.id.btnSelectLocal) {
            getLocation();
        }
    }

    private void registerProblem () {
        //fieldTitleLayout.setError("Título inválido");
        //fieldCategoryLayout.setError("Categoria inválida");
        //fieldDescriptionLayout.setError("Descrição inválida");

        RequestParams params = new RequestParams();
        params.put("title", fieldTitle.getText());
        params.put("category", fieldCategory.getText());
        params.put("description", fieldDescription.getText());
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        invokeWS(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTION_REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ((ImageView) findViewById(R.id.problemRegisterImage)).setImageBitmap(thumbnail);
            } else if (requestCode == ACTION_SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                ((ImageView) findViewById(R.id.problemRegisterImage)).setImageBitmap(bm);
            }
            else if (requestCode == ACTION_REQUEST_LOCAL) {
                latitude = data.getDoubleExtra(MapActivity.EXTRA_LATITUDE, -5.837523);
                longitude = data.getDoubleExtra(MapActivity.EXTRA_LONGITUDE, -35.203309);
                String address = data.getStringExtra(MapActivity.EXTRA_ADDRESS);
                fieldRegisterLocation.setText(address);
            }
        }
    }

    @SuppressLint("ValidFragment")
    public class ImageDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Adicione uma foto");
            builder.setIcon(R.drawable.ic_add_a_photo_black_24dp);

            final CharSequence[] items = {"Tirar uma Foto", "Escolher da Galeria", "Cancelar" };
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        getActivity().startActivityForResult(intent, ACTION_REQUEST_CAMERA);
                    }
                    else if (which == 1) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        getActivity().startActivityForResult(Intent.createChooser(intent, "Escolher Arquivo"), ACTION_SELECT_FILE);
                    }
                    else if (which == 2) {
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            return builder.create();
        }
    }

    public void invokeWS(RequestParams params){
        Log.d("Brunno", "Vai tentar criar o problema");
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getString(R.string.api_url) + "/problem/create", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("Brunno", "Problema criado com sucesso");
                prgDialog.hide();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_MESSAGE_REGISTER, "Problema Registrado com Sucesso");
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("Brunno", "Erro ao criar problema");
                String message;
                // Hide Progress Dialog
                prgDialog.hide();

                // When Http response code is '404'
                if(statusCode == 404){
                    message = "Requested resource not found";
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    message = "Something went wrong at server end";
                }
                // When Http response code other than 404, 500
                else{
                    message = "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]";
                }
                Snackbar.make(coordinatorLayout,message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void getLocation() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, ACTION_REQUEST_LOCAL);
    }
}

