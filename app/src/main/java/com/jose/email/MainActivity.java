package com.jose.email;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView foto;
    ImageButton btnEnviar, btnSacar;
    TextView txtMensaje, txtEmail;
    EditText txtNombreFoto;
    String rutaAbsolutaFoto = "";
    final int SACAR_FOTO = 1;
    Uri uriFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        foto = findViewById(R.id.imageFoto);
        txtNombreFoto = findViewById(R.id.textNombreFoto);
        txtEmail = findViewById(R.id.textEmail);
        btnEnviar = findViewById(R.id.buttonEnviarEmail);
        btnSacar = findViewById(R.id.buttonSacarFoto);

        checkCamaraPermission();

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uriFoto!=null) {
                    enviarEmail(uriFoto);
                }
            }
        });

        btnSacar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSacarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intentSacarFoto.resolveActivity(getPackageManager()) != null) {
                    File rutaFoto = null;
                    try {
                        rutaFoto = crearRutaFoto();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (rutaFoto != null) {
                        uriFoto = FileProvider.getUriForFile(MainActivity.this, "com.jose.email.fileprovider", rutaFoto);
                        intentSacarFoto.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto);
                        startActivityForResult(intentSacarFoto, SACAR_FOTO);
                    }
                }
            }
        });
    }


    //metodo para crear la ruta donde se va a guardar la imagen
    private File crearRutaFoto() throws IOException {

        String nombreRutaFoto = txtNombreFoto.getText().toString();
        File directorioFoto = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File rutaFoto = File.createTempFile(
                nombreRutaFoto,
                ".jpg",
                directorioFoto
        );
        rutaAbsolutaFoto = rutaFoto.getAbsolutePath();
        return rutaFoto;
    }
    private void enviarEmail(Uri uriFoto){
        //Instanciamos un Intent del tipo ACTION_SEND
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        //Aqui definimos la tipologia de datos del contenido del Email
        emailIntent.setType("application/jpg");
        // Indicamos con un Array de tipo String las direcciones de correo a las cuales enviar
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{txtEmail.getText().toString()});
        // Aqui definimos un titulo para el Email
        emailIntent.putExtra(android.content.Intent.EXTRA_TITLE, "SOS");
        // Aqui definimos un Asunto para el Email
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Ayuda");
        // Aqui definimos un mensaje
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hola, te envío una foto");
        //Aqui definimo la foto
        emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, uriFoto);
        try {
            //Enviamos el Correo iniciando una nueva Activity con el emailIntent.
            startActivity(Intent.createChooser(emailIntent, "Enviar Correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "No hay ningun cliente de correo instalado.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //Medoto para comprobar los permisos de la camara
    private void checkCamaraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Aviso", "No se tiene permiso para la camara");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, 225);
        } else {
            Log.i("Mensaje", "Se tiene permiso para usar la camara");
        }
    }

    //OnActivityResult
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Si sacamos una foto
        if(requestCode == SACAR_FOTO && resultCode == RESULT_OK){
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriFoto);
                foto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}