package com.example.becoming.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM character_state WHERE id = 1")
    fun getCharacterState(): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCharacter(character: CharacterEntity)

    @Query("UPDATE character_state SET currentXp = :newXp WHERE id = 1")
    suspend fun updateXp(newXp: Int)
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveQuests(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuest(quest: QuestEntity)

    @Query("UPDATE quests SET status = 'COMPLETED' WHERE id = :questId")
    suspend fun completeQuest(questId: String)
}

@Dao
interface ChronicleDao {
    @Query("SELECT * FROM chronicles ORDER BY completedAt DESC")
    fun getAllChronicles(): Flow<List<ChronicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChronicle(chronicle: ChronicleEntity)
}
