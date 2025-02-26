package il.rjana.recipeit.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.repository.RecipeRepository;


public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository repository;
    private LiveData<List<RecipeEntity>> allRecipes;
    private LiveData<List<RecipeEntity>> favoriteRecipes;
    private LiveData<List<String>> allAreas;
    private LiveData<List<String>> allCategories;
    private LiveData<List<String>> allIngredients;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        allRecipes = repository.getAllRecipes();
        favoriteRecipes = repository.getFavoriteRecipes();
        allAreas = repository.getAllAreas();
        allCategories = repository.getAllCategories();
        allIngredients = repository.getAllIngredients();
    }

    public void insert(RecipeEntity recipe) {
        repository.insert(recipe);
    }

    public void update(RecipeEntity recipe) {
        repository.update(recipe);
    }

    public LiveData<List<RecipeEntity>> getAllRecipes() {
        return allRecipes;
    }

    public LiveData<List<RecipeEntity>> getFavoriteRecipes() {
        return favoriteRecipes;
    }

    // New Method for Searching by Name
    public LiveData<List<RecipeEntity>> searchRecipesByName(String query) {
        return repository.searchRecipesByName(query);
    }

    public LiveData<RecipeEntity> getRecipeById(int id) {
        return repository.getRecipeById(id);
    }

    public void toggleFavorite(RecipeEntity recipe) {
        recipe.setFavorite(!recipe.isFavorite());
        repository.update(recipe);
    }

    public void delete(RecipeEntity recipe) {
        repository.delete(recipe);
    }

    public LiveData<List<RecipeEntity>> searchRecipesByArea(String area) {
        return repository.searchRecipesByArea(area);
    }

    // Search by Category
    public LiveData<List<RecipeEntity>> searchRecipesByCategory(String category) {
        return repository.searchRecipesByCategory(category);
    }
    
    // Search by Ingredient
    public LiveData<List<RecipeEntity>> searchRecipesByIngredient(String ingredient) {
        return repository.searchRecipesByIngredient(ingredient);
    }
    
    // Get all areas for spinner
    public LiveData<List<String>> getAllAreas() {
        return allAreas;
    }
    
    // Get all categories for spinner
    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }
    
    // Get all ingredients for spinner
    public LiveData<List<String>> getAllIngredients() {
        return allIngredients;
    }
}
