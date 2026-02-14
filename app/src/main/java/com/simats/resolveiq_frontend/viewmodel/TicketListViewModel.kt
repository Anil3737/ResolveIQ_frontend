package com.simats.resolveiq_frontend.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.resolveiq_frontend.data.models.Ticket
import com.simats.resolveiq_frontend.repository.TicketRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Ticket List screen
 * Manages ticket list state and filtering
 */
class TicketListViewModel(private val ticketRepository: TicketRepository) : ViewModel() {
    
    private val _tickets = MutableLiveData<List<Ticket>>(emptyList())
    val tickets: LiveData<List<Ticket>> = _tickets
    
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _totalCount = MutableLiveData<Int>(0)
    val totalCount: LiveData<Int> = _totalCount
    
    // Current filters
    private var currentStatus: String? = null
    private var currentPriority: String? = null
    private var currentSearch: String? = null
    private var currentOffset: Int = 0
    private val pageLimit: Int = 50
    
    /**
     * Load tickets with current filters
     */
    fun loadTickets(
        status: String? = currentStatus,
        priority: String? = currentPriority,
        search: String? = currentSearch,
        refresh: Boolean = false
    ) {
        // Update filters
        currentStatus = status
        currentPriority = priority
        currentSearch = search
        
        // Reset offset if refreshing
        if (refresh) {
            currentOffset = 0
        }
        
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            val result = ticketRepository.getTickets(
                status = currentStatus,
                priority = currentPriority,
                search = currentSearch,
                limit = pageLimit,
                offset = currentOffset
            )
            
            _loading.value = false
            
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                _totalCount.value = response.total
                
                // If refreshing, replace list; otherwise append
                _tickets.value = if (refresh || currentOffset == 0) {
                    response.tickets
                } else {
                    (_tickets.value ?: emptyList()) + response.tickets
                }
                
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load tickets"
            }
        }
    }
    
    /**
     * Load more tickets (pagination)
     */
    fun loadMore() {
        if (_loading.value == true) return  // Don't load if already loading
        
        currentOffset += pageLimit
        loadTickets()
    }
    
    /**
     * Refresh tickets
     */
    fun refresh() {
        loadTickets(refresh = true)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Filter by status
     */
    fun filterByStatus(status: String?) {
        loadTickets(status = status, refresh = true)
    }
    
    /**
     * Filter by priority
     */
    fun filterByPriority(priority: String?) {
        loadTickets(priority = priority, refresh = true)
    }
    
    /**
     * Search tickets
     */
    fun searchTickets(query: String?) {
        loadTickets(search = query, refresh = true)
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        loadTickets(status = null, priority = null, search = null, refresh = true)
    }
}
