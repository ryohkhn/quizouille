package com.univ.quizouille.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.univ.quizouille.model.Answer
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.model.QuestionSetStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert
    suspend fun insertQuestionSetStats(questionSetStatistics: QuestionSetStatistics)

    @Insert
    suspend fun insertQuestionSet(questionSet: QuestionSet) : Long

    @Insert
    suspend fun insertQuestion(question: Question) : Long

    @Insert
    suspend fun insertAnswer(answer: Answer)

    @Query("SELECT * FROM answers WHERE questionId = :questionId")
    fun getAllAnswerForQuestion(questionId: Int): Flow<List<Answer>>

    @Query("SELECT * FROM answers WHERE answerId = :answerId")
    fun getAnswerById(answerId: Int): Flow<Answer>

    @Query("SELECT * FROM question_sets")
    fun getAllQuestionSets(): Flow<List<QuestionSet>>
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>
    @Query("SELECT * FROM answers")
    fun getAllAnswers(): Flow<List<Answer>>

    @Query("SELECT * FROM questions WHERE questionSetId = :setId")
    fun getQuestionsForSet(setId: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE questionId = :questionId")
    fun getQuestionById(questionId: Int): Flow<Question>

    @Query("SELECT MAX(questionId) FROM questions")
    fun getLatestQuestionId(): Flow<Int>
    @Query("SELECT MAX(setId) FROM question_sets")
    fun getLatestQuestionSetId(): Flow<Int>

    @Query("SELECT * FROM question_set_statistics WHERE questionSetId= :setId")
    fun getSetStatisticsById(setId: Int): Flow<QuestionSetStatistics>

    @Query("SELECT * FROM question_set_statistics")
    fun getSetsStatistics(): Flow<List<QuestionSetStatistics>>

    @Query("SELECT * FROM question_sets WHERE setId= :setId")
    fun getQuestionSetById(setId: Int): Flow<QuestionSet>

    @Query("SELECT setId FROM question_sets WHERE name = :setName")
    fun getQuestionSetIdFromName(setName: String): Flow<Int>

    @Update
    suspend fun updateQuestion(question: Question)
    @Update
    suspend fun updateAnswer(answer: Answer)

    @Update
    suspend fun updateQuestionSetStats(questionSetStatistics: QuestionSetStatistics)

    @Update
    suspend fun updateQuestionSet(questionSet: QuestionSet)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Delete
    suspend fun deleteQuestionSet(questionSet: QuestionSet)
}