package com.univ.quizouille.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["questionId"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["answer", "questionId"], unique = true)]
    )
data class Answer (
    @PrimaryKey(autoGenerate = true)
    val answerId: Int = 0,
    val questionId: Int,
    val answer: String,
    val correct: Boolean
)