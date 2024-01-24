package com.univ.quizouille.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_sets")
data class QuestionSet(
    @PrimaryKey(autoGenerate = true)
    var setId: Int = 0,
    var name: String
)
