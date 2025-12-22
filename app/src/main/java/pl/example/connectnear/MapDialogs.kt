package pl.example.connectnear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FourthStageFriendRequestDialog(
    senderName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Celowo puste */ },
        title = { Text("Zaproszenie do znajomych") },
        text = { Text("Użytkownik $senderName chce dodać Cię do znajomych.") },
        confirmButton = { Button(onClick = onAccept) { Text("Akceptuj") } },
        dismissButton = { Button(onClick = onReject) { Text("Odrzuć") } }
    )
}

@Composable
fun FourthStageSettingsDialog(
    isSharingLocation: Boolean, onSharingChange: (Boolean) -> Unit,
    tempCategory: String, onCategoryChange: (String) -> Unit,
    expandedCategory: Boolean, onExpandedCategoryChange: (Boolean) -> Unit,
    tempVisibilityMode: String, onVisibilityChange: (String) -> Unit,
    tempInterval: Long, onIntervalChange: (Long) -> Unit,
    expandedIntervals: Boolean, onExpandedChange: (Boolean) -> Unit,
    intervalNames: Map<Long, String>, intervals: Map<String, Long>,
    tempRadius: Double, onRadiusChange: (Double) -> Unit,
    expandedRadius: Boolean, onExpandedRadiusChange: (Boolean) -> Unit,
    radiuses: List<Double>,

    tempPreferredAge: String, onPreferredAgeChange: (String) -> Unit,
    expandedPreferredAge: Boolean, onExpandedPreferredAgeChange: (Boolean) -> Unit,
    tempPreferredSex: String, onPreferredSexChange: (String) -> Unit,
    expandedPreferredSex: Boolean, onExpandedPreferredSexChange: (Boolean) -> Unit,

    tempSportMode: String, onSportModeChange: (String) -> Unit,
    tempSportLevel: String, onSportLevelChange: (String) -> Unit,
    tempSubCategory: String, onSubCategoryChange: (String) -> Unit,

    tempLearningMode: String, onLearningModeChange: (String) -> Unit,
    tempSubject: String, onSubjectChange: (String) -> Unit,
    tempLearningLevel: String, onLearningLevelChange: (String) -> Unit,

    tempDateMode: String, onDateModeChange: (String) -> Unit,
    tempDatePartnerGender: String, onDatePartnerGenderChange: (String) -> Unit,
    tempDateCoupleGender: String, onDateCoupleGenderChange: (String) -> Unit,
    tempDateAnimalType: String, onDateAnimalTypeChange: (String) -> Unit,
    tempDateOtherAnimal: String, onDateOtherAnimalChange: (String) -> Unit,

    tempPartyType: String, onPartyTypeChange: (String) -> Unit,

    onClose: () -> Unit
) {
    val categories = listOf("Impreza", "Sport", "Nauka", "Spacer", "Kawa", "Wspólna gra", "Randka")
    val preferredAgeOptions = listOf("18-25", "26-35", "36-45", "46-100", "Bez znaczenia")
    val sexOptions = listOf("Kobieta", "Mężczyzna", "Wszyscy")
    val sportModes = listOf("Partner do ćwiczeń", "Trener personalny")
    val sportLevels = listOf("Dla fanu", "Początkujący", "Średniozaawansowany", "Zaawansowany")
    
    val sportSubCategories = listOf(
        "Sporty walki", 
        "Siłowy", 
        "Wytrzymałościowy", 
        "Cardio / HIIT", 
        "Funkcjonalny / Crossfit", 
        "Elastyczność / Joga / Pilates"
    )
    
    val learningModes = listOf("Korepetycje", "Towarzysz do nauki", "Po prostu lubię się uczyć")
    val learningLevels = listOf("Szkoła podstawowa", "Szkoła średnia", "Studia", "Ogólny")

    val dateModes = listOf("Pojedyncza", "Para", "Zwierzę")
    val dateGenders = listOf("Kobieta", "Mężczyzna", "Obojętnie")
    val dateAnimalTypes = listOf("Pies", "Kot", "Inne")
    
    val partyTypes = listOf("Domówka", "Plener", "Klub")


    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Ustawienia Mapy i Filtry") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Ustawienia Główne", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Udostępniaj lokalizację:", fontWeight = FontWeight.Bold)
                    Switch(checked = isSharingLocation, onCheckedChange = onSharingChange)
                }
                 Text(
                    if (isSharingLocation) "Jesteś widoczny na mapie" else "Tryb Ducha (jesteś niewidoczny)",
                    fontSize = 12.sp,
                    color = if (isSharingLocation) Color(0xFF006400) else Color.Red
                )
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text("Filtry Wyszukiwania", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Szukaj kategorii:", fontWeight = FontWeight.Bold)
                SettingDropDown(text = if (tempCategory.isEmpty()) "Wszystkie" else tempCategory, expanded = expandedCategory, onExpandedChange = onExpandedCategoryChange) {
                    DropdownMenuItem(text = { Text("Wszystkie") }, onClick = { onCategoryChange(""); onExpandedCategoryChange(false) })
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { onCategoryChange(cat); onExpandedCategoryChange(false) })
                    }
                }

                AnimatedVisibility(visible = tempCategory == "Impreza") {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Typ imprezy:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FilterRadioButtons(options = partyTypes, selected = tempPartyType, onSelect = { onPartyTypeChange(if(it == tempPartyType) "" else it) })
                    }
                }
                
                AnimatedVisibility(visible = tempCategory == "Randka") {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Tryb randki:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FilterRadioButtons(options = dateModes, selected = tempDateMode, onSelect = { onDateModeChange(if(it == tempDateMode) "" else it) })
                        
                        AnimatedVisibility(visible = tempDateMode == "Pojedyncza") {
                            Column {
                                Text("Szukana płeć:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = dateGenders, selected = tempDatePartnerGender, onSelect = { onDatePartnerGenderChange(if(it == tempDatePartnerGender) "" else it) })
                            }
                        }
                        
                        AnimatedVisibility(visible = tempDateMode == "Para") {
                            Column {
                                Text("Szukana płeć pary:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = dateGenders, selected = tempDateCoupleGender, onSelect = { onDateCoupleGenderChange(if(it == tempDateCoupleGender) "" else it) })
                            }
                        }
                        
                        AnimatedVisibility(visible = tempDateMode == "Zwierzę") {
                             Column {
                                Text("Płeć osoby:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = dateGenders, selected = tempDatePartnerGender, onSelect = { onDatePartnerGenderChange(if(it == tempDatePartnerGender) "" else it) })
                                
                                Text("Jakie zwierzę?", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = dateAnimalTypes, selected = tempDateAnimalType, onSelect = { onDateAnimalTypeChange(if(it == tempDateAnimalType) "" else it) })
                                
                                AnimatedVisibility(visible = tempDateAnimalType == "Inne") {
                                    OutlinedTextField(value = tempDateOtherAnimal, onValueChange = onDateOtherAnimalChange, label = {Text("Jakie inne zwierzę?")}, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = tempCategory == "Sport") {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Szukam:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FilterRadioButtons(options = sportModes, selected = tempSportMode, onSelect = { onSportModeChange(if(it == tempSportMode) "" else it) })

                        AnimatedVisibility(visible = tempSportMode.isNotEmpty()) {
                             Column {
                                Text("Poziom:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = sportLevels, selected = tempSportLevel, onSelect = { onSportLevelChange(if(it == tempSportLevel) "" else it) })
                            }
                        }
                        AnimatedVisibility(visible = tempSportLevel.isNotEmpty()) {
                             Column {
                                Text("Podkategoria:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                FilterRadioButtons(options = sportSubCategories, selected = tempSubCategory, onSelect = { onSubCategoryChange(if(it == tempSubCategory) "" else it) })
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = tempCategory == "Nauka") {
                     Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text("Tryb:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FilterRadioButtons(options = learningModes, selected = tempLearningMode, onSelect = { onLearningModeChange(if(it == tempLearningMode) "" else it) })
                        
                        AnimatedVisibility(visible = tempLearningMode.isNotEmpty()) {
                            Column {
                                if (tempLearningMode == "Po prostu lubię się uczyć") {
                                    OutlinedTextField(value = tempSubject, onValueChange = onSubjectChange, label = {Text("Zainteresowania (np. Historia, Fizyka)")}, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                                } else {
                                    Column {
                                        Text("Poziom:", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                                        FilterRadioButtons(options = learningLevels, selected = tempLearningLevel, onSelect = { onLearningLevelChange(if(it == tempLearningLevel) "" else it) })
                                        
                                        AnimatedVisibility(visible = tempLearningLevel.isNotEmpty()){
                                            OutlinedTextField(value = tempSubject, onValueChange = onSubjectChange, label = {Text("Przedmiot")}, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text("Szukany wiek:", fontWeight = FontWeight.Bold)
                SettingDropDown(text = tempPreferredAge.ifEmpty { "Wybierz" }, expanded = expandedPreferredAge, onExpandedChange = onExpandedPreferredAgeChange) {
                    preferredAgeOptions.forEach { age ->
                        DropdownMenuItem(text = { Text(age) }, onClick = { onPreferredAgeChange(age); onExpandedPreferredAgeChange(false) })
                    }
                }

                Text("Szukana płeć:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=8.dp))
                SettingDropDown(text = tempPreferredSex.ifEmpty { "Wybierz" }, expanded = expandedPreferredSex, onExpandedChange = onExpandedPreferredSexChange) {
                    sexOptions.forEach { sex ->
                        DropdownMenuItem(text = { Text(sex) }, onClick = { onPreferredSexChange(sex); onExpandedPreferredSexChange(false) })
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text("Ustawienia Techniczne", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Promień szukania:", fontWeight = FontWeight.Bold)
                SettingDropDown(text = "${tempRadius.toInt()} km", expanded = expandedRadius, onExpandedChange = onExpandedRadiusChange) {
                    radiuses.forEach { radius ->
                        DropdownMenuItem(text = { Text("${radius.toInt()} km") }, onClick = { onRadiusChange(radius); onExpandedRadiusChange(false) })
                    }
                }

                Text("Odświeżanie GPS:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=8.dp))
                SettingDropDown(text = intervalNames[tempInterval] ?: "Wybierz", expanded = expandedIntervals, onExpandedChange = onExpandedChange, enabled = isSharingLocation) {
                    intervals.forEach { (name, value) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = { onIntervalChange(value); onExpandedChange(false) })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onClose) { Text("Zapisz i Zamknij") } }
    )
}

@Composable
private fun FilterRadioButtons(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        options.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelect(option) }) {
                RadioButton(selected = selected == option, onClick = { onSelect(option) })
                Text(option, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SettingDropDown(text: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, enabled: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Box {
        Button(onClick = { onExpandedChange(true) }, modifier = Modifier.fillMaxWidth(), enabled = enabled) { Text(text) }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) { content() }
    }
}
