package il.rjana.recipeit.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import il.rjana.recipeit.R;
import il.rjana.recipeit.api.RecipeFetcher;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.ui.adapter.RecipeAdapter;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class Search_Rec extends Fragment {

    private static final String TAG = "Search_Rec";
    private static final String PREFS_NAME = "RecipeItPrefs";
    private static final String KEY_LOCATION_PERMISSION_ASKED = "location_permission_asked";
    private static final String KEY_LOCATION_PERMISSION_GRANTED = "location_permission_granted";

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter adapter;
    private EditText searchInput;
    private RecipeFetcher fetcher;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    
    // Track current active LiveData
    private LiveData<List<RecipeEntity>> currentLiveData;
    private Observer<List<RecipeEntity>> currentObserver;
    
    // Spinners for filtering
    private Spinner regionSpinner;
    private Spinner categorySpinner;
    private Spinner ingredientSpinner;
    private Button clearFiltersButton;
    private TextView resultsLabel;
    
    // Current filter values
    private String currentRegion = "";
    private String currentCategory = "";
    private String currentIngredient = "";
    private String currentSearchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize location permission launcher
        requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                // Save the user's choice
                sharedPreferences.edit()
                    .putBoolean(KEY_LOCATION_PERMISSION_ASKED, true)
                    .putBoolean(KEY_LOCATION_PERMISSION_GRANTED, isGranted)
                    .apply();
                
                if (isGranted) {
                    getLocationAndSuggestRecipes();
                } else {
                    Log.d(TAG, "Location permission denied by user");
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_rec, container, false);
        
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize views with standard ID naming conventions
        searchInput = view.findViewById(R.id.search_input);
        Button searchButton = view.findViewById(R.id.search_button);
        RecyclerView recyclerView = view.findViewById(R.id.recipes_recycler_view);
        regionSpinner = view.findViewById(R.id.region_spinner);
        categorySpinner = view.findViewById(R.id.category_spinner);
        ingredientSpinner = view.findViewById(R.id.ingredient_spinner);
        clearFiltersButton = view.findViewById(R.id.clear_filters_button);
        resultsLabel = view.findViewById(R.id.results_label);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(new ArrayList<>(), recipeViewModel);
        
        // Set item click listener for recipe details
        adapter.setOnItemClickListener(recipe -> {
            Bundle bundle = new Bundle();
            bundle.putInt("recipeId", recipe.getId());
            Navigation.findNavController(view).navigate(R.id.action_search_Rec_to_recipeDetailFragment, bundle);
        });
        
        recyclerView.setAdapter(adapter);

        // Initialize fetcher
        fetcher = new RecipeFetcher(requireActivity().getApplication());

        // Initialize location manager
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        
        // Setup spinners
        setupSpinners();
        
        // Clear filters at startup to ensure proper initial state
        clearFilters();
        
        // Set default observer to show all recipes
        updateObserver(recipeViewModel.getAllRecipes());
        
        // Handle search button click
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            currentSearchQuery = query;

            if (!query.isEmpty()) {
                // Search local database
                updateObserver(recipeViewModel.searchRecipesByName(query));

                // Also fetch from API to update database
                fetcher.fetchAndSaveRecipes(query);
            } else {
                // If search is empty, apply other filters if any
                applyFilters();
            }
        });
        
        // Handle clear filters button
        clearFiltersButton.setOnClickListener(v -> clearFilters());
        
        // Check if we should ask for location permission
        checkLocationPermissionState();
    }
    
    private void setupSpinners() {
        // Setup region spinner
        List<String> defaultRegions = new ArrayList<>();
        defaultRegions.add(getString(R.string.all));
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, defaultRegions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        
        // Setup category spinner
        List<String> defaultCategories = new ArrayList<>();
        defaultCategories.add(getString(R.string.all));
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, defaultCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Setup ingredient spinner with a null option
        List<String> defaultIngredients = new ArrayList<>();
        defaultIngredients.add(""); // Empty option for no filtering
        defaultIngredients.add(getString(R.string.all));
        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_spinner_item, defaultIngredients) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                
                // Display empty string as "Select Ingredient"
                if (position == 0) {
                    textView.setText(R.string.select_ingredient);
                }
                
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                
                // Display empty string as "Select Ingredient" in dropdown too
                if (position == 0) {
                    textView.setText(R.string.select_ingredient);
                }
                
                return view;
            }
        };
        ingredientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientSpinner.setAdapter(ingredientAdapter);
        
        // Load actual data from database
        recipeViewModel.getAllAreas().observe(getViewLifecycleOwner(), areas -> {
            if (areas != null && !areas.isEmpty()) {
                List<String> regionList = new ArrayList<>();
                regionList.add(getString(R.string.all));
                regionList.addAll(areas);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, regionList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                regionSpinner.setAdapter(adapter);
            }
        });
        
        recipeViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                List<String> categoryList = new ArrayList<>();
                categoryList.add(getString(R.string.all));
                categoryList.addAll(categories);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, categoryList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });
        
        recipeViewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            if (ingredients != null && !ingredients.isEmpty()) {
                // Extract individual ingredients from the combined strings
                Set<String> uniqueIngredients = new HashSet<>();
                
                for (String ingredientList : ingredients) {
                    if (ingredientList != null && !ingredientList.isEmpty()) {
                        String[] parts = ingredientList.split("\n");
                        for (String part : parts) {
                            // Extract ingredient name (after the measurement)
                            String[] ingredientParts = part.split(" ", 2);
                            if (ingredientParts.length > 1) {
                                uniqueIngredients.add(ingredientParts[1].trim());
                            }
                        }
                    }
                }
                
                List<String> ingredientList = new ArrayList<>();
                ingredientList.add(""); // Empty option for no filtering
                ingredientList.add(getString(R.string.all));
                ingredientList.addAll(uniqueIngredients);
                java.util.Collections.sort(ingredientList.subList(2, ingredientList.size())); // Sort all except first two items
                
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        requireContext(), android.R.layout.simple_spinner_item, ingredientList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        
                        // Display empty string as "Select Ingredient"
                        if (position == 0) {
                            textView.setText(R.string.select_ingredient);
                        }
                        
                        return view;
                    }
                    
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        
                        // Display empty string as "Select Ingredient" in dropdown too
                        if (position == 0) {
                            textView.setText(R.string.select_ingredient);
                        }
                        
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ingredientSpinner.setAdapter(adapter);
            }
        });
        
        // Set spinner listeners
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals(getString(R.string.all))) {
                    currentRegion = "";
            } else {
                    currentRegion = selected;
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentRegion = "";
            }
        });
        
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals(getString(R.string.all))) {
                    currentCategory = "";
                } else {
                    currentCategory = selected;
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategory = "";
            }
        });
        
        ingredientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                
                // Handle the special empty case (position 0)
                if (position == 0) {
                    // Set to null to indicate no ingredient filtering
                    currentIngredient = null;
                } else if (selected.equals(getString(R.string.all))) {
                    // "All" option - empty string means show all
                    currentIngredient = "";
                } else {
                    // Normal ingredient selection
                    currentIngredient = selected;
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Set to null to indicate no ingredient filtering
                currentIngredient = null;
            }
        });
    }
    
    private void clearFilters() {
        // Reset spinners to "All"
        if (regionSpinner.getAdapter() != null && regionSpinner.getAdapter().getCount() > 0) {
            regionSpinner.setSelection(0);
        }
        
        if (categorySpinner.getAdapter() != null && categorySpinner.getAdapter().getCount() > 0) {
            categorySpinner.setSelection(0);
        }
        
        // Reset ingredient spinner to the first position (null option)
        if (ingredientSpinner.getAdapter() != null && ingredientSpinner.getAdapter().getCount() > 0) {
            ingredientSpinner.setSelection(0);
        }
        
        searchInput.setText("");
        
        // Reset filter values
        currentRegion = "";
        currentCategory = "";
        currentIngredient = null; // Set to null to indicate no ingredient filtering
        currentSearchQuery = "";
        
        // Show all recipes
        updateObserver(recipeViewModel.getAllRecipes());
    }
    
    private void applyFilters() {
        // Determine which filter to apply based on what's selected
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            // If search query is provided, it takes precedence
            updateObserver(recipeViewModel.searchRecipesByName(currentSearchQuery));
            return;
        }
        
        if (!TextUtils.isEmpty(currentRegion)) {
            // If region is selected
            updateObserver(recipeViewModel.searchRecipesByArea(currentRegion));
            return;
        }
        
        if (!TextUtils.isEmpty(currentCategory)) {
            // If category is selected
            updateObserver(recipeViewModel.searchRecipesByCategory(currentCategory));
            return;
        }
        
        // Only apply ingredient filter if currentIngredient is not null
        // (null means the user hasn't selected any ingredient option)
        if (currentIngredient != null && !TextUtils.isEmpty(currentIngredient)) {
            // If ingredient is selected
            updateObserver(recipeViewModel.searchRecipesByIngredient(currentIngredient));
            return;
        }
        
        // If no filters are applied, show all recipes
        updateObserver(recipeViewModel.getAllRecipes());
    }
    
    /**
     * Updates the current observer by removing any existing observer
     * and adding a new one for the provided LiveData
     */
    private void updateObserver(LiveData<List<RecipeEntity>> newLiveData) {
        // Remove previous observer if it exists
        if (currentLiveData != null && currentObserver != null) {
            currentLiveData.removeObserver(currentObserver);
        }
        
        // Create new observer
        currentObserver = recipes -> {
            if (recipes != null) {
                Log.d(TAG, "Recipes Found: " + recipes.size());
                adapter.setRecipes(recipes);
                
                // Update results label
                resultsLabel.setText(getString(R.string.results) + " (" + recipes.size() + ")");
            } else {
                Log.d(TAG, "No Recipes Found");
                adapter.setRecipes(new ArrayList<>());
                
                // Update results label
                resultsLabel.setText(getString(R.string.results) + " (0)");
            }
        };
        
        // Set new LiveData and observe it
        currentLiveData = newLiveData;
        currentLiveData.observe(getViewLifecycleOwner(), currentObserver);
    }
    
    private void checkLocationPermissionState() {
        boolean permissionAsked = sharedPreferences.getBoolean(KEY_LOCATION_PERMISSION_ASKED, false);
        
        if (!permissionAsked) {
            // First time - show dialog asking if user wants location-based recipes
            showLocationSuggestionDialog();
        } else {
            // We've asked before, check if permission was granted
            boolean permissionGranted = sharedPreferences.getBoolean(KEY_LOCATION_PERMISSION_GRANTED, false);
            
            if (permissionGranted) {
                // Permission was granted previously, get location recipes
                getLocationAndSuggestRecipes();
            }
            // If not granted, do nothing - respect user's previous choice
        }
    }
    
    private void showLocationSuggestionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage(R.string.location_suggestion)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                checkLocationPermission();
            })
            .setNegativeButton(R.string.no, (dialog, which) -> {
                // User declined - save this preference
                sharedPreferences.edit()
                    .putBoolean(KEY_LOCATION_PERMISSION_ASKED, true)
                    .putBoolean(KEY_LOCATION_PERMISSION_GRANTED, false)
                    .apply();
            })
            .show();
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            // Permission already granted
            sharedPreferences.edit()
                .putBoolean(KEY_LOCATION_PERMISSION_ASKED, true)
                .putBoolean(KEY_LOCATION_PERMISSION_GRANTED, true)
                .apply();
            getLocationAndSuggestRecipes();
        }
    }
    
    private void getLocationAndSuggestRecipes() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                
                Location location = null;
                
                // Try to get last known location from GPS
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                        == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                // If GPS location is null, try network provider
                if (location == null && ContextCompat.checkSelfPermission(requireContext(), 
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                
                if (location != null) {
                    // Get country from location
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    
                    if (addresses != null && !addresses.isEmpty()) {
                        String country = addresses.get(0).getCountryName();
                        Log.d(TAG, "Current country: " + country);
                        
                        // Search for recipes from this country/region
                        if (!TextUtils.isEmpty(country)) {
                            searchInput.setText(country);
                            currentSearchQuery = country;
                            fetcher.fetchAndSaveRecipes(country);
                            
                            // Update observer with area-based results
                            updateObserver(recipeViewModel.searchRecipesByArea(country));
                        }
                    }
                }
            }
        } catch (SecurityException | IOException e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
        }
    }
    
    @Override
    public void onDestroyView() {
        // Clean up observer when view is destroyed
        if (currentLiveData != null && currentObserver != null) {
            currentLiveData.removeObserver(currentObserver);
        }
        super.onDestroyView();
    }
}
