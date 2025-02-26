package il.rjana.recipeit.ui.fragments;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_rec, container, false);

        view.findViewById(R.id.button_search_rec).setOnClickListener(v -> {
            if (isInternetAvailable()) {
                Navigation.findNavController(v).navigate(R.id.action_mainRec_to_search_Rec);
            } else {
                Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_LONG).show();
            }
        });

        view.findViewById(R.id.button_myrec).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mainRec_to_myrec);
        });

        view.findViewById(R.id.button_saved_recipes).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mainRec_to_savedRecipesFragment);
        });

        view.findViewById(R.id.button_youtube).setOnClickListener(v -> {
            if (isInternetAvailable()) {
                Navigation.findNavController(v).navigate(R.id.action_mainRec_to_youTubeVideosFragment);
            } else {
                Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }

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