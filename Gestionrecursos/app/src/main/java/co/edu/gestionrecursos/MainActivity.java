package co.edu.gestionrecursos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 25;
    //1. Declaracion de los objetos de la interfaz que se usaran en la parte logica
    private Button btnCheckPermission;
    private Button btnRequestPermission;
    private TextView tvCamera;
    private TextView tvBiometric;
    private TextView tvExternalWS;
    private TextView tvReadExternalS;
    private TextView tvInternet;
    private TextView tvResponse;

    //1.1 objetos para recursos

    private TextView versionAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBatt;
    private TextView tvLevelBatt;
    IntentFilter batFilter;
    CameraManager CameraManager;
    String cameraId;
    private Button btnOn;
    private Button btnOff;
    ConnectivityManager conexion;
    private TextView tvConexion;

    //Variable para los 5 nuevos permisos
    int REQUEST = 1;

    //Variable para el archivo .txt
    TextView etTexto;
    Button btnGuardarTexto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LinearLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        etTexto = findViewById(R.id.etTexto);
        btnGuardarTexto = findViewById(R.id.btnSavefile);


        //3. Llamado del metodo de enlace de objetos
        initObject();

        //Lamado del metodo de la verificacion de permisos
        verificarPermisos();


        //4. enlace de botones a los metodos
        btnCheckPermission.setOnClickListener(this::voidCheckPermissions);
        btnRequestPermission.setOnClickListener(this::voidRequestPermission);

        //botones para la linterna
        btnOn.setOnClickListener(this::onLigth);
        btnOff.setOnClickListener(this::offLigth);

        //Bateria
        batFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, batFilter);
    }

    //10. Bateria

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBaterry = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            pbLevelBatt.setProgress(levelBaterry);
            tvLevelBatt.setText("Level Baterry" + levelBaterry + " %");
        }
    };

    //8. Implementación de onResume para la version de Android


    @Override
    protected void onResume() {
        super.onResume();
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("Version SO" + versionSO + " / SDK:" + versionSDK);
    }

    //9.encendido y apagado de linterna

    private void onLigth(View view) {
        try {
            CameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = CameraManager.getCameraIdList()[0];
            CameraManager.setTorchMode(cameraId, true);
        } catch (Exception e) {
            Toast.makeText(this, "No se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH", e.getMessage());

        }

    }

    private void offLigth(View view) {
        try {
            CameraManager.setTorchMode(cameraId, false);
        } catch (Exception e) {
            Toast.makeText(this, "No se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH", e.getMessage());

        }

    }

    //5. verificacion de permisos
    private void voidRequestPermission(View view) {
// si hay permiso--> 0 --> No hay permiso --> -1
        int statusCamera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int statusWES = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int statusRES = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int statusInternet = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);
        int statusBiometric = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.USE_BIOMETRIC);

        tvCamera.setText("Status Camera:" + statusCamera);
        tvExternalWS.setText("Status WES:" + statusWES);
        tvReadExternalS.setText("Status RES:" + statusRES);
        tvInternet.setText("Status Internet:" + statusInternet);
        tvBiometric.setText("Status Biometric:" + statusBiometric);
        btnRequestPermission.setEnabled(true);
    }

    //6. Solicitud de permiso de camara
    private void voidCheckPermissions(View view) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);

        }

    }

    //7. Gestion de respuesta del usuario respecto a la solicitud del permiso


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        tvResponse.setText(" " + grantResults[0]);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this).setTitle("Box Permissions").setMessage("You denied the permission Camera").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        finish();
                    }
                }).create().show();

            } else {
                Toast.makeText(this, "Usted no ha otorgado los permisos", Toast.LENGTH_LONG);

            }

        }
    }

    //2. Enlace de objetos
    private void initObject() {
        btnCheckPermission = findViewById(R.id.btnCheckPermission);
        btnRequestPermission = findViewById(R.id.btnRequestPermission);
        btnRequestPermission.setEnabled(false);
        tvCamera = findViewById(R.id.tvCamera);
        tvBiometric = findViewById(R.id.tvDactilar);
        tvExternalWS = findViewById(R.id.tvEws);
        tvReadExternalS = findViewById(R.id.tvRS);
        tvInternet = findViewById(R.id.tvInternet);

        // enlace de los demas objetos
        versionAndroid = findViewById(R.id.tvVersionAndroid);
        pbLevelBatt = findViewById(R.id.pbLevelBattery);
        tvLevelBatt = findViewById(R.id.tvLevelBaterryLB);
        tvConexion = findViewById(R.id.tvConection);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
    }

    //metodo de verificacion permisos
    private void verificarPermisos() {
        int PermissionsSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int PermissionsNFC = ContextCompat.checkSelfPermission(this, Manifest.permission.NFC);
        int PermissionsBT = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int PermissionsCall = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int PermissionsRec = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);


        if (PermissionsSms == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso SMS aceptado", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST);

        }
        if (PermissionsNFC == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso NFC aceptado", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.NFC}, REQUEST);
        }
        if (PermissionsBT == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso BLUETOOTH aceptado", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST);
        }
        if (PermissionsCall == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de llamadas aceptado", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST);
        }
        if (PermissionsRec == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de Grabación de Audio aceptado", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST);
        }
    }
}