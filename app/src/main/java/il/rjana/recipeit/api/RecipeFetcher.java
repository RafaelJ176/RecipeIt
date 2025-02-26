package il.rjana.recipeit.api;

import android.app.Application;
import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.viewmodel.RecipeViewModel;
import androidx.lifecycle.ViewModelProvider;


public class RecipeFetcher {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/search.php?s=";
    private RecipeViewModel viewModel;

    public RecipeFetcher(Application application) {
        viewModel = new ViewModelProvider.AndroidViewModelFactory(application).create(RecipeViewModel.class);
    }

    public void fetchAndSaveRecipes(String query) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + query);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "application/json");

                int status = connection.getResponseCode();
                if (status == 200) {
                    InputStream responseStream = connection.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseStream);

                    JsonNode meals = root.path("meals");
                    if (meals.isArray()) {
                        meals.forEach(meal -> {
                            String title = meal.path("strMeal").asText().trim();
                            String category = meal.path("strCategory").asText();
                            String area = meal.path("strArea").asText();
                            String instructions = meal.path("strInstructions").asText();
                            String imageUrl = meal.path("strMealThumb").asText();

                            StringBuilder ingredients = new StringBuilder();
                            for (int i = 1; i <= 20; i++) {
                                String ingredient = meal.path("strIngredient" + i).asText();
                                String measure = meal.path("strMeasure" + i).asText();
                                
                                if (ingredient != null && !ingredient.isEmpty() && !ingredient.equals("null")) {
                                    if (ingredients.length() > 0) {
                                        ingredients.append("\n");
                                    }
                                    ingredients.append(measure).append(" ").append(ingredient);
                                }
                            }

                            RecipeEntity recipeEntity = new RecipeEntity(
                                title,
                                ingredients.toString(),
                                category,
                                instructions,
                                imageUrl,
                                area
                            );
                            viewModel.insert(recipeEntity);

                            Log.d("RecipeFetcher", "Saved Recipe: " + title + 
                                  " | Category: " + category + 
                                  " | Area: " + area);
                        });
                    } else {
                        Log.d("RecipeFetcher", "No recipes found for query: " + query);
                    }
                } else {
                    Log.e("RecipeFetcher", "Failed to fetch recipes. HTTP Status: " + status);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
