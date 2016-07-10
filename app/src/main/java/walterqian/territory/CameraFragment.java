package walterqian.territory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.loopj.android.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by bpon on 7/9/16.
 */
public class CameraFragment extends Fragment {

    Button mTakePhotoButton;
    Camera mCamera;
    CameraPreview mCameraPreview;
    ImageView cameraView;

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d("CameraFragment", "on Shutter'd");
        }
    };

    Camera.PictureCallback mRawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("CameraFragment", "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("CameraFragment", "onPictureTaken - jpeg");


            UploadPhotoTask task = new UploadPhotoTask();
            task.execute(data);
            Toast.makeText(getActivity(), "Sent", Toast.LENGTH_SHORT).show();
        }
    };

    public void finishCamera(){
        getActivity().finish();
    }

    public boolean verifyphoto(){
        return false;
    }

    public void retryfragment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("The picture you took did not match.");

// Add the buttons
        builder.setPositiveButton("try again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.cancel();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                finishCamera();
            }
        });

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_camera, container, false);
        // Check if camera is available.
        if (checkForCameraHardware(getActivity())) {
            Log.d("CameraFragment", "Camera hardware is available! :)");
        } else {
            Log.d("CameraFragment", "Camera hardware is not available.");
        }

        // Get an instance of the camera
        mCamera = getCameraInstance();

        if (mCamera == null) {
            Log.d("CameraFragment", "Camera instance is null");
        } else {
            Log.d("CameraFragment", "Camera instance retrieved!");
        }
        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) v.findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);


        mTakePhotoButton = (Button) v.findViewById(R.id.take_photo_button);
        mTakePhotoButton.getBackground().setAlpha(255);
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Take a photo", Toast.LENGTH_SHORT).show();
                mCamera.takePicture(mShutterCallback, mRawCallback, mJpegCallback);
            }
        });

        return v;
    }


    /**
     * Check if this device has a camera
     */
    private boolean checkForCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("camerafragment", "camera not available");
        }
        return c; // returns null if camera is unavailable
    }


    private class UploadPhotoTask extends AsyncTask<byte [] , String,String>{
        @Override
        protected String doInBackground(byte []... params) {
            byte [] rawJpegImageData = params[0];
            String imageString = Base64.encodeToString(rawJpegImageData, Base64.DEFAULT);

            HttpClient httpClient = new DefaultHttpClient();
            //HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost postRequest = new HttpPost("http://52.40.56.30/verify");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            JSONArray jsonArray;
            String lat = preferences.getString("Lat", "failed to get lat");
            String lng = preferences.getString("Lng", "failed to get lng");
            String url = preferences.getString("url","failed to get url");
            String email = preferences.getString("email","failed to get email");

            ArrayList<NameValuePair> ValuePairs= new ArrayList<NameValuePair>();
            ValuePairs .add(new BasicNameValuePair("image", imageString));
            ValuePairs .add(new BasicNameValuePair("targetUrl", url));
            ValuePairs .add(new BasicNameValuePair("lat", lat));
            ValuePairs .add(new BasicNameValuePair("long", lng));


            JSONObject obj = new JSONObject();
            jsonArray = new JSONArray(ValuePairs);

            Log.d("camera sent", jsonArray.toString());
            Log.d("camera asynctask", ValuePairs.toString());
            Log.d("camera asynctask lat",ValuePairs.get(2).toString());
            Log.d("camera asynctask lng",ValuePairs.get(3).toString());
            Log.d("camera asynctask url",ValuePairs.get(1).toString());
            try {
                obj.put("image",imageString);
                obj.put("email",email);
                obj.put("targetUrl",url);
                obj.put("lat",lat);
                obj.put("lng",lng);
                StringEntity se = new StringEntity( obj.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                postRequest.setEntity(se);

                Log.d("camera sent", obj.toString());
                HttpResponse response = httpClient.execute(postRequest);
                //Read the

                InputStream ips  = response.getEntity().getContent();
                BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String s;
                while(true )
                {
                    s = buf.readLine();
                    if(s==null || s.length()==0)
                        break;
                    sb.append(s);
                }
                buf.close();
                ips.close();

                Log.v("camera return", "after uploading file "
                        + sb.toString());
                return sb.toString();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e){

            }
            return "FAAAILED";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Boolean verified = false;
            try {
                JSONObject obj = new JSONObject(s);
                verified = obj.getBoolean("result");
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            Log.d("async", "response recorded");

            verified = true;
            if(verified){
                Intent i = new Intent(getActivity(), UnlockActivity.class);
                startActivity(i);
            }
            else {
                retryfragment();
            }
        }
    }


}
