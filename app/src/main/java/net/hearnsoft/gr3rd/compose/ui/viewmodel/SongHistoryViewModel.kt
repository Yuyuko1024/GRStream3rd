package net.hearnsoft.gr3rd.compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.domain.beans.OnlineSongHistoryData
import net.hearnsoft.gr3rd.compose.infrastructure.repository.GRStationApiRepository

class SongHistoryViewModel: ViewModel() {

    private val repository = GRStationApiRepository()

    private val _songList = MutableStateFlow<List<OnlineSongHistoryData>>(emptyList())
    val songList: StateFlow<List<OnlineSongHistoryData>> = _songList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadOnlineHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.fetchOnlineSongHistory()
                result.fold(
                    onSuccess = { historyData ->
                        _songList.value = historyData
                    },
                    onFailure = { error ->
                        // 处理错误情况
                        _songList.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                // 处理异常
            } finally {
                _isLoading.value = false
            }
        }
    }

}