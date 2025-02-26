package il.rjana.recipeit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.ui.adapter.RecipeAdapter;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class SavedRecipesFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter adapter;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize views
        RecyclerView recyclerView = view.findViewById(R.id.saved_recipes_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(new ArrayList<>(), recipeViewModel);
        
        // Set item click listener for recipe details
        adapter.setOnItemClickListener(recipe -> {
            Bundle bundle = new Bundle();
            bundle.putInt("recipeId", recipe.getId());
            Navigation.findNavController(view).navigate(R.id.action_savedRecipesFragment_to_recipeDetailFragment, bundle);
        });
        
        // Set edit button click listener
        adapter.setOnEditClickListener(recipe -> {
            // Navigate to edit fragment with recipe ID
            Bundle bundle = new Bundle();
            bundle.putInt("recipeId", recipe.getId());
            Navigation.findNavController(view).navigate(R.id.action_savedRecipesFragment_to_editRecipeFragment, bundle);
        });
        
        recyclerView.setAdapter(adapter);

        // Setup swipe to delete
        setupSwipeToDelete(recyclerView);

        // Observe favorite recipes
        recipeViewModel.getFavoriteRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.setRecipes(recipes);
            
            // Show empty view if no recipes
            if (recipes.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSwipeToDelete(RecyclerView recyclerView) {
        // Add swipe to delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want to support moving items
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RecipeEntity recipe = adapter.getRecipeAt(position);
                
                // Show confirmation dialog
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        // Delete the recipe
                        recipeViewModel.delete(recipe);
                        Snackbar.make(recyclerView, R.string.recipe_deleted, Snackbar.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        // Cancel deletion and reset the adapter
                        adapter.notifyItemChanged(position);
                    })
                    .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                    .show();
            }
        }).attachToRecyclerView(recyclerView);
    }
} 