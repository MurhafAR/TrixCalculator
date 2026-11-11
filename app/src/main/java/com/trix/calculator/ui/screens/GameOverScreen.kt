package com.trix.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trix.calculator.data.GameMode
import com.trix.calculator.ui.components.ScoreBadge
import com.trix.calculator.ui.components.SectionHeader
import com.trix.calculator.ui.components.TrixCard
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * شاشة نهاية اللعبة وتتويج الفائزين
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverScreen(
    state: TrixUiState,
    viewModel: TrixViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scores = state.cumulativeScores
    val config = state.config
    val playerNames = config.playerNames
    val teamScores = state.teamScores

    val sortedPlayers = (0..3).sortedByDescending { scores[it] ?: 0 }
    val winnerPlayerId = sortedPlayers.first()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "نهاية اللعبة - النتائج النهائية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.setShowHistoryDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "السجل",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.restartWithSameConfig() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkBackground
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("لعبة جديدة بنفس اللاعبين والإعدادات", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الرجوع لشاشة الإعدادات الرئيسية")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // كرت تتويج الفائز
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "مبروك للفائز!",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val winnerTitle = if (config.gameMode == GameMode.PARTNERS) {
                        if (teamScores.leadingTeam == 1) config.getTeamName(1) else config.getTeamName(2)
                    } else {
                        playerNames.getOrElse(winnerPlayerId) { "اللاعب 1" }
                    }
                    Text(
                        text = winnerTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // منصة الترتيب الفردي
            TrixCard {
                SectionHeader(
                    title = "الترتيب النهائي للاعبين",
                    subtitle = "الرصيد النهائي المحقق بعد انتهاء كافة الممالك",
                    icon = Icons.Default.EmojiEvents
                )
                Spacer(modifier = Modifier.height(12.dp))

                sortedPlayers.forEachIndexed { index, pId ->
                    val score = scores[pId] ?: 0
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "  "
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground)
                            .border(1.dp, if (index == 0) GoldPrimary else BorderDark, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = medal, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) GoldLight else TextPrimary
                                )
                                if (config.gameMode == GameMode.PARTNERS) {
                                    Text(
                                        text = if (pId == 0 || pId == 2) "الفريق 1" else "الفريق 2",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        ScoreBadge(score = score, fontSize = 20)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
