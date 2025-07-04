@file:OptIn(ExperimentalMaterial3Api::class)

package com.tazzix.citysearch

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tazzix.citysearch.api.NetworkModule
import com.tazzix.citysearch.ui.theme.CitySearchTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitySearchTheme {
                SearchScreen(viewModel = viewModel())
            }
        }
    }
}

// ViewModel to handle API calls and UI state
class CitySearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchCities() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _cities.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = NetworkModule.geoNamesApiService.searchCities(
                    nameStartsWith = query
                )
                
                _cities.value = response.geonames.map { geoName ->
                    City(
                        name = geoName.name.uppercase(),
                        region = geoName.adminName1,
                        country = geoName.countryName
                    )
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: Resources.getSystem().getString(R.string.general_error)
                _isLoading.value = false
                _cities.value = emptyList()
            }
        }
    }
}

data class City(
    val name: String,
    val region: String,
    val country: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: CitySearchViewModel = viewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.Home, contentDescription = stringResource(id = R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A90E2)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(id = R.string.search_hint)) },
                    singleLine = true,
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { viewModel.searchCities() },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search_button),
                        tint = Color(0xFF4A90E2)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFF4A90E2)
                )
            }
            
            // Error message
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // City listings
            LazyColumn {
                items(cities) { city ->
                    CityItem(city)
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun CityItem(city: City) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = city.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = city.region,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = city.country,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    CitySearchTheme {
        SearchScreen()
    }
}