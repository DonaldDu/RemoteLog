package com.dhy.remotelog.room

import android.content.Context
import androidx.paging.DataSource
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Dao
interface RequestDao {
    @Insert
    fun insert(log: RequestLog)

    @Query("DELETE FROM NetLog")
    fun clearAll()

    @Query("SELECT * FROM NetLog order by id desc")
    fun getAllByDataSource(): DataSource.Factory<Int, RequestLog>
}

@Database(entities = [RequestLog::class], version = 1)
abstract class NetLogDatabase : RoomDatabase() {
    abstract fun requestDao(): RequestDao
}

private var dao: RequestDao? = null
fun getDb(context: Context? = null): RequestDao? {
    if (dao != null || context == null) return dao
    val migration: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }
    dao = Room.databaseBuilder(context.applicationContext, NetLogDatabase::class.java, "NetLog.db")
        .allowMainThreadQueries()
        .addMigrations(migration)
        .build()
        .requestDao()
    return dao
}