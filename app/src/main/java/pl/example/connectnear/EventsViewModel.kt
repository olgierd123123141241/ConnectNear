package pl.example.connectnear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _events = MutableStateFlow<List<FlashEvent>>(emptyList())
    val events: StateFlow<List<FlashEvent>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        loadEvents()
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var query = db.collection("flash_events").whereGreaterThan("expiresAt", System.currentTimeMillis())
                if (_selectedCategory.value != null) {
                    query = query.whereEqualTo("category", _selectedCategory.value)
                }
                val snapshot = query.get().await()
                _events.value = snapshot.toObjects(FlashEvent::class.java)
            } catch (e: Exception) {
                // Handle error
            }
            _isLoading.value = false
        }
    }
}
