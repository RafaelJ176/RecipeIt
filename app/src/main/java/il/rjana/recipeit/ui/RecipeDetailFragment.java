package il.rjana.recipeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class RecipeDetailFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private int recipeId;
    private LinearLayout ingredientsContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getInt("recipeId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        ImageView recipeImageView = view.findViewById(R.id.recipe_detail_image);
        TextView titleTextView = view.findViewById(R.id.recipe_detail_title);
        TextView categoryTextView = view.findViewById(R.id.recipe_detail_category);
        TextView areaTextView = view.findViewById(R.id.recipe_detail_area);
        ingredientsContainer = view.findViewById(R.id.ingredients_container);
        TextView instructionsTextView = view.findViewById(R.id.recipe_detail_instructions);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Observe recipe data
        if (recipeId != -1) {
            recipeViewModel.getRecipeById(recipeId).observe(getViewLifecycleOwner(), recipe -> {
                if (recipe != null) {
                    // Populate views with recipe data
                    titleTextView.setText(recipe.getTitle());
                    categoryTextView.setText("Category: " + recipe.getCategory());
                    areaTextView.setText("Area: " + recipe.getArea());
                    instructionsTextView.setText(recipe.getInstructions());

                    // Create interactive ingredient checkboxes
                    createIngredientCheckboxes(recipe.getIngredients());

                    // Load image
                    String imageUrl = recipe.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.startsWith("file://")) {
                            // Load local image
                            Glide.with(this)
                                .load(imageUrl.replace("file://", ""))
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(recipeImageView);
                        } else {
                            // Load remote image
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(recipeImageView);
                        }
                    } else {
                        recipeImageView.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            });
        }
    }
    
    private void createIngredientCheckboxes(String ingredientsText) {
        // Clear previous ingredients
        ingredientsContainer.removeAllViews();
        
        // Split ingredients by new line
        String[] ingredients = ingredientsText.split("\\n");
        
        // Create a checkbox for each ingredient
        for (String ingredient : ingredients) {
            if (ingredient.trim().isEmpty()) continue;
            
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(ingredient.trim());
            checkBox.setTextSize(16);
            
            // Add some padding
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            checkBox.setPadding(0, padding / 2, 0, padding / 2);
            
            ingredientsContainer.addView(checkBox);
        }
    }
} 