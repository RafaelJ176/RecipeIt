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
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.viewmodel.RecipeViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class EditRecipeFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private TextInputEditText titleInput, categoryInput, areaInput, ingredientsInput, instructionsInput;
    private ImageView recipeImageView;
    private String currentPhotoPath;
    private int recipeId;
    private RecipeEntity currentRecipe;
    
    // Activity result launchers for modern permission handling
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get recipe ID from arguments
        if (getArguments() != null) {
            recipeId = getArguments().getInt("recipeId", -1);
        }
        
        // Initialize permission launchers
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(requireContext(), R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchPickImageIntent();
                } else {
                    Toast.makeText(requireContext(), R.string.storage_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Initialize activity result launchers
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        recipeImageView.setImageBitmap(imageBitmap);
                        
                        // Save bitmap to internal storage
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
                            
                            // Save bitmap to internal storage
                            currentPhotoPath = saveImageToInternalStorage(bitmap);
                            Log.d(TAG, "Image saved at: " + currentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), R.string.failed_load_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize views
        titleInput = view.findViewById(R.id.recipe_title_input);
        categoryInput = view.findViewById(R.id.recipe_category_input);
        areaInput = view.findViewById(R.id.recipe_area_input);
        ingredientsInput = view.findViewById(R.id.recipe_ingredients_input);
        instructionsInput = view.findViewById(R.id.recipe_instructions_input);
        Button saveButton = view.findViewById(R.id.save_recipe_button);
        Button addImageButton = view.findViewById(R.id.add_image_button);
        recipeImageView = view.findViewById(R.id.recipe_image_preview);

        // Load recipe data if we have a valid ID
        if (recipeId != -1) {
            recipeViewModel.getRecipeById(recipeId).observe(getViewLifecycleOwner(), recipe -> {
                if (recipe != null) {
                    currentRecipe = recipe;
                    populateFields(recipe);
                }
            });
        }

        // Handle save button click
        saveButton.setOnClickListener(v -> updateRecipe());
        
        // Handle add image button click
        addImageButton.setOnClickListener(v -> showImageSourceDialog());
    }
    
    private void populateFields(RecipeEntity recipe) {
        titleInput.setText(recipe.getTitle());
        categoryInput.setText(recipe.getCategory());
        areaInput.setText(recipe.getArea());
        ingredientsInput.setText(recipe.getIngredients());
        instructionsInput.setText(recipe.getInstructions());
        
        // Load image if available
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(recipe.getImageUrl())
                    .into(recipeImageView);
            recipeImageView.setVisibility(View.VISIBLE);
            currentPhotoPath = recipe.getImageUrl().replace("file://", "");
        }
    }
    
    private void showImageSourceDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.choose_gallery)};
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_recipe_image)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Take photo with camera
                    checkCameraPermission();
                } else {
                    // Choose from gallery
                    checkStoragePermission();
                }
            })
            .show();
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
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
            requestStoragePermissionLauncher.launch(permission);
        } else {
            dispatchPickImageIntent();
        }
    }
    
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(requireContext(), R.string.no_camera_app, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
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

    private void updateRecipe() {
        if (currentRecipe == null) {
            Toast.makeText(getContext(), "Error: Recipe not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String category = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";
        String area = areaInput.getText() != null ? areaInput.getText().toString().trim() : "";
        String ingredients = ingredientsInput.getText() != null ? ingredientsInput.getText().toString().trim() : "";
        String instructions = instructionsInput.getText() != null ? instructionsInput.getText().toString().trim() : "";

        // Validate title is not empty
        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.title_required));
            Toast.makeText(getContext(), R.string.title_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate title contains only letters, numbers, and spaces
        if (!title.matches("^[a-zA-Z0-9 ]+$")) {
            titleInput.setError(getString(R.string.title_invalid));
            Toast.makeText(getContext(), R.string.title_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        // Update recipe fields
        currentRecipe.setTitle(title);
        currentRecipe.setCategory(category);
        currentRecipe.setArea(area);
        currentRecipe.setIngredients(ingredients);
        currentRecipe.setInstructions(instructions);
        
        // Update image if changed
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            if (!currentPhotoPath.startsWith("file://")) {
                currentRecipe.setImageUrl("file://" + currentPhotoPath);
            } else {
                currentRecipe.setImageUrl(currentPhotoPath);
            }
        }
        
        // Save changes
        recipeViewModel.update(currentRecipe);

        Toast.makeText(getContext(), R.string.recipe_updated, Toast.LENGTH_SHORT).show();
        
        // Navigate back to saved recipes
        Navigation.findNavController(requireView()).navigateUp();
    }
} 