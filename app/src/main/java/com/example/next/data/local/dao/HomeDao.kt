package com.example.next.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.next.data.local.entity.HomeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {

    @Query("SELECT * FROM home_items")
    fun getAllItems(): Flow<List<HomeItemEntity>>

    @Query("SELECT * FROM home_items WHERE id = :id")
    suspend fun getItemById(id: String): HomeItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HomeItemEntity>)

    @Query("UPDATE home_items SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String): Int

    @Query("DELETE FROM home_items")
    suspend fun clearAll(): Int
}
