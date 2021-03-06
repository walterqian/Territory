package walterqian.territory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TerritoryMarkFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TerritoryMarkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TerritoryMarkFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ImageView territoryTarget;
    TextView snippet;
    Button unlock;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TerritoryMarkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TerritoryMarkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TerritoryMarkFragment newInstance(String param1, String param2) {
        TerritoryMarkFragment fragment = new TerritoryMarkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_territory_mark, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        unlock = (Button) view.findViewById(R.id.unlock);
        territoryTarget = (ImageView) view.findViewById(R.id.territory_mark_image);
        snippet = (TextView) view.findViewById(R.id.snippet);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String urlStr = sharedpreferences.getString("url", "failed url");
        String snip = sharedpreferences.getString("snippet","failed snippet");


        Log.d("territorymark",urlStr);
        Log.d("territorymark",snip);


        snip = "\"" + snip + "\"";
        snippet.setText(snip);
        snippet.setTypeface(null, Typeface.ITALIC);
        Log.d("territorymark", snippet.toString());
        new DownloadImageTask(territoryTarget)
                .execute(urlStr);


        unlock.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(getActivity(), CameraActivity.class);
                startActivity(i);
            }
        });
    }
//
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null, scaled = null;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 2;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in, null, o);
                scaled = Bitmap.createScaledBitmap(mIcon11, 240, 240, true);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return scaled;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void update(){
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String urlStr = sharedpreferences.getString("url", "failed url");
        String snip = sharedpreferences.getString("snippet","failed snippet");


        Log.d("territorymark",urlStr);
        Log.d("territorymark",snip);

        snippet.setText(snip);
        new DownloadImageTask(territoryTarget)
                .execute(urlStr);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
