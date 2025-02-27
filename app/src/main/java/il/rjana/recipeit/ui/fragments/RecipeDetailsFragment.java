package il.rjana.recipeit.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import il.rjana.recipeit.R;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class RecipeDetailsFragment extends Fragment {
    private static final String ARG_RECIPE_ID = "recipe_id";
    private RecipeViewModel recipeViewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        ImageView recipeImage = view.findViewById(R.id.recipe_image);
        TextView titleText = view.findViewById(R.id.recipe_title);
        TextView categoryAreaText = view.findViewById(R.id.recipe_category_area);
        TextView ingredientsText = view.findViewById(R.id.recipe_ingredients);
        TextView instructionsText = view.findViewById(R.id.recipe_instructions);

        if (getArguments() != null) {
            int recipeId = getArguments().getInt(ARG_RECIPE_ID);
            
            recipeViewModel.getRecipeById(recipeId).observe(getViewLifecycleOwner(), recipe -> {
                if (recipe != null) {
                    titleText.setText(recipe.getTitle());
                    categoryAreaText.setText(String.format("%s | %s", recipe.getCategory(), recipe.getArea()));
                    ingredientsText.setText(recipe.getIngredients());
                    instructionsText.setText(recipe.getInstructions());

                    if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(recipe.getImageUrl())
                                .into(recipeImage);
                    }
                }
            });
        }
    }
} 