package com.univ.quizouille.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "question_set_statistics",
    foreignKeys = [
        ForeignKey(
            entity = QuestionSet::class,
            parentColumns = ["setId"],
            childColumns = ["questionSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["questionSetId"], unique = true)])
data class QuestionSetStatistics(
    @PrimaryKey
    val questionSetId: Int,
    var correctCount: Int = 0,
    var totalAsked: Int = 0,
    var lastTrainedDate: String = ""
)
