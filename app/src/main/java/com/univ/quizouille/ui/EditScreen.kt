@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.quizouille.model.Answer
import com.univ.quizouille.model.Question
import com.univ.quizouille.model.QuestionSet
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel


/**
 * Permet de construire une bordure avec une couleur differente pour décorer un Text()
 * si il à été selectionné par l'utilisateur
 * @param idSelected l'id du set sélectionnée
 * @param itemId l'id de l'item qui appelle cette méthode
 * @return la couleur correspondant au status du Text(), Noir si selectionné, Gris sinon
 * */
fun getBorderColor(idSelected: Int, itemId: Int): Color {
    if (idSelected == itemId) {
        return Color.Black
    } else {
        return Color.Gray
    }
}

/**
 * Permet de construire une bordure plus épaisse pour décorer un Text()
 * si il à été selectionné par l'utilisateur
 * @param idSelected l'id du set sélectionnée
 * @param itemId l'id de l'item qui appelle cette méthode
 * @return l'épaisseur correspondant au status du Text(), 3 si selectionné, 1 sinon
 * */
fun getBorderWidth(idSelected: Int, itemId: Int): Dp {
    if (idSelected == itemId) {
        return 3.dp
    } else {
        return 1.dp
    }
}

/**
 * Vérifie si la réponse créée par l'utilisateur peut être considérée comme valide,
 * une liste de réponses est valide si au moins une des réponses est définie comme correcte
 * et est une chaîne non vide
 * @param answers la liste des chaînes qui représenteront les différentes réponses dans la base de données
 * @param answerCorrect la liste des Booleans qui représenteront le fait que les réponses dans la BDD soit correcte ou non
 * @return vrai si les réponses sont valides faux sinon
 * */
fun areAnswersValid(answers: List<String>, answerCorrect: List<Boolean>): Boolean {
    for (i in 0..<4) {
        if (!answers[i].equals("") && answerCorrect[i]) {
            return true
        }
    }
    return false
}

/**
 * EditScreen est décomposé en 4 menus pour les 4 tâches d'éditions que notre application propose:
 *  -Ajouter une nouvelle question avec un nombre de réponses variables à un set de question existant de la BDD
 *   ou bien même d'en créer un nouveau en même temps,
 *  -Suppimer un set de question de la BDDainsi que toutes les questions qui y sont attachées,
 *  -Supprimer une question de la BDD ainsi que toutes les réponses qui y sont attachées,
 *  -Modifier un question déjà existante ainsi que les réponses qui lui sont rattachées
 * Ces 4 menus sont découpés en 4 méthodes composable qui sont appelées en fonction des intérractions avec l'interface
 * */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun EditScreen(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState)
{
    // Used to chose edit action
    var addNewQuestion by remember { mutableStateOf(false) }
    var deleteSet by remember { mutableStateOf(false) }
    var deleteQuestion by remember { mutableStateOf(false) }
    var modifyQuestion by remember { mutableStateOf(false)}

    // Used to add new question
    var setId by remember { mutableIntStateOf(-1) }
    var newSetName by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer = remember { mutableStateListOf("", "", "", "") }
    var answerCorrect = remember { mutableStateListOf(true, false, false, false) }
    var newSet by remember { mutableStateOf(false) }

    val lastQuestionSet by gameViewModel.lastSetInsertedIdFlow.collectAsState(initial = -1)
    val lastQuestion by gameViewModel.lastQuestionInsertedIdFlow.collectAsState(initial = -1)

    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val allQuestionSets: List<QuestionSet> by gameViewModel.allQuestionSetsFlow.collectAsState(initial = mutableListOf())
    val allQuestions: List<Question> by gameViewModel.allQuestionsFlow.collectAsState(initial = mutableListOf())
    val allAnswers: List<Answer> by gameViewModel.allAnswersFlow.collectAsState(initial = mutableListOf())

    /**
     * Cette méthode comme son nom l'indique permet de mettre sur l'écran de l'appli tout ce qui est necessaire
     * pour récupérer de quoi ajouter une nouvelle question dans la BDD ainsi qu'un nouveau set.
     * */
    @Composable
    fun addNewQuestionScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = {
                    addNewQuestion = false
                    setId = -1
                }) {
                    Text(text = "Retour", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.3f)
            ) {
                if (newSet) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = newSetName,
                            onValueChange = { newSetName = it },
                            label = {
                                Text(text = "Nouveau Jeu")
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(allQuestionSets) {
                                Text(
                                    text = it.name,
                                    fontSize = policeSize.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier
                                        .clickable {
                                            if (setId != it.setId) {
                                                setId = it.setId
                                            } else {
                                                setId = -1
                                            }
                                        }
                                        .border(
                                            width = getBorderWidth(setId, it.setId),
                                            color = getBorderColor(setId, it.setId),
                                            shape = RoundedCornerShape(10)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        newSet = !newSet
                        newSetName = ""
                        setId = -1
                    }) {
                        if (newSet) {
                            Image(Icons.Outlined.Clear, contentDescription = "cancel create set")
                        } else {
                            Image(Icons.Outlined.Add, contentDescription = "create set")
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(text = "question") }
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Switch à activé si réponse correcte",
                    fontSize = policeTitleSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            for (index in 0..<4) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    OutlinedTextField(
                        value = answer[index],
                        onValueChange = { answer[index] = it },
                        label = {
                            if (index > 0) {
                                Text(text = "Réponse (Optionel)")
                            } else {
                                Text(text = "Réponse")
                            }
                        }
                    )
                    Switch(
                        checked = answerCorrect[index],
                        onCheckedChange = {
                            if (!it) {
                                for (i in 0..<4) {
                                    if (i != index && answerCorrect[i]) {
                                        answerCorrect[index] = false
                                        break;
                                    }
                                }
                            } else {
                                answerCorrect[index] = true
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp)
            ) {
                Button(onClick = {
                    if ((setId != -1 || !newSetName.equals("")) && (areAnswersValid(
                            answer,
                            answerCorrect
                        ) && !question.equals(""))
                    ) {
                        if (setId == -1) {
                            gameViewModel.insertQuestionSet(newSetName)
                            if (lastQuestionSet != -1) {
                                setId = lastQuestionSet
                            }
                        }
                        Log.d("test", "setId: $setId && question = $question")
                        gameViewModel.insertQuestion(setId = setId, question = question)
                        var questionId = lastQuestion + 1
                        for (i in 0..<4) {
                            if (!answer[i].equals("")) {
                                gameViewModel.insertAnswer(
                                    questionId = questionId,
                                    answer = answer[i],
                                    correct = answerCorrect[i]
                                )
                            }
                        }
                        question = ""
                        setId = -1
                        newSetName = ""
                        newSet = false
                        for (i in 0..<4) {
                            answer[i] = ""
                            answerCorrect[i] = false
                        }
                        answerCorrect[0] = true
                    }
                }) {
                    Text(text = "Insérer", Modifier.padding(1.dp))
                }
            }
        }
    }
    /**
     * Cette méthode offre le choix à l'utilisateur de selectionner un set puis de le supprimer de la BDD
     * avec tout ce qui lui faisait référence
     * */
    @Composable
    fun deleteSetScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = {
                    deleteSet = false
                    setId = -1
                }) {
                    Text(text = "Retour", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.8f)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(allQuestionSets) {
                        Text(
                            text = it.name,
                            fontSize = policeSize.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clickable {
                                    if (setId != it.setId) {
                                        setId = it.setId
                                    } else {
                                        setId = -1
                                    }
                                }
                                .border(
                                    width = getBorderWidth(setId, it.setId),
                                    color = getBorderColor(setId, it.setId),
                                    shape = RoundedCornerShape(10)
                                )
                                .padding(vertical = 10.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    if (setId != -1) {
                        gameViewModel.deleteQuestionSet(setId = setId)
                        setId = -1
                    }
                }) {
                    Text(text = "Supprimer le jeu", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    /**
     * Cette méthode offre le choix à l'utilisateur de selectionner une question puis de la supprimer de la BDD
     * avec toute les réponses qui lui faisait référence
     * */
    @Composable
    fun deleteQuestionScreen() {
        var questionId by remember { mutableStateOf(-1) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 25.dp)
            ) {
                Button(onClick = { deleteQuestion = false }) {
                    Text(text = "Retour", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.8f)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(allQuestions) {
                        Text(
                            text = it.content,
                            fontSize = policeSize.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clickable {
                                    if (questionId != it.questionId) {
                                        questionId = it.questionId
                                    } else {
                                        questionId = -1
                                    }
                                }
                                .border(
                                    width = getBorderWidth(questionId, it.questionId),
                                    color = getBorderColor(questionId, it.questionId),
                                    shape = RoundedCornerShape(10)
                                )
                                .padding(vertical = 10.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    if (questionId != -1) {
                        gameViewModel.deleteQuestion(questionId = questionId)
                        questionId = -1
                    }
                }) {
                    Text(text = "Supprimer la question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    /**
     * Cette méthode permet de completement modifier une réponse existante dans la BDD,
     * elle fait confiance à l'utilisateur plainnement pour que la nouvelle question créée soit cohérente
     * */
    @Composable
    fun modifyQuestionScreen() {
        var questionId by remember { mutableStateOf(-1) }
        var showAnswers by remember { mutableStateOf(false) }

        var answerId by remember { mutableStateOf(-1) }
        var questionStatus by remember { mutableStateOf(-1)}

        var newAnswerContent by remember { mutableStateOf("") }
        var newAnswerCorrect by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .padding(vertical = 25.dp)
            ) {
                Button(onClick = { 
                    questionId = -1
                    if (showAnswers) {
                        showAnswers = false
                    } else {
                        modifyQuestion = false
                    }
                }) {
                    Text(text = "Retour", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (showAnswers) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxHeight(0.4f)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier
                        .align(alignment = Alignment.CenterVertically)) {
                        items(allAnswers.filter{ it.questionId == questionId }) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = it.answer,
                                    fontSize = policeSize.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier
                                        .clickable {
                                            if (answerId != it.answerId) {
                                                newAnswerContent = it.answer
                                                newAnswerCorrect = it.correct
                                                answerId = it.answerId
                                            } else {
                                                newAnswerContent = ""
                                                newAnswerCorrect = false
                                                answerId = -1
                                            }
                                        }
                                        .border(
                                            width = getBorderWidth(answerId, it.answerId),
                                            color = getBorderColor(answerId, it.answerId),
                                            shape = RoundedCornerShape(10)
                                        )
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(horizontal = 5.dp)
                        .align(alignment = Alignment.CenterVertically)) {
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it }
                        )
                    }
                    Column(modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .align(alignment = Alignment.CenterVertically)) {
                        Text(
                            text = questionStatus.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(10)
                                )
                                .padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                        Button(modifier = Modifier.align(alignment = Alignment.CenterHorizontally), onClick = {
                            questionStatus++
                        }) {
                            Image(Icons.Outlined.KeyboardArrowUp, contentDescription = "increment status")
                        }
                        Button(modifier = Modifier.align(alignment = Alignment.CenterHorizontally), onClick = {
                            if (questionStatus > 1) { questionStatus-- }
                        }) {
                            Image(Icons.Outlined.KeyboardArrowDown, contentDescription = "decrement status")
                        }
                    }

                }
                if (answerId != -1) {
                    Row(modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(vertical = 25.dp),
                        horizontalArrangement = Arrangement.Center)
                    {
                        Column(modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 5.dp)
                            .align(alignment = Alignment.CenterVertically)) {
                            OutlinedTextField(value = newAnswerContent, onValueChange = {newAnswerContent = it})
                        }
                        Column(modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .padding(horizontal = 5.dp)
                            .align(alignment = Alignment.CenterVertically)) {
                            Switch(checked = newAnswerCorrect, onCheckedChange = {newAnswerCorrect = it})
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        if (questionId != -1) {
                            gameViewModel.updateQuestion(questionId, question, questionStatus)
                            if (answerId != -1) {
                                gameViewModel.updateAnswer(answerId, newAnswerContent, newAnswerCorrect)
                                answerId = -1
                                newAnswerContent = ""
                                newAnswerCorrect = false
                                showAnswers = false
                            }
                            questionId = -1
                            question = ""
                            questionStatus = -1
                        }
                    }) {
                        Text(text = "Modifier", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxHeight(0.5f)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        items(allQuestions) {
                            Text(
                                text = it.content,
                                fontSize = policeSize.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .clickable {
                                        if (questionId != it.questionId) {
                                            questionId = it.questionId
                                            question = it.content
                                            questionStatus = it.status
                                        } else {
                                            questionId = -1
                                            question = ""
                                            questionStatus = -1
                                        }
                                    }
                                    .border(
                                        width = getBorderWidth(questionId, it.questionId),
                                        color = getBorderColor(questionId, it.questionId),
                                        shape = RoundedCornerShape(10)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 15.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 25.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        if (questionId != -1) {
                            showAnswers = true
                        }
                    }) {
                        Text(text = "Afficher les réponses pour la question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        // si l'une des ces variables est vrai on affiche le Screen necessaire pour réaliser la tâche demandé
        if (addNewQuestion || deleteSet || deleteQuestion || modifyQuestion) {
            if (addNewQuestion) {
                addNewQuestionScreen()
            } else if (deleteSet) {
                deleteSetScreen()
            } else if (deleteQuestion) {
                deleteQuestionScreen()
            } else {
                modifyQuestionScreen()
            }
        } else { // sinon on affiche le menu des choix pour le sdifférentes tâches d'edition de la BDD qu'offre notre appli
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            )
            {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {addNewQuestion = true}) {
                        Text(text = "Ajouter une question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {deleteSet = true}) {
                        Text(text = "Supprimer un jeu", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {deleteQuestion = true}) {
                        Text(text = "Supprimer une question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 25.dp)
                )
                {
                    Button(onClick = {modifyQuestion = true}) {
                        Text(text = "Modifier une question", fontSize = policeSize.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // envoie des snackbars quand une entrée dans la BD n'a pas marché
        LaunchedEffect(gameViewModel.snackBarMessage) {
            if (gameViewModel.snackBarMessage.isNotEmpty()) {
                snackbarHostState.showSnackbar(gameViewModel.snackBarMessage)
                gameViewModel.snackBarMessage = ""
            }
        }
    }
}

