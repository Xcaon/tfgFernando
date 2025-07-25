package com.example.data.classes

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class FormData(
    val chronicDiseases: SnapshotStateList<String> = mutableStateListOf(),
    val mobilityProblems: Boolean = false,
    val objectives: SnapshotStateList<String> = mutableStateListOf(),
    val exercisedRecently: Boolean = false,
    val weight: String = "70",
    val age: String = "40",
    val altura : String = "170",
    val pasos: String = "1000",
    val distancia: String = "1000",
    val calories: String = "1000"
) {
    fun tieneCamposVacio(): Boolean {
        var valor = chronicDiseases.isEmpty()
                || objectives.isEmpty()
                || weight.isEmpty()
                || weight.length > 3
                || age.isEmpty()
                || age.length > 2
                || altura.isEmpty()
                || altura.length > 3
//                || mobilityProblems == false
//                || exercisedRecently == false
//                || pasos.isEmpty()
//                || distancia.isEmpty()
//                || calories.isEmpty()

        return valor
    }
}