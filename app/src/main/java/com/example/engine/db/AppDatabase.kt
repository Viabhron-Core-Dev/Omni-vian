package com.example.engine.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.engine.db.ProviderPrepopulator

@Database(entities = [
    ChatMessageEntity::class, 
    WorkspaceConfigEntity::class, 
    WorkspaceIssueEntity::class, 
    WorkspacePullRequestEntity::class,
    ApiProviderEntity::class,
    ApiKeyEntity::class,
    FallbackChainEntity::class,
    TokenUsageEntity::class,
    ModelRatingEntity::class,
    RequestLogEntity::class,
    AiModelEntity::class
], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workspaceConfigDao(): WorkspaceConfigDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun workspaceIssueDao(): WorkspaceIssueDao
    abstract fun workspacePullRequestDao(): WorkspacePullRequestDao
    abstract fun apiProviderDao(): ApiProviderDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun fallbackChainDao(): FallbackChainDao
    abstract fun metricsDao(): MetricsDao
    abstract fun aiModelDao(): AiModelDao
    abstract fun modelRatingDao(): ModelRatingDao


    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_models ADD COLUMN inputType TEXT NOT NULL DEFAULT 'TEXT'")
                db.execSQL("ALTER TABLE ai_models ADD COLUMN outputType TEXT NOT NULL DEFAULT 'TEXT'")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN modelName TEXT")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN providerId TEXT")
                db.execSQL("CREATE TABLE IF NOT EXISTS `model_ratings` (`id` TEXT NOT NULL, `modelName` TEXT NOT NULL, `providerId` TEXT NOT NULL, `isPositive` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `model_ratings`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `model_ratings` (`messageId` TEXT NOT NULL, `modelName` TEXT NOT NULL, `providerId` TEXT NOT NULL, `isPositive` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`messageId`))")
            }
        }


        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_models ADD COLUMN description TEXT DEFAULT NULL")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omnivian_database"
                )
                .addCallback(DatabaseCallback())
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val providerDao = database.apiProviderDao()
                    providerDao.insertProviders(ProviderPrepopulator.defaultProviders)
                }
            }
        }
    }
}
