package com.ask.admin

import androidx.lifecycle.viewModelScope
import com.ask.analytics.AnalyticsLogger
import com.ask.category.GetCategoryUseCase
import com.ask.common.BaseViewModel
import com.ask.common.combine
import com.ask.widget.GetWidgetsFromAiUseCase
import com.ask.widget.Widget
import com.ask.widget.WidgetWithOptionsAndVotesForTargetAudience
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AdminViewModel @Inject constructor(
    analyticsLogger: AnalyticsLogger,
    getCategoryUseCase: GetCategoryUseCase,
    private val getWidgetWithAiUseCase: GetWidgetsFromAiUseCase
) : BaseViewModel(analyticsLogger) {


    private val _categories = getCategoryUseCase()
    private val _selectedWidgetForTextToImageOption =
        MutableStateFlow<WidgetWithOptionsAndVotesForTargetAudience?>(null)

    private val _aiLoading = MutableStateFlow(false)
    private val _widgetsFromAiFlow =
        MutableStateFlow(emptyList<WidgetWithOptionsAndVotesForTargetAudience>())
    private val _aiErrorFlow = MutableStateFlow<String?>(null)
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    private val _selectedOption = MutableStateFlow<Widget.Option?>(null)
    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())

    val uiStateFlow = combine(
        _categories,
        _selectedWidgetForTextToImageOption,
        _widgetsFromAiFlow,
        _aiErrorFlow,
        _selectedImages,
        _aiLoading,
        _selectedOption,
        _selectedCategories
    ) { categories, selectedWidget, widgetsFromAi, aiError, images, aiLoading, selectedOption, selectedCategories ->
        val stringCategories =
            categories.map { categoryWithSubCategory -> listOf(categoryWithSubCategory.category.name) + categoryWithSubCategory.subCategories.map { it.title } }
                .flatten()
        AdminUiState(
            categories = stringCategories,
            selectedCategories = selectedCategories,
            selectedWidget = selectedWidget,
            loading = aiLoading,
            widgets = widgetsFromAi,
            error = aiError,
            webViewSelectedImages = images,
            selectedOption = selectedOption
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AdminUiState())


    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _selectedCategories.value =
                uiStateFlow.value.categories.shuffled().take(Random.nextInt(1, 5))
        }
    }

    fun askAI(text: String, number: Int) {
        safeApiCall({
            _aiLoading.value = true
        }, {
            _widgetsFromAiFlow.value = getWidgetWithAiUseCase(
                number,
                uiStateFlow.value.selectedCategories,
                myWords = text.takeIf { it.isNotEmpty() })
            _aiLoading.value = false
            _aiErrorFlow.value = null
        }, {
            _aiLoading.value = false
            _aiErrorFlow.value = it
            _widgetsFromAiFlow.value = emptyList()
        })
    }

    fun removeWidget(widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) {
        viewModelScope.launch {
            _widgetsFromAiFlow.value = _widgetsFromAiFlow.value.filter {
                it != widgetWithOptionsAndVotesForTargetAudience
            }
        }
    }

    fun selectWidgetForTextToImageOption(
        widget: WidgetWithOptionsAndVotesForTargetAudience?
    ) {
        _selectedWidgetForTextToImageOption.value = widget
    }

    fun onFetchImage(json: String) {
        safeApiCall({}, {
            _selectedImages.value +=
                Json.decodeFromString<List<String>>(json).also {
                    println("find image: ${it.size}")
                }.onEach {
                    println("imga>$it")
                }.mapNotNull { urlString ->
                    val decodedUrl = URLDecoder.decode(urlString, StandardCharsets.UTF_8.name())
                    val url = URL(decodedUrl)
                    val queryParams = url.query.split("&").associate {
                        val (key, value) = it.split("=")
                        key to value
                    }
                    queryParams["imgurl"]
                }.distinct()
        }, {
            _aiErrorFlow.value = it
        })
    }

    fun updateWidget(widgetWithOptionsAndVotesForTargetAudience: WidgetWithOptionsAndVotesForTargetAudience) {
        _widgetsFromAiFlow.value = _widgetsFromAiFlow.value.map {
            if (it.widget.id == widgetWithOptionsAndVotesForTargetAudience.widget.id) {
                widgetWithOptionsAndVotesForTargetAudience
            } else {
                it
            }
        }
    }

    fun onOptionSelected(option: Widget.Option) {
        _selectedOption.value = option
    }
}