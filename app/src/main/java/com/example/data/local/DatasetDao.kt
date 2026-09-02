package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DatasetDao {

    @Query("SELECT * FROM custom_datasets ORDER BY createdAt DESC")
    fun getAllCustomDatasets(): Flow<List<DatasetEntity>>

    @Query("SELECT * FROM custom_datasets WHERE id = :id LIMIT 1")
    suspend fun getDatasetById(id: String): DatasetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DatasetEntity)

    @Delete
    suspend fun deleteDataset(dataset: DatasetEntity)

    @Query("DELETE FROM custom_datasets WHERE id = :id")
    suspend fun deleteDatasetById(id: String)

    // Training Runs History
    @Query("SELECT * FROM training_runs ORDER BY timestamp DESC")
    fun getAllTrainingRuns(): Flow<List<TrainingRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingRun(run: TrainingRunEntity)

    @Query("DELETE FROM training_runs WHERE runId = :runId")
    suspend fun deleteTrainingRun(runId: Long)
}
