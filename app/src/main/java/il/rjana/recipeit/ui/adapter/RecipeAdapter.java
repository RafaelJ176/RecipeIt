package il.rjana.recipeit.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import il.rjana.recipeit.R;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.viewmodel.RecipeViewModel;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeEntity> recipes;
    private RecipeViewModel viewModel;
    private OnItemClickListener listener;
    private OnEditClickListener editListener;

    public interface OnItemClickListener {
        void onItemClick(RecipeEntity recipe);
    }
    
    public interface OnEditClickListener {
        void onEditClick(RecipeEntity recipe);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public RecipeAdapter(List<RecipeEntity> recipes, RecipeViewModel viewModel) {
        this.recipes = recipes;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeEntity currentRecipe = recipes.get(position);
        holder.titleTextView.setText(currentRecipe.getTitle());
        
        String categoryAndArea = currentRecipe.getCategory() + " | " + currentRecipe.getArea();
        holder.categoryAreaTextView.setText(categoryAndArea);
        
        if (currentRecipe.getImageUrl() != null && !currentRecipe.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentRecipe.getImageUrl())
                    .into(holder.recipeImageView);
        }

        holder.favoriteButton.setImageResource(
            currentRecipe.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );

        holder.favoriteButton.setOnClickListener(v -> {
            viewModel.toggleFavorite(currentRecipe);
        });
        
        if (currentRecipe.isFavorite() && holder.editButton != null) {
            holder.editButton.setVisibility(View.VISIBLE);
            
            holder.editButton.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEditClick(currentRecipe);
                }
            });
        } else if (holder.editButton != null) {
            holder.editButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentRecipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public void setRecipes(List<RecipeEntity> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public RecipeEntity getRecipeAt(int position) {
        return recipes.get(position);
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView categoryAreaTextView;
        private ImageView recipeImageView;
        private ImageButton favoriteButton;
        private ImageButton editButton;

        RecipeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipe_title);
            categoryAreaTextView = itemView.findViewById(R.id.recipe_category_area);
            recipeImageView = itemView.findViewById(R.id.recipe_image);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            editButton = itemView.findViewById(R.id.edit_button);
        }
    }
}
