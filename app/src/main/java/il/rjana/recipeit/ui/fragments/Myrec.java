package il.rjana.recipeit.ui.fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.ui.adapter.RecipeAdapter;
import il.rjana.recipeit.viewmodel.RecipeViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Myrec extends Fragment {

        private RecipeViewModel recipeViewModel;
    private TextInputEditText titleInput, categoryInput, areaInput, ingredientsInput, instructionsInput;
    private RecipeAdapter adapter;
    private ImageView recipeImageView;
    private String currentPhotoPath;
    
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    public Myrec() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchPickImageIntent();
                } else {
                    Toast.makeText(requireContext(), "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        recipeImageView.setImageBitmap(imageBitmap);
                        recipeImageView.setVisibility(View.VISIBLE);
                        
                        currentPhotoPath = saveImageToInternalStorage(imageBitmap);
                        Log.d(TAG, "Image saved at: " + currentPhotoPath);
                    }
                }
            }
        );
        
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(), selectedImageUri);
                            recipeImageView.setImageBitmap(bitmap);
                            recipeImageView.setVisibility(View.VISIBLE);
                            
                            currentPhotoPath = saveImageToInternalStorage(bitmap);
                            Log.d(TAG, "Image saved at: " + currentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myrec, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        titleInput = view.findViewById(R.id.recipe_title_input);
        categoryInput = view.findViewById(R.id.recipe_category_input);
        areaInput = view.findViewById(R.id.recipe_area_input);
        ingredientsInput = view.findViewById(R.id.recipe_ingredients_input);
        instructionsInput = view.findViewById(R.id.recipe_instructions_input);
        Button saveButton = view.findViewById(R.id.save_recipe_button);
        Button addImageButton = view.findViewById(R.id.add_image_button);
        recipeImageView = view.findViewById(R.id.recipe_image_preview);

        RecyclerView recyclerView = view.findViewById(R.id.favorite_recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(new ArrayList<>(), recipeViewModel);
        
        adapter.setOnItemClickListener(recipe -> {
            Bundle bundle = new Bundle();
            bundle.putInt("recipeId", recipe.getId());
            Navigation.findNavController(view).navigate(R.id.action_myrec_to_recipeDetailFragment, bundle);
        });
        
        recyclerView.setAdapter(adapter);

        recipeViewModel.getFavoriteRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.setRecipes(recipes);
            adapter.notifyDataSetChanged();
        });

        saveButton.setOnClickListener(v -> saveRecipe());
        
        addImageButton.setOnClickListener(v -> showImageSourceDialog());
        
        Log.d(TAG, "Fragment view created");
    }
    
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Recipe Image")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermission();
                } else {
                    checkStoragePermission();
                }
            })
            .show();
        
        Log.d(TAG, "Image source dialog shown");
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting camera permission");
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d(TAG, "Camera permission already granted");
            dispatchTakePictureIntent();
        }
    }
    
    private void checkStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting storage permission: " + permission);
            requestStoragePermissionLauncher.launch(permission);
        } else {
            Log.d(TAG, "Storage permission already granted");
            dispatchPickImageIntent();
        }
    }
    
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            Log.d(TAG, "Launching camera intent");
            takePictureLauncher.launch(takePictureIntent);
        } else {
            Log.e(TAG, "No camera app available");
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        Log.d(TAG, "Launching gallery intent");
        pickImageLauncher.launch(pickImageIntent);
    }
    
    private String saveImageToInternalStorage(Bitmap bitmap) {
        String fileName = "RECIPE_" + UUID.randomUUID().toString() + ".jpg";
        File directory = requireContext().getFilesDir();
        File file = new File(directory, fileName);
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRecipe() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String category = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";
        String area = areaInput.getText() != null ? areaInput.getText().toString().trim() : "";
        String ingredients = ingredientsInput.getText() != null ? ingredientsInput.getText().toString().trim() : "";
        String instructions = instructionsInput.getText() != null ? instructionsInput.getText().toString().trim() : "";

        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.title_required));
            Toast.makeText(getContext(), R.string.title_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!title.matches("^[a-zA-Z0-9 ]+$")) {
            titleInput.setError(getString(R.string.title_invalid));
            Toast.makeText(getContext(), R.string.title_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        RecipeEntity recipe = new RecipeEntity(
            title,
            ingredients,
            category,
            instructions,
            currentPhotoPath != null ? "file://" + currentPhotoPath : "",  // Use local file path if available
            area
        );
        
        recipe.setFavorite(true);
        
        recipeViewModel.insert(recipe);

        titleInput.setText("");
        categoryInput.setText("");
        areaInput.setText("");
        ingredientsInput.setText("");
        instructionsInput.setText("");
        recipeImageView.setImageBitmap(null);
        recipeImageView.setVisibility(View.GONE);
        currentPhotoPath = null;

        Toast.makeText(getContext(), R.string.recipe_saved, Toast.LENGTH_SHORT).show();
        
        Navigation.findNavController(requireView()).navigate(R.id.action_myrec_to_savedRecipesFragment);
    }
}