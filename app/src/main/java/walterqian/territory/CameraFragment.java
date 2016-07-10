package walterqian.territory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.HttpClientBuilder;

//import com.loopj.android.http.*;

import java.io.IOException;

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


    private class UploadPhotoTask extends AsyncTask<byte [] , String, String>{
        @Override
        protected String doInBackground(byte []... params) {
            byte [] rawJpegImageData = params[0];
            HttpClient httpClient = new DefaultHttpClient();
            //HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost postRequest = new HttpPost("http://52.40.56.30/verify");
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);

            //Set Data and Content-type header for the image
            entity.addPart("file",
                    new ByteArrayBody(rawJpegImageData, "image/jpeg", "file"));
            postRequest.setEntity(entity);

            try {

                HttpResponse response = httpClient.execute(postRequest);
                //Read the response
                String jsonString = EntityUtils.toString(response.getEntity());
                Log.v("uploading", "after uploading file "
                        + jsonString);
                return jsonString;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "FAAAILED";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("async", "response recorded");

            if(verifyphoto()){
                Intent i = new Intent(getActivity(), UnlockActivity.class);
                startActivity(i);
            }
            else {
                retryfragment();

            }
        }
    }


}
