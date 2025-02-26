package il.rjana.recipeit.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import il.rjana.recipeit.model.RecipeEntity;
import il.rjana.recipeit.data.RecipeDao;

@Database(entities = {RecipeEntity.class}, version = 3, exportSchema = false)
public abstract class RecipeDatabase extends RoomDatabase {
    private static volatile RecipeDatabase INSTANCE;
    public abstract RecipeDao recipeDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add the new 'area' column
            database.execSQL("ALTER TABLE recipe_table ADD COLUMN area TEXT");
        }
    };
    
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add the new 'isFavorite' column with default value 0 (false)
            database.execSQL("ALTER TABLE recipe_table ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static RecipeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RecipeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RecipeDatabase.class, "recipe_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()  // Fallback if migrations fail
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
