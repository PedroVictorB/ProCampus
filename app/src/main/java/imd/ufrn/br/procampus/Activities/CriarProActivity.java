package imd.ufrn.br.procampus.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import imd.ufrn.br.procampus.R;

public class CriarProActivity extends AppCompatActivity {

    private static final String TAG = CriarProActivity.class.getSimpleName();

    private static final int ACTION_REQUEST_CAMERA = 0;
    private static final int ACTION_SELECT_FILE = 1;

    public static final String EXTRA_MESSAGE_REGISTER = CriarProActivity.class.getSimpleName() + ".EXTRA_MESSAGE_REGISTER";

    private EditText fieldTitle;
    private EditText fieldCategory;
    private EditText fieldDescription;

    private TextInputLayout fieldTitleLayout;
    private TextInputLayout fieldCategoryLayout;
    private TextInputLayout fieldDescriptionLayout;

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

        fieldTitle = (EditText) findViewById(R.id.fieldRegisterTitle);
        fieldCategory = (EditText) findViewById(R.id.fieldRegisterCategory);
        fieldDescription = (EditText) findViewById(R.id.fieldRegisterDescription);

        fieldTitleLayout = (TextInputLayout) findViewById(R.id.fieldRegisterTitleLayout);
        fieldCategoryLayout = (TextInputLayout) findViewById(R.id.fieldRegisterCategoryLayout);
        fieldDescriptionLayout = (TextInputLayout) findViewById(R.id.fieldRegisterDescriptionLayout);
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
    }

    private void registerProblem () {
        //fieldTitleLayout.setError("Título inválido");
        //fieldCategoryLayout.setError("Categoria inválida");
        //fieldDescriptionLayout.setError("Descrição inválida");

        Intent intent = new Intent();
        intent.putExtra(EXTRA_MESSAGE_REGISTER, "Problem Registrado com Sucesso");
        setResult(RESULT_OK,intent);
        finish();
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
}

