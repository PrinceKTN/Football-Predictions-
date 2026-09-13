package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiService
import com.example.ui.PredictionViewModel
import com.example.ui.components.SnippetCredibilityAuditorCard
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin
import java.util.Locale

@Composable
fun ThinkingAndChatScreen(
    viewModel: PredictionViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("High Thinking Audit", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gemini Chatbot", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            HighThinkingAuditContent(viewModel = viewModel)
        } else {
            GeminiChatbotContent(viewModel = viewModel)
        }
    }
}

@Composable
fun HighThinkingAuditContent(viewModel: PredictionViewModel) {
    val auditResult by viewModel.thinkingAuditState.collectAsState()
    val isLoading by viewModel.isThinkingLoading.collectAsState()

    var customPrompt by remember { mutableStateOf("") }

    // Calculator state
    var unitSize by remember { mutableStateOf("25.0") }
    var monthlyFee by remember { mutableStateOf("45.0") }
    var avgOdds by remember { mutableStateOf("1.90") }
    var betsPerMonth by remember { mutableStateOf("60") }

    // Math calculations
    val uSize = unitSize.toDoubleOrNull() ?: 25.0
    val mFee = monthlyFee.toDoubleOrNull() ?: 45.0
    val odds = avgOdds.toDoubleOrNull() ?: 1.90
    val bets = betsPerMonth.toIntOrNull() ?: 60

    val breakEvenWinRate = if (odds > 1.0) (1.0 / odds) * 100.0 else 50.0
    val feeInUnits = if (uSize > 0.0) mFee / uSize else 0.0
    val requiredMonthlyEdgeUnits = feeInUnits
    val requiredRoiPercent = if (bets > 0 && uSize > 0) (feeInUnits / bets) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("thinking_audit_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberVIP.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AmberVIP, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "High Thinking Mathematical Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "gemini-3.1-pro-preview (ThinkingLevel.HIGH)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Uses advanced mathematical reasoning to expose subscription fee traps, calculate true break-even thresholds, model variance, and audit whether any prediction service is genuinely beating the closing line.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Snippet Credibility Auditor
        item {
            SnippetCredibilityAuditorCard(viewModel = viewModel, initiallyExpanded = true)
        }

        // Interactive Subscription Drag Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = CyanOdds)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Interactive Subscription Drag Calculator",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unitSize,
                            onValueChange = { unitSize = it },
                            label = { Text("Your Bet Size ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = monthlyFee,
                            onValueChange = { monthlyFee = it },
                            label = { Text("VIP Fee ($/mo)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = avgOdds,
                            onValueChange = { avgOdds = it },
                            label = { Text("Average Odds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = betsPerMonth,
                            onValueChange = { betsPerMonth = it },
                            label = { Text("Bets / Month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculator Results Grid
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Break-Even Strike Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format(Locale.US, "%.1f", breakEvenWinRate)}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Fee in Units", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format(Locale.US, "%.2f", feeInUnits)} units/mo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberVIP)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Required ROI Just To Break Even", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+${String.format(Locale.US, "%.2f", requiredRoiPercent)}% Yield", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CrimsonLoss)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mathematical Reality: At a $uSize unit size, you must win ${String.format(Locale.US, "%.1f", feeInUnits)} net units every month JUST to pay the tipster subscription before keeping a single penny.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val prompt = "Perform a high-thinking mathematical breakdown of subscription drag: Bet unit $uSize, Monthly VIP fee $mFee, Average Odds $odds, $bets bets/month. Explain the mathematics of variance, probability of a 10-bet losing streak, and why free daily games should be tested before paying."
                            viewModel.runDeepThinkingAudit(prompt)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_audit_calculator_math")
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audit This Bankroll with High Thinking")
                    }
                }
            }
        }

        // Preset Deep Audit Topics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Popular High-Thinking Audit Queries:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                val presets = listOf(
                    "Why 90% Win Rate Claims Are Mathematically Impossible",
                    "How to Audit Third-Party Proofing (Tipstrr vs Telegram Scams)",
                    "Forebet Poisson Distribution Model Strengths & Blind Spots",
                    "30-Day Testing Protocol for Free Daily Predictions"
                )

                presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                customPrompt = preset
                                viewModel.runDeepThinkingAudit(preset)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(preset, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AmberVIP, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Custom Prompt Box
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Custom Deep Audit Query", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    placeholder = { Text("Ask any complex mathematical or scam audit question...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_thinking_prompt_input"),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (customPrompt.isNotBlank()) {
                            viewModel.runDeepThinkingAudit(customPrompt)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_run_custom_thinking"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run High Thinking Analysis (gemini-3.1-pro-preview)")
                }
            }
        }

        // Results Section
        if (isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AmberVIP)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Gemini 3.1 Pro Thinking in Progress...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Calculating statistical distribution, odds margins, and expectation...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (auditResult != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thinking_audit_result_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, AmberVIP.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AmberVIP)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "High Thinking Audit Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberVIP
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = auditResult!!.text,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiChatbotContent(viewModel: PredictionViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val selectedModel by viewModel.selectedChatModel.collectAsState()

    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Which site is best for Over/Under 2.5 goals?",
        "Audit this VIP service claiming 85% accuracy",
        "How to track Forebet vs PredictZ for 30 days?",
        "What is Closing Line Value (CLV)?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gemini_chatbot_screen")
    ) {
        // Model Selector Bar & Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_FLASH,
                        onClick = { viewModel.setChatModel(GeminiService.MODEL_FLASH) },
                        label = { Text("gemini-3.5-flash", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_PRO,
                        onClick = { viewModel.setChatModel(GeminiService.MODEL_PRO) },
                        label = { Text("gemini-3.1-pro (High Think)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedModel == GeminiService.MODEL_FLASH_LITE,
                        onClick = { viewModel.setChatModel(GeminiService.MODEL_FLASH_LITE) },
                        label = { Text("gemini-3.1-flash-lite", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Quick prompt chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { q ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(q)
                    }
                ) {
                    Text(
                        text = q,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing prediction methodology...", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Input Field Bar
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Ask about prediction sites or paste picks...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        val text = userInput
                        userInput = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_send_chat"),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser && message.modelUsed != null) {
                    Text(
                        text = "Analyst (${message.modelUsed})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanOdds
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
