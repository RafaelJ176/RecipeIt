package il.rjana.recipeit.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipe_table")
public class RecipeEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String ingredients;
    private String category;
    private String instructions;
    private String imageUrl;
    private String area;
    private boolean isFavorite;

    public RecipeEntity(String title, String ingredients, String category, String instructions, String imageUrl, String area) {
        this.title = title;
        this.ingredients = ingredients;
        this.category = category;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.area = area;
        this.isFavorite = false;
    }

    public RecipeEntity() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
