package il.rjana.recipeit.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.data.RecipeDao;
import il.rjana.recipeit.data.RecipeDatabase;

public class RecipeRepository {

    private RecipeDao recipeDao;
    private LiveData<List<RecipeEntity>> allRecipes;
    private LiveData<List<RecipeEntity>> favoriteRecipes;
    private LiveData<List<String>> allAreas;
    private LiveData<List<String>> allCategories;
    private LiveData<List<String>> allIngredients;

    public RecipeRepository(Application application) {
        RecipeDatabase database = RecipeDatabase.getDatabase(application);
        recipeDao = database.recipeDao();
        allRecipes = recipeDao.getAllRecipes();
        favoriteRecipes = recipeDao.getFavoriteRecipes();
        allAreas = recipeDao.getAllAreas();
        allCategories = recipeDao.getAllCategories();
        allIngredients = recipeDao.getAllIngredients();
    }

    public void insert(RecipeEntity recipe) {
        new Thread(() -> recipeDao.insert(recipe)).start();
    }

    public void update(RecipeEntity recipe) {
        new Thread(() -> recipeDao.update(recipe)).start();
    }

    public void delete(RecipeEntity recipe) {
        new Thread(() -> recipeDao.delete(recipe)).start();
    }

    public LiveData<List<RecipeEntity>> getAllRecipes() {
        return allRecipes;
    }

    public LiveData<List<RecipeEntity>> searchRecipesByName(String query) {
        return recipeDao.searchRecipesByName(query);
    }

    public LiveData<RecipeEntity> getRecipeById(int id) {
        return recipeDao.getRecipeById(id);
    }

    public LiveData<List<RecipeEntity>> getFavoriteRecipes() {
        return favoriteRecipes;
    }

    public LiveData<List<RecipeEntity>> searchRecipesByArea(String area) {
        return recipeDao.searchRecipesByArea(area);
    }

    public LiveData<List<RecipeEntity>> searchRecipesByCategory(String category) {
        return recipeDao.searchRecipesByCategory(category);
    }

    public LiveData<List<RecipeEntity>> searchRecipesByIngredient(String ingredient) {
        return recipeDao.searchRecipesByIngredient(ingredient);
    }

    public LiveData<List<String>> getAllAreas() {
        return allAreas;
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<String>> getAllIngredients() {
        return allIngredients;
    }
}
