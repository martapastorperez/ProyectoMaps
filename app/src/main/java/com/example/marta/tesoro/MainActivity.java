package com.example.marta.tesoro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    private static final String TAG = "gpslog";
    private LocationManager mLocMgr;
    private TextView textViewGPS, textViewDist;
    private GoogleMap mMap;
    public Intent intento;
    // este es el requestCode
    private final static int codigo = 0;
    private TextView reciboDato;
    // coordenadas del tesoro
    private LatLng tesoro;
    private Location tesoroLoc;
    private double lat = 42.23685840732421, lng = -8.712726831436157;
    private Spinner mMapTypeSelector;
    private int mMapTypes[] = {

            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_NONE,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN
    };
    //Minimo tiempo para updates en Milisegundos
    private static final long MIN_CAMBIO_DISTANCIA_PARA_UPDATES = (long) 20; // 20 metro
    //Minimo tiempo para updates en Milisegundos
    private static final long MIN_TIEMPO_ENTRE_UPDATES = 10000; // 10 sg


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String salutation = "Hola";
        intento = new Intent(MainActivity.this, QuickResponse.class);
        intento.putExtra("salutation", salutation);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // inicio la segunda activity y espero un resultado devuelto
                // identifico la llamada con un codigo, en este caso 0
                startActivityForResult(intento, codigo);
            }
        });





        textViewGPS = (TextView) findViewById(R.id.lat);
        textViewDist = (TextView) findViewById(R.id.dist);
        // intento = new Intent(MainActivity.this,SimpleScannerActivity.class);

        mMapTypeSelector = (Spinner) findViewById(R.id.spinner);
        mMapTypeSelector.setOnItemSelectedListener(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requiere permisos para Android 6.0
            Log.e(TAG, "No se tienen permisos necesarios!, se requieren.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
            return;
        } else {
            Log.i(TAG, "Permisos necesarios OK!.");
            mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locListener, Looper.getMainLooper());
            // mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locListener, Looper.getMainLooper());
        }
        textViewGPS.setText("Lat " + " Long ");

        // creamos objetos para determinar el tesoro
        tesoro = new LatLng(lat, lng);
        tesoroLoc = new Location(LocationManager.GPS_PROVIDER);
        tesoroLoc.setLatitude(lat);
        tesoroLoc.setLongitude(lng);

    }

    public LocationListener locListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Lat " + location.getLatitude() + " Long " + location.getLongitude());

            textViewGPS.setText("Lat " + (float) location.getLatitude() + " Long " + (float) location.getLongitude());
            // textViewGPS.setText("Lat " + location.getLatitude() + " Long " + location.getLongitude());
            // movemos la camara para la nueva posicion
            LatLng nuevaPosicion = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(nuevaPosicion)
                    .zoom(15)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //calculamos la distancia a la marca
            textViewDist.setText(String.valueOf(location.distanceTo(tesoroLoc)));
        }


        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled()");
        }

        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled()");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged()");
        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //engadimos controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Engadimos tesoro
        mMap.addMarker(new MarkerOptions().position(tesoro).title("Marca de Tesoro"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tesoro));
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mMap.setMapType(mMapTypes[position]);
    }


    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Comprobamos si el resultado de la segunda actividad es "RESULT_OK".
        if (resultCode == RESULT_OK) {
            // Comprobamos el codigo de nuestra llamada
            if (requestCode == codigo) {
                // Recojemos el dato que viene en el Intent (se pasa por parámetro con el nombre de data)
                // Rellenamos el TextView
                reciboDato.setText(data.getExtras().getString("retorno"));
            }
        }
    }
}