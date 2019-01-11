package lueorganisation.winmall.via.touristapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import lueorganisation.winmall.via.touristapp.Model.AbsRuntimePermission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AbsRuntimePermission implements View.OnClickListener {
    String TAG="LOCATIONS";
    LocationManager mlocManager;
    AlertDialog.Builder alert;
    String lat = "";
    String lang = "";
    Timer timer1;
    TimerTask timerTask;
    int count = 0;
    Location myLocation;
    public static final int REQUEST_PERMISSION_CODE=1;
    private static final int REQUEST_PERMISSION = 10;
    Context context;
    TextView tvheadertitle;
    ImageView ivheaderleft;
    private static LatLng Source = null;
    private static LatLng Destination = null;
    String otherPlaceLat="25.6203";
    String otherPlaceLang="85.1394";
    String museumPatnaLat="25.6127";
    String museumPatnaLang="85.1332";
    double slat;
    double slong;
    double dlat;
    double dlong;
    ImageButton golgharImage, museumImage;
    LinearLayout linearLoader;
    TextView current_locationText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        checkRunTimePermission();

        requestAppPermissions(new String[]{

                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        //       Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                },

                R.string.ok, REQUEST_PERMISSION);


        tvheadertitle = findViewById(R.id.tvheadertitle);
        tvheadertitle.setText(getString(R.string.app_name));
        ivheaderleft = findViewById(R.id.ivheaderleft);
        ivheaderleft.setVisibility(View.GONE);
        golgharImage = findViewById(R.id.golghar);
        museumImage = findViewById(R.id.museum);
        linearLoader = findViewById(R.id.linearLoader);
        current_locationText = findViewById(R.id.current_location);
        golgharImage.setOnClickListener(this);
        museumImage.setOnClickListener(this);
        getGpsLocation();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    // Permission giving at runtime

    private void checkRunTimePermission() {

        if(checkPermission()){

            Toast.makeText(MainActivity.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();

        }
        else {

            requestPermission();
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        CAMERA,
                        ACCESS_COARSE_LOCATION,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE

                },  REQUEST_PERMISSION_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case  REQUEST_PERMISSION_CODE:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadContactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    boolean ReadPhoneStatePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && ReadContactsPermission ) {

                        //   Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        //   Toast.makeText(MainActivity.this,"Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult == PackageManager.PERMISSION_GRANTED ;
    }



    // Get GPS location

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getGpsLocation() {

        //  locationProgressDialog.show();
        linearLoader.setVisibility(View.VISIBLE);
        alert = new AlertDialog.Builder(this);
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
           /* alert.setTitle("GPS");
            alert.setMessage("GPS is turned OFF...\nDo U Want Turn On GPS...");*/
            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });
            alert.setView(R.layout.gps_message);
            alert.setPositiveButton(R.string.allow_gps,
                    new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                return;
                            }
                            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                                    (float) 0.01, (android.location.LocationListener) listener);
                            setCriteria();

                            mlocManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER, 0, (float)
                                            0.01, (android.location.LocationListener) listener);

                            Intent I = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(I);

                        }
                    });
            alert.show();


        } else {
            try {
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, (float) 0.01, (android.location.LocationListener) listener);
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, (float) 0.01, (android.location.LocationListener) listener);
            }catch (Exception e){}
        }
        count =0;
        startTimer();

    }


    private void startTimer(){
        timer1 = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count++;
                        if(count >20)
                        {
                            if (myLocation==null){
                                //Toast.makeText(getApplicationContext(), "Please check your Internet Connection", Toast.LENGTH_LONG).show();
                                timer1.cancel();
                            }
                        }
                    }
                });
            }
        };

        timer1.schedule(timerTask, 1000, 1000);
    }


    private final android.location.LocationListener listener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            myLocation=location;


            if (location.getLatitude() > 0.0) {
                DecimalFormat df= new DecimalFormat("#.00000000");

                lat=String.valueOf(df.format(location.getLatitude()));
                lang=String.valueOf(df.format(location.getLongitude()));
                getAddress(location.getLatitude(), location.getLongitude());
                linearLoader.setVisibility(View.GONE);
                //    locationProgressDialog.setMessage("lat: "+lat+"\n"+"long:"+lang+"\n"+"accuracy:"+location.getAccuracy());
                if (location.getAccuracy()>0 && location.getAccuracy()<100) {

                    lat=lat.replaceAll(",",".");

                    lang=lang.replaceAll(",",".");
                }
                else
                {


                }
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

    };

    public String setCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String provider = mlocManager.getBestProvider(criteria, true);
        return provider;
    }

    public String getLat() {
        return lat;
    }

    public String getLang() {
        return lang;
    }


    public void proceedMessage(String response) {
        final Dialog dialog = new Dialog(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View subView = inflater.inflate(R.layout.alert_response, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(subView);
        // dialog.setTitle("Title...");
        Button yes = (Button) dialog.findViewById(R.id.ok);
        TextView responseText = (TextView)dialog.findViewById(R.id.text1);

        responseText.setText(response);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.golghar:
                 if (lat!=null && !lat.equals("")) {
                     getDistance(lat, lang, otherPlaceLat, otherPlaceLang);
                 }

                break;

            case R.id.museum:
                if (lat!=null && !lat.equals("")) {
                    getDistance(lat, lang, museumPatnaLat, museumPatnaLang);
                }
                break;
        }
    }

  public void getDistance(String lat, String lang, String otherPlaceLat, String otherPlaceLang){
      slat = Double.parseDouble(lat);
      slong = Double.parseDouble(lang);
      dlat = Double.parseDouble(otherPlaceLat);
      dlong = Double.parseDouble(otherPlaceLang);
      Source = new LatLng(slat, slong);
      Destination = new LatLng(dlat, dlong);
      //  calculateDistance();
      Double distance = distance(dlat, dlong, slat, slong);

      Double dd = distance / 0.62137;

      DecimalFormat dff = new DecimalFormat("#.00");
      // System.out.print(df.format(distance));
      String ddd = String.valueOf("This location is "+dff.format(dd) +" "+ getString(R.string.km_away_from_you));
      proceedMessage(ddd);
  }

    public void getAddress(Double latitude, Double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getSubAdminArea();
            String state = addresses.get(0).getAdminArea();
            String street = addresses.get(0).getSubLocality();
            String address2 = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city2 = addresses.get(0).getLocality();
            String state2 = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            Log.d(TAG, "getAddress:  address" + address2);
            Log.d(TAG, "getAddress:  city" + city2);
            Log.d(TAG, "getAddress:  state" + state2);
            Log.d(TAG, "getAddress:  postalCode" + postalCode);
            Log.d(TAG, "getAddress:  knownName" + knownName);
           if (!knownName.equals("null") && !city2.equals("null")) {
               current_locationText.setText("Now you are at : " + knownName + ", " + city2);
               current_locationText.setVisibility(View.VISIBLE);
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
