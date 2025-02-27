package il.rjana.recipeit.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;
import il.rjana.recipeit.model.RecipeEntity;

@Dao
public interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecipeEntity recipe);

    @Update
    void update(RecipeEntity recipe);

    @Query("SELECT * FROM recipe_table WHERE title LIKE '%' || :query || '%' GROUP BY title")
    LiveData<List<RecipeEntity>> searchRecipesByName(String query);

    @Query("SELECT * FROM recipe_table GROUP BY title ORDER BY title ASC")
    LiveData<List<RecipeEntity>> getAllRecipes();

    @Query("SELECT * FROM recipe_table WHERE id = :id")
    LiveData<RecipeEntity> getRecipeById(int id);

    @Query("SELECT * FROM recipe_table WHERE isFavorite = 1")
    LiveData<List<RecipeEntity>> getFavoriteRecipes();

    @Delete
    void delete(RecipeEntity recipe);

    @Query("SELECT * FROM recipe_table WHERE area LIKE '%' || :area || '%' GROUP BY title")
    LiveData<List<RecipeEntity>> searchRecipesByArea(String area);
    
    @Query("SELECT * FROM recipe_table WHERE category LIKE '%' || :category || '%' GROUP BY title")
    LiveData<List<RecipeEntity>> searchRecipesByCategory(String category);
    
    @Query("SELECT * FROM recipe_table WHERE ingredients LIKE '%' || :ingredient || '%' GROUP BY title")
    LiveData<List<RecipeEntity>> searchRecipesByIngredient(String ingredient);
    
    @Query("SELECT DISTINCT area FROM recipe_table WHERE area IS NOT NULL AND area != '' ORDER BY area ASC")
    LiveData<List<String>> getAllAreas();
    
    @Query("SELECT DISTINCT category FROM recipe_table WHERE category IS NOT NULL AND category != '' ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();
    
    @Query("SELECT DISTINCT ingredients FROM recipe_table WHERE ingredients IS NOT NULL AND ingredients != ''")
    LiveData<List<String>> getAllIngredients();
}
