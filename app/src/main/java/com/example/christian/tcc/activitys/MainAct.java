package com.example.christian.tcc.activitys;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christian.tcc.R;
import com.example.christian.tcc.config.ConfiguracaoFirebase;
import com.example.christian.tcc.modelo.Usuario;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainAct extends AppCompatActivity{

    private FirebaseAuth mAuth;

    private TextView tvCoordinate;
    private GoogleApiClient mGoogleApiClient;

    private List<Usuario> listUsuarios = new ArrayList<>();

    public static DatabaseReference mRootRef;
    public static DatabaseReference mUserRef;

    Button btnGps;
    TextView txtLatitude, txtLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);


        mRootRef = ConfiguracaoFirebase.getFirebaseDatabase();
        mUserRef = mRootRef.child("usuarios");

        mAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

            Query query = mUserRef.orderByChild("email").equalTo(user.getEmail()).limitToFirst(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.getValue()!=null) {
                        DataSnapshot child = dataSnapshot.getChildren().iterator().next();
                        Usuario usuario = child.getValue(Usuario.class);

                        switch (usuario.getTipoUsuario()) {
                            case "Pessoa com deficiência": {
                                startActivity(new Intent(MainAct.this, PdiMainActivity.class));
                                break;
                            }

                            default: {
                                Toast.makeText(getApplicationContext(), "Bem vindo de volta " + usuario.getEmail() + "!", Toast.LENGTH_LONG).show();
                            }

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Se ocorrer um erro
                }
            });

        } else {
            Intent intent = new Intent(this, LoginAct.class);
            startActivity(intent);
            finish();
        }


        txtLatitude= (TextView) findViewById(R.id.tv_exibe_latitude);
        txtLongitude = (TextView) findViewById(R.id.tv_exibe_longidute);

        btnGps = (Button) findViewById(R.id.btn_onde_estou);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pedirPermissoes();

        //Usuarios de teste ************

        //writeNewUser(2,"a");
        //writeNewUser(3,"b");


        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("usuarios");

        refUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario;

                for (DataSnapshot usuarioSnapshot: dataSnapshot.getChildren()) {
                    usuario = usuarioSnapshot.getValue(Usuario.class);
                    listUsuarios.add(usuario);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w( "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });


    }

    private void pedirPermissoes() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
            configurarServico();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configurarServico();
                } else {
                    Toast.makeText(this, "Não vai funcionar!!!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void configurarServico(){
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    atualizar(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) { }

                public void onProviderEnabled(String provider) { }

                public void onProviderDisabled(String provider) { }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }catch(SecurityException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void atualizar(Location location) {
        Double latPoint = location.getLatitude();
        Double lngPoint = location.getLongitude();

        txtLatitude.setText(latPoint.toString());
        txtLongitude.setText(lngPoint.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginAct.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

