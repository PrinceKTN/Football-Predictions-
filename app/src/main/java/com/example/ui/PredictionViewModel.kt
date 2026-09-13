package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BestPickNotificationDao
import com.example.data.model.BestPickNotification
import com.example.data.model.BetPrediction
import com.example.data.model.PredictionSite
import com.example.data.model.PredictionStatus
import com.example.data.model.SampleTip
import com.example.data.model.SiteCategory
import com.example.data.model.SiteStats
import com.example.data.model.SportyBetPick
import com.example.data.model.SiteAccuracyHistory
import com.example.data.repository.HistoricalAccuracyData
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiResult
import com.example.data.remote.GeminiService
import com.example.data.repository.PredictionRepository
import com.example.service.FcmNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PredictionRepository
    private val notificationDao: BestPickNotificationDao
    private val geminiService = GeminiService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PredictionRepository(db.betPredictionDao())
        notificationDao = db.bestPickNotificationDao()
        
        // Initialize FCM channels, token, and subscription
        FcmNotificationManager.init(application)

        viewModelScope.launch {
            repository.initializeDefaultPredictionsIfEmpty()
        }
    }

    // FCM Notification Flows
    val notifications: StateFlow<List<BestPickNotification>> = notificationDao.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isPushNotificationsEnabled: StateFlow<Boolean> = FcmNotificationManager.notificationsEnabledFlow
    val fcmToken: StateFlow<String> = FcmNotificationManager.fcmTokenFlow

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            notificationDao.markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationDao.clearAll()
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        FcmNotificationManager.setNotificationsEnabled(getApplication(), enabled)
    }

    fun triggerBestPickAlert(pick: SportyBetPick, isUpdate: Boolean = false) {
        FcmNotificationManager.simulateIncomingBestPickAlert(
            context = getApplication(),
            pick = pick,
            isUpdate = isUpdate
        )
    }

    fun sendTestBestPickAlert(isUpdate: Boolean = false) {
        FcmNotificationManager.simulateIncomingBestPickAlert(
            context = getApplication(),
            pick = null,
            isUpdate = isUpdate
        )
    }

    // Repository flows
    val allPredictions: StateFlow<List<BetPrediction>> = repository.allPredictions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val siteStats: StateFlow<List<SiteStats>> = repository.siteStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sitesDirectory: List<PredictionSite> = repository.analyzedSites

    // Selected site detail for inspection
    private val _selectedSite = MutableStateFlow<PredictionSite?>(null)
    val selectedSite: StateFlow<PredictionSite?> = _selectedSite.asStateFlow()

    // Filter in directory
    private val _categoryFilter = MutableStateFlow<SiteCategory?>(null)
    val categoryFilter: StateFlow<SiteCategory?> = _categoryFilter.asStateFlow()

    // SportyBet Curated Today/Tomorrow Picks
    val sportyBetPicks: List<SportyBetPick> = repository.todayAndTomorrowSportyBetPicks
    val sportyBetDayFilter = MutableStateFlow("All") // "All", "Today", "Tomorrow"

    // 30-Day Historical Accuracy Analytics
    val siteHistories: List<SiteAccuracyHistory> = HistoricalAccuracyData.siteHistories
    private val _selectedHistoricalSiteId = MutableStateFlow("all_aggregate")
    val selectedHistoricalSiteId: StateFlow<String> = _selectedHistoricalSiteId.asStateFlow()

    private val _chartViewMode = MutableStateFlow("trend") // "trend" (30-day curve) or "bars" (site comparison)
    val chartViewMode: StateFlow<String> = _chartViewMode.asStateFlow()

    fun selectHistoricalSite(siteId: String) {
        _selectedHistoricalSiteId.value = siteId
    }

    fun setChartViewMode(mode: String) {
        _chartViewMode.value = mode
    }

    // Status filter in test ledger
    private val _statusFilter = MutableStateFlow<PredictionStatus?>(null)
    val statusFilter: StateFlow<PredictionStatus?> = _statusFilter.asStateFlow()

    // Site filter in test ledger
    private val _siteFilter = MutableStateFlow<String?>(null)
    val siteFilter: StateFlow<String?> = _siteFilter.asStateFlow()

    // High Thinking Audit State (gemini-3.1-pro-preview with thinkingLevel HIGH)
    private val _thinkingAuditState = MutableStateFlow<GeminiResult?>(null)
    val thinkingAuditState: StateFlow<GeminiResult?> = _thinkingAuditState.asStateFlow()

    private val _isThinkingLoading = MutableStateFlow(false)
    val isThinkingLoading: StateFlow<Boolean> = _isThinkingLoading.asStateFlow()

    // Google Search Grounding State (gemini-3.5-flash with googleSearch tool)
    private val _searchGroundedResult = MutableStateFlow<GeminiResult?>(null)
    val searchGroundedResult: StateFlow<GeminiResult?> = _searchGroundedResult.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _activeSearchQuery = MutableStateFlow("Today free soccer predictions forebet predictz windrawwin")
    val activeSearchQuery: StateFlow<String> = _activeSearchQuery.asStateFlow()

    // Quick Tip Evaluation (gemini-3.1-flash-lite-preview)
    private val _quickTipResult = MutableStateFlow<GeminiResult?>(null)
    val quickTipResult: StateFlow<GeminiResult?> = _quickTipResult.asStateFlow()

    private val _isQuickTipLoading = MutableStateFlow(false)
    val isQuickTipLoading: StateFlow<Boolean> = _isQuickTipLoading.asStateFlow()

    // Snippet Credibility Analysis State (Gemini API)
    val snippetInput = MutableStateFlow("")
    private val _snippetCredibilityResult = MutableStateFlow<GeminiResult?>(null)
    val snippetCredibilityResult: StateFlow<GeminiResult?> = _snippetCredibilityResult.asStateFlow()

    private val _isAnalyzingSnippet = MutableStateFlow(false)
    val isAnalyzingSnippet: StateFlow<Boolean> = _isAnalyzingSnippet.asStateFlow()

    // Multi-turn Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Hello! I am your Betting Site Auditor & Prediction Analyst. Ask me to audit any subscription service (e.g. VIP packages, Telegram channels), evaluate claims against mathematical variance, or guide you on how to test free daily games without risking real money.",
                modelUsed = GeminiService.MODEL_FLASH
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _selectedChatModel = MutableStateFlow(GeminiService.MODEL_FLASH)
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    // Calculator State (Subscription Drag & Break-Even Odds)
    val unitSize = MutableStateFlow("25.0")
    val monthlySubscriptionFee = MutableStateFlow("40.0")
    val averageOdds = MutableStateFlow("1.85")
    val betsPerMonth = MutableStateFlow("60")

    fun selectSite(site: PredictionSite?) {
        _selectedSite.value = site
    }

    fun setCategoryFilter(category: SiteCategory?) {
        _categoryFilter.value = category
    }

    fun setStatusFilter(status: PredictionStatus?) {
        _statusFilter.value = status
    }

    fun setSiteFilter(siteName: String?) {
        _siteFilter.value = siteName
    }

    fun setChatModel(model: String) {
        _selectedChatModel.value = model
    }

    fun setSearchQuery(query: String) {
        _activeSearchQuery.value = query
    }

    // Prediction Ledger Operations
    fun addPrediction(
        siteId: String,
        siteName: String,
        matchTitle: String,
        league: String,
        predictionTip: String,
        marketType: String,
        odds: Double,
        stakeUnits: Double = 1.0,
        notes: String = "",
        isFreeDaily: Boolean = true
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val bet = BetPrediction(
                siteId = siteId,
                siteName = siteName,
                matchTitle = matchTitle,
                league = league,
                predictionTip = predictionTip,
                marketType = marketType,
                odds = odds,
                stakeUnits = stakeUnits,
                date = dateStr,
                status = PredictionStatus.PENDING,
                isFreeDaily = isFreeDaily,
                notes = notes
            )
            repository.insert(bet)
        }
    }

    fun addFromSampleTip(site: PredictionSite, tip: SampleTip) {
        addPrediction(
            siteId = site.id,
            siteName = site.name,
            matchTitle = tip.match,
            league = tip.league,
            predictionTip = tip.tip,
            marketType = tip.market,
            odds = tip.odds,
            stakeUnits = 1.0,
            notes = tip.rationale,
            isFreeDaily = true
        )
    }

    fun updateStatus(prediction: BetPrediction, newStatus: PredictionStatus) {
        viewModelScope.launch {
            repository.update(prediction.copy(status = newStatus))
        }
    }

    fun deletePrediction(prediction: BetPrediction) {
        viewModelScope.launch {
            repository.delete(prediction)
        }
    }

    fun addSportyBetPickToTestingLedger(pick: SportyBetPick) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        viewModelScope.launch {
            repository.insert(
                BetPrediction(
                    siteId = "sportybet_pick",
                    siteName = "SportyBet Top Pick (${pick.scheduleDay})",
                    matchTitle = pick.match,
                    league = pick.league,
                    predictionTip = "${pick.tip} (${pick.sportyBetSelection})",
                    marketType = pick.sportyBetMarketName,
                    odds = pick.estimatedOdds,
                    stakeUnits = 1.0,
                    date = currentDate,
                    status = PredictionStatus.PENDING,
                    isFreeDaily = true,
                    notes = "Source: ${pick.algorithmicSource}. Market: ${pick.sportyBetMarketName} -> ${pick.sportyBetSelection}"
                )
            )
        }
    }

    // Gemini High Thinking Audit (gemini-3.1-pro-preview, thinkingLevel HIGH)
    fun runDeepThinkingAudit(prompt: String) {
        viewModelScope.launch {
            _isThinkingLoading.value = true
            _thinkingAuditState.value = null
            val result = geminiService.analyzeWithHighThinking(prompt)
            _thinkingAuditState.value = result
            _isThinkingLoading.value = false
        }
    }

    fun auditSiteWithHighThinking(site: PredictionSite) {
        val auditPrompt = """
            Perform a rigorous deep mathematical audit on the betting prediction service: ${site.name}.
            Category: ${site.category.title}
            Pricing: ${site.pricingModel.label} (Monthly fee: $${site.monthlyCostUsd})
            Claimed Win Rate: ${site.claimedWinRate}
            Reported Verified Yield: ${site.verifiedYieldRoi}
            Algorithm / Methodology: ${site.predictionMethodology}
            
            Audit Requirements:
            1. Mathematical Feasibility: Can the claimed win rate be sustained at their typical odds? Calculate the required break-even strike rate.
            2. Subscription Drag: How severely does a $${site.monthlyCostUsd}/month subscription fee impair a retail bankroll (e.g. $500 vs $2,000)?
            3. Red Flags & Proofing: What standard verification or third-party proofing (e.g., Blogabet/Tipstrr style) is missing or present?
            4. Free Testing Protocol: Exact statistical method a bettor should use to taste and test their daily free games over 30 days before ever paying a subscription fee.
        """.trimIndent()
        runDeepThinkingAudit(auditPrompt)
    }

    // Gemini Google Search Grounding (gemini-3.5-flash with googleSearch tool)
    fun runSearchGroundedQuery(query: String = _activeSearchQuery.value) {
        viewModelScope.launch {
            _isSearchLoading.value = true
            _searchGroundedResult.value = null
            _activeSearchQuery.value = query
            val prompt = "Search Google for: $query. Provide the latest daily free predictions from top sites (like Forebet, PredictZ, Windrawwin), current odds, match rationale, and any recent community alerts or accuracy reports."
            val result = geminiService.searchGroundedQuery(prompt)
            _searchGroundedResult.value = result
            _isSearchLoading.value = false
        }
    }

    // Quick Tip Evaluator (gemini-3.1-flash-lite-preview)
    fun quickEvaluateTip(prediction: BetPrediction) {
        viewModelScope.launch {
            _isQuickTipLoading.value = true
            _quickTipResult.value = null
            val tipDetails = "${prediction.matchTitle} (${prediction.league}) - Tip: ${prediction.predictionTip} @ Odds ${prediction.odds} (Market: ${prediction.marketType}). Source: ${prediction.siteName}"
            val result = geminiService.quickEvaluateTip(tipDetails)
            _quickTipResult.value = result
            _isQuickTipLoading.value = false
        }
    }

    // Snippet Credibility Analysis (Gemini API)
    fun analyzeSnippet(snippet: String = snippetInput.value) {
        if (snippet.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingSnippet.value = true
            _snippetCredibilityResult.value = null
            snippetInput.value = snippet
            val result = geminiService.analyzeSnippetCredibility(snippet)
            _snippetCredibilityResult.value = result
            _isAnalyzingSnippet.value = false
        }
    }

    fun clearSnippetAnalysis() {
        snippetInput.value = ""
        _snippetCredibilityResult.value = null
    }

    // Multi-turn Chat
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(role = "user", text = userText)
        val currentList = _chatMessages.value + userMsg
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isChatLoading.value = true
            val model = _selectedChatModel.value
            val result = geminiService.chatTurn(
                history = currentList.dropLast(1),
                userMessage = userText,
                model = model
            )

            val modelMsg = ChatMessage(
                role = "model",
                text = result.text,
                searchSources = result.searchSources,
                modelUsed = model
            )
            _chatMessages.value = _chatMessages.value + modelMsg
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                role = "model",
                text = "Chat history cleared. How can I help you analyze prediction sites or test daily free picks today?",
                modelUsed = _selectedChatModel.value
            )
        )
    }
}
