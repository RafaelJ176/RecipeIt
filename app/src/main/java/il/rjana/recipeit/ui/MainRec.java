package il.rjana.recipeit.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import il.rjana.recipeit.R;


public class MainRec extends Fragment {


    public MainRec() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_rec, container, false);

        // Button to navigate to Search Rec
        view.findViewById(R.id.button_search_rec).setOnClickListener(v -> {
            if (isInternetAvailable()) {
                Navigation.findNavController(v).navigate(R.id.action_mainRec_to_search_Rec);
            } else {
                Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_LONG).show();
            }
        });

        // Button to navigate to Create Recipe (Myrec)
        view.findViewById(R.id.button_myrec).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mainRec_to_myrec);
        });

        // Button to navigate to Saved Recipes
        view.findViewById(R.id.button_saved_recipes).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mainRec_to_savedRecipesFragment);
        });

        // Button to navigate to YouTube Videos
        view.findViewById(R.id.button_youtube).setOnClickListener(v -> {
            if (isInternetAvailable()) {
                Navigation.findNavController(v).navigate(R.id.action_mainRec_to_youTubeVideosFragment);
            } else {
                Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_LONG).show();
            }
        });

   /*     // Button to navigate to Myrec
        view.findViewById(R.id.button_myrec).setOnClickListener(v -> {
            // Navigate to Myrec fragment
            getParentFragmentManager().beginTransaction()
                .replace(R.id.main, Myrec.newInstance("param1", "param2"))
                .addToBackStack(null)
                .commit();
        });

        // Button to navigate to Search Rec fragment
        view.findViewById(R.id.button_search_rec).setOnClickListener(v -> {
            // Navigate to Search Rec fragment
            // Assuming SearchRec is another fragment class
            getParentFragmentManager().beginTransaction()
                .replace(R.id.main, SearchRec.newInstance("param1", "param2"))
                .addToBackStack(null)
                .commit();
        });

        // Button to navigate to YouTube Videos
        view.findViewById(R.id.button_youtube).setOnClickListener(v -> {
            // Navigate to YouTube Videos fragment or activity
            // Assuming YouTubeFragment is another fragment class
            getParentFragmentManager().beginTransaction()
                .replace(R.id.main, YouTubeFragment.newInstance("param1", "param2"))
                .addToBackStack(null)
                .commit();
        });
*/
        return view;
    }
    
    /**
     * Checks if the device has an active internet connection
     * @return true if internet is available, false otherwise
     */
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}