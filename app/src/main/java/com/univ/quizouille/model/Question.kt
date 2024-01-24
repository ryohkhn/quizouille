package com.univ.quizouille.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuestionSet::class,
            parentColumns = ["setId"],
            childColumns = ["questionSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["content", "questionSetId"], unique = true)])
data class Question(
    @PrimaryKey(autoGenerate = true)
    val questionId: Int = 0,
    val questionSetId: Int,
    val content: String,
    var status: Int = 1,
    var lastShownDate: String = ""
)
