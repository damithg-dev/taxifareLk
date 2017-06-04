package com.damithamarasinghe.taxifarelk;

import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import android.app.FragmentTransaction;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;





//((EditText)placeAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(10.0f);
public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener   {


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location startLocation;
    Location endLocation;
    Marker startLocationMarker;
    Marker endLocationMarker;
    LocationRequest mLocationRequest;
    PlaceAutocompleteFragment startfragment;
    PlaceAutocompleteFragment endfragment;
    private ArrayList<LatLng> mLatLngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.





        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);


        //PlaceAutocompleteFragment
        startfragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.start_fragment);
        startfragment.setHint("Search Hail Location");



        //PlaceAutocompleteFragment
        endfragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.end_fragment);
        endfragment.setHint("Search Destination");

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY).setCountry("LK")
                .build();

        startfragment.setFilter(typeFilter);
        endfragment.setFilter(typeFilter);





        startfragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {

                // TODO: Get info about the selected place.
                Log.d("Place: ",""+ place.getName());
                Location location = new Location("selectedplace");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                onStartLocationChanged(location);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d("An error occurred: ",""+ status);
            }
        });

        endfragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            //polyline.setMap(null)
            @Override
            public void onPlaceSelected(Place place) {

                // TODO: Get info about the selected place.
                Log.d("Place: ",""+ place.getName());
                Location location = new Location("selectedplace");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                onEndLocationChanged(location);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d("An error occurred: ",""+ status);
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();

                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        startLocation = location;
        if (startLocationMarker != null) {
            startLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            double lat = location.getLatitude();
            double longi = location.getLongitude();

            List<Address> addresses = geocoder.getFromLocation(lat, longi, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                //Log.i("jaya wewa " ,""+addresses.toString());
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");

                String Addres = ""+returnedAddress.getAddressLine(0) +"," +returnedAddress.getAddressLine(1);
               // Log.i("jaya wewa " ,Addres);
                markerOptions.title(Addres.toString());
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                startLocationMarker = mMap.addMarker(markerOptions);
                startfragment.setText(Addres);


                //Toast.makeText(this,strReturnedAddress.toString(), Toast.LENGTH_LONG).show();

                //.setText(strReturnedAddress.toString());

            }
            else{
                // myAddress.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // myAddress.setText("Canont get Address!");
        }



        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }


    public void onStartLocationChanged(Location location) {

        startLocation = location;
        if (startLocationMarker != null) {
            startLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            double lat = location.getLatitude();
            double longi = location.getLongitude();

            List<Address> addresses = geocoder.getFromLocation(lat, longi, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                //Log.i("jaya wewa " ,""+addresses.toString());
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");

                String Addres = ""+returnedAddress.getAddressLine(0) +"," +returnedAddress.getAddressLine(1);
                // Log.i("jaya wewa " ,Addres);
                markerOptions.title(Addres.toString());
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                startLocationMarker = mMap.addMarker(markerOptions);
                startfragment.setText(Addres);


                //Toast.makeText(this,strReturnedAddress.toString(), Toast.LENGTH_LONG).show();

                //.setText(strReturnedAddress.toString());

            }
            else{
                // myAddress.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // myAddress.setText("Canont get Address!");
        }



        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    public void onEndLocationChanged(Location location) {

        endLocation = location;
        if (endLocationMarker != null) {
            endLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            double lat = location.getLatitude();
            double longi = location.getLongitude();

            List<Address> addresses = geocoder.getFromLocation(lat, longi, 1);

            if(addresses != null) {
                Address returnedAddress = addresses.get(0);
                //Log.i("jaya wewa " ,""+addresses.toString());
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");

                String Addres = ""+returnedAddress.getAddressLine(0) +"," +returnedAddress.getAddressLine(1);
                // Log.i("jaya wewa " ,Addres);
                markerOptions.title(Addres.toString());
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                endLocationMarker = mMap.addMarker(markerOptions);
                endfragment.setText(Addres);


                //Toast.makeText(this,strReturnedAddress.toString(), Toast.LENGTH_LONG).show();

                //.setText(strReturnedAddress.toString());

            }
            else{
                // myAddress.setText("No Address returned!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // myAddress.setText("Canont get Address!");
        }


        LatLng origin = startLocationMarker.getPosition();
        LatLng dest = endLocationMarker.getPosition();

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }



    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //add multipathes
        String pathes = "alternatives=true";

        // Sensor enabled
        String sensor = "sensor=false";
        //avoid highways
        String avoidHighways = "avoidHighways=false";
        //avoid trolls
        String trolls = "avoidTolls=true";
        //mode of driection
        String mode = "travelMode=DRIVING";


        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor+ "&" + pathes+ "&" + avoidHighways+ "&" + trolls + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            ArrayList<ArrayList> listOfPoints = null;
            listOfPoints = new ArrayList<>();

            PolylineOptions lineOptions1 = null;
            PolylineOptions lineOptions2 = null;
            PolylineOptions lineOptions3 = null;
            PolylineOptions lineOptions4 = null;

            Log.e("elakiri",""+result.size());

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {

                points = new ArrayList<>();
                lineOptions1 = new PolylineOptions();
                lineOptions2 = new PolylineOptions();
                lineOptions3 = new  PolylineOptions();
                lineOptions4 = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                listOfPoints.add(points);
            }
            Log.e("elakiri",""+listOfPoints.size());

            if (listOfPoints.size() != 0){

                if (listOfPoints.size() == 1){
                    lineOptions1.addAll(listOfPoints.get(0));
                    lineOptions1.width(10);
                    lineOptions1.color(Color.RED);
                    double distance = SphericalUtil.computeLength(listOfPoints.get(0))/1000.0;
                    Log.i("elakkiri 1" ,""+distance+"Km");
                    mMap.addPolyline(lineOptions1);
                }else if (listOfPoints.size() == 2){
                    lineOptions1.addAll(listOfPoints.get(0));
                    lineOptions1.width(10);
                    lineOptions1.color(Color.RED);
                    double distance1 = SphericalUtil.computeLength(listOfPoints.get(0))/1000.0;
                    Log.i("elakkiri 1" ,""+distance1+"Km");
                    mMap.addPolyline(lineOptions1);

                    lineOptions2.addAll(listOfPoints.get(1));
                    lineOptions2.width(10);
                    lineOptions2.color(Color.GRAY);
                    double distance2 = SphericalUtil.computeLength(listOfPoints.get(1))/1000.0;
                    Log.i("elakkiri 1" ,""+distance2+"Km");
                    mMap.addPolyline(lineOptions2);
                }else if (listOfPoints.size() == 3){
                    lineOptions1.addAll(listOfPoints.get(0));
                    lineOptions1.width(10);
                    lineOptions1.color(Color.RED);
                    double distance1 = SphericalUtil.computeLength(listOfPoints.get(0))/1000.0;
                    Log.i("elakkiri 1" ,""+distance1+"Km");
                    mMap.addPolyline(lineOptions1);

                    lineOptions2.addAll(listOfPoints.get(1));
                    lineOptions2.width(10);
                    lineOptions2.color(Color.GRAY);
                    double distance2 = SphericalUtil.computeLength(listOfPoints.get(1))/1000.0;
                    Log.i("elakkiri 1" ,""+distance2+"Km");
                    mMap.addPolyline(lineOptions2);

                    lineOptions3.addAll(listOfPoints.get(2));
                    lineOptions3.width(10);
                    lineOptions3.color(Color.GRAY);
                    double distance3 = SphericalUtil.computeLength(listOfPoints.get(2))/1000.0;
                    Log.i("elakkiri 1" ,""+distance3+"Km");
                    mMap.addPolyline(lineOptions3);
                }else if (listOfPoints.size() == 4){
                    lineOptions1.addAll(listOfPoints.get(0));
                    lineOptions1.width(10);
                    lineOptions1.color(Color.RED);
                    double distance1 = SphericalUtil.computeLength(listOfPoints.get(0))/1000.0;
                    Log.i("elakkiri 1" ,""+distance1+"Km");
                    mMap.addPolyline(lineOptions1);

                    lineOptions2.addAll(listOfPoints.get(1));
                    lineOptions2.width(10);
                    lineOptions2.color(Color.GRAY);
                    double distance2 = SphericalUtil.computeLength(listOfPoints.get(1))/1000.0;
                    Log.i("elakkiri 1" ,""+distance2+"Km");
                    mMap.addPolyline(lineOptions2);

                    lineOptions3.addAll(listOfPoints.get(2));
                    lineOptions3.width(10);
                    lineOptions3.color(Color.GRAY);
                    double distance3 = SphericalUtil.computeLength(listOfPoints.get(2))/1000.0;
                    Log.i("elakkiri 1" ,""+distance3+"Km");
                    mMap.addPolyline(lineOptions3);

                    lineOptions4.addAll(listOfPoints.get(3));
                    lineOptions4.width(10);
                    lineOptions4.color(Color.GRAY);
                    double distance4 = SphericalUtil.computeLength(listOfPoints.get(3))/1000.0;
                    Log.i("elakkiri 1" ,""+distance4+"Km");
                    mMap.addPolyline(lineOptions4);
                }
            }else {
                Log.d("onPostExecute","without Polylines drawn");
            }

            Log.d("onPostExecute","onPostExecute lineoptions decoded");
           // Log.i("elakkiri",""+distance+"Km");
            // Drawing polyline in the Google Map for the i-th route
            //if(lineOptions != null) {


        }
    }


    public class DataParser {

        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude) );
                                hm.put("lng", Double.toString((list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }


            return routes;
        }


        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         * */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

}
