package com.example.engine.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    RequestLogEntity::class
], version = 4, exportSchema = false)
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

    companion object {
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
                .fallbackToDestructiveMigration()
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
