package com.trix.calculator.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trix.calculator.data.ContractType
import com.trix.calculator.data.GameMode
import com.trix.calculator.data.GameType
import com.trix.calculator.data.PartnerScoringRule
import com.trix.calculator.ui.components.ScoreBadge
import com.trix.calculator.ui.components.SectionHeader
import com.trix.calculator.ui.components.TrixCard
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * لوحة المتابعة الرئيسية للعبة (Scoreboard & Dashboard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDashboardScreen(
    state: TrixUiState,
    viewModel: TrixViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val config = state.config
    val playerNames = config.playerNames
    val scores = state.cumulativeScores
    val teamScores = state.teamScores

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "حاسبة التركس",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = config.gameType.titleAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "المملكة الحالية: ${playerNames.getOrElse(state.activeKingdomOwnerId) { "" }} (الجولة ${state.rounds.size} من ${state.totalExpectedRounds})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    // زر التراجع السريع
                    IconButton(
                        onClick = { viewModel.undoLastRound() },
                        enabled = state.rounds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "تراجع",
                            tint = if (state.rounds.isNotEmpty()) GoldLight else TextMuted
                        )
                    }
                    // زر سجل الجولات
                    IconButton(onClick = { viewModel.setShowHistoryDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "السجل",
                            tint = TextSecondary
                        )
                    }
                    // زر القواعد
                    IconButton(onClick = { viewModel.setShowRulesDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "القواعد",
                            tint = TextSecondary
                        )
                    }
                    // زر إعادة اللعبة
                    IconButton(onClick = { viewModel.setShowResetConfirmDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة اللعبة",
                            tint = CrimsonHeart
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // زر تسجيل دور جديد رئيسي
                    Button(
                        onClick = { viewModel.openNewRoundInput() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkBackground
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل دور جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ----------------------------------------------------
            // 1. نتائج الشراكة (إذا كان نظام اللعب شراكة)
            // ----------------------------------------------------
            if (config.gameMode == GameMode.PARTNERS) {
                if (config.partnerScoringRule == PartnerScoringRule.SINGLE_TEAM_NET) {
                    // نمط تسجيل فريق واحد: الفارق المباشر بين الفريقين
                    SingleTeamNetCard(teamScores, config)
                } else {
                    // نمط تسجيل الفريقين جنباً إلى جنب
                    BothTeamsScoreCard(teamScores, config)
                }
            }

            // ----------------------------------------------------
            // 2. بطاقات اللاعبين الأربعة وترتيبهم الفردي
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "لوحة نقاط اللاعبين",
                    subtitle = "الرصيد التراكمي وترتيب اللاعبين",
                    icon = Icons.Default.Leaderboard
                )
                Spacer(modifier = Modifier.height(12.dp))

                // ترتيب اللاعبين تنازلياً حسب الرصيد
                val sortedPlayers = (0..3).sortedByDescending { scores[it] ?: 0 }

                sortedPlayers.forEachIndexed { rankIndex, pId ->
                    val score = scores[pId] ?: 0
                    val isCurrentKing = state.activeKingdomOwnerId == pId
                    val teamColor = if (config.gameMode == GameMode.PARTNERS) {
                        if (pId == 0 || pId == 2) EmeraldAccent else SlateBlueLight
                    } else GoldPrimary

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrentKing) DarkCardElevated else DarkBackground)
                            .border(
                                1.dp,
                                if (isCurrentKing) GoldPrimary.copy(alpha = 0.6f) else BorderDark,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // رقم الترتيب
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (rankIndex == 0) GoldPrimary else BorderDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${rankIndex + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (rankIndex == 0) DarkBackground else TextSecondary
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (isCurrentKing) {
                                        Text(text = "👑 صاحب المملكة", fontSize = 11.sp, color = GoldLight)
                                    }
                                }
                                if (config.gameMode == GameMode.PARTNERS) {
                                    Text(
                                        text = if (pId == 0 || pId == 2) "الفريق 1" else "الفريق 2",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = teamColor
                                    )
                                }
                            }
                        }

                        ScoreBadge(score = score, fontSize = 18)
                    }
                }
            }

            // ----------------------------------------------------
            // 3. حالة ممالك اللاعبين والعقود المتبقية
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "ممالك اللاعبين والعقود المتبقية",
                    subtitle = "انقر على أي عقد فارغ لتسجيله مباشرة",
                    icon = Icons.Default.GridOn
                )
                Spacer(modifier = Modifier.height(12.dp))

                (0..3).forEach { pId ->
                    val isCurrent = state.activeKingdomOwnerId == pId
                    val completed = state.completedContractsPerKingdom[pId] ?: emptySet()
                    val allContracts = if (config.gameType == GameType.COMPLEX) {
                        listOf(ContractType.COMPLEX, ContractType.TRIX)
                    } else {
                        listOf(
                            ContractType.KING_OF_HEARTS,
                            ContractType.QUEENS,
                            ContractType.DIAMONDS,
                            ContractType.LUTO,
                            ContractType.TRIX
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground)
                            .border(1.dp, if (isCurrent) GoldPrimary.copy(alpha = 0.5f) else BorderDark, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مملكة ${playerNames.getOrElse(pId) { "لاعب ${pId + 1}" }}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) GoldPrimary else TextPrimary
                            )
                            Text(
                                text = "${completed.size} من ${allContracts.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (completed.size == allContracts.size) EmeraldAccent else TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allContracts.forEach { contract ->
                                val isDone = contract in completed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDone) EmeraldAccent.copy(alpha = 0.15f) else DarkCard)
                                        .border(
                                            1.dp,
                                            if (isDone) EmeraldAccent.copy(alpha = 0.4f) else BorderDark,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(!isDone) {
                                            viewModel.openNewRoundInput(pId)
                                            viewModel.updateDraftInput { it.copy(contractType = contract) }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = contract.iconEmoji, fontSize = 16.sp)
                                        Text(
                                            text = if (isDone) "✓" else contract.titleAr.take(5),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = if (isDone) EmeraldAccent else TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // تأكيد إعادة ضبط اللعبة
    if (state.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowResetConfirmDialog(false) },
            title = { Text(text = "إعادة ضبط اللعبة", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(text = "هل ترغب في بدء لعبة جديدة من شاشة الإعدادات أم إعادة تصفير اللعبة الحالية؟", color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetGame() }) {
                    Text("شاشة الإعدادات الجديدة", color = CrimsonHeart)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restartWithSameConfig() }) {
                    Text("تصفير النقاط الحالية فقط", color = GoldLight)
                }
            },
            containerColor = DarkSurface
        )
    }
}

/**
 * بطاقة عرض نتائج الشراكة لكلا الفريقين
 */
@Composable
private fun BothTeamsScoreCard(teamScores: com.trix.calculator.data.TeamScores, config: com.trix.calculator.data.GameConfig) {
    TrixCard {
        SectionHeader(
            title = "نتائج فرق الشراكة",
            subtitle = "الفريق 1 (متقابلان) ضد الفريق 2 (متقابلان)",
            icon = Icons.Default.Groups
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // كرت الفريق الأول
            TeamCard(
                teamNumber = 1,
                teamName = config.getTeamName(1),
                score = teamScores.team1Score,
                isLeading = teamScores.leadingTeam == 1,
                accentColor = EmeraldAccent,
                modifier = Modifier.weight(1f)
            )

            // كرت الفريق الثاني
            TeamCard(
                teamNumber = 2,
                teamName = config.getTeamName(2),
                score = teamScores.team2Score,
                isLeading = teamScores.leadingTeam == 2,
                accentColor = SlateBlueLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeamCard(
    teamNumber: Int,
    teamName: String,
    score: Int,
    isLeading: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(if (isLeading) 2.dp else 1.dp, if (isLeading) accentColor else BorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "الفريق $teamNumber",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                if (isLeading) {
                    Text(text = "👑", fontSize = 12.sp)
                }
            }
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            ScoreBadge(score = score, fontSize = 20)
        }
    }
}

/**
 * بطاقة عرض الفارق على فريق واحد (طريقة التسجيل السورية الشعبية)
 */
@Composable
private fun SingleTeamNetCard(teamScores: com.trix.calculator.data.TeamScores, config: com.trix.calculator.data.GameConfig) {
    TrixCard(borderColor = GoldPrimary.copy(alpha = 0.5f)) {
        SectionHeader(
            title = "طريقة التسجيل على فريق واحد (الفارق المباشر)",
            subtitle = "فارق النقاط بين الفريقين",
            icon = Icons.Default.CompareArrows
        )
        Spacer(modifier = Modifier.height(10.dp))

        val diff = teamScores.netDifference
        val statusText = when {
            diff > 0 -> "الفريق 1 متقدم بـ +$diff نقطة"
            diff < 0 -> "الفريق 2 متقدم بـ +${-diff} نقطة"
            else -> "النتيجة متعادلة تماماً (0)"
        }
        val statusColor = when {
            diff > 0 -> EmeraldAccent
            diff < 0 -> SlateBlueLight
            else -> TextSecondary
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${config.getTeamName(1)}: ${teamScores.team1Score}  |  ${config.getTeamName(2)}: ${teamScores.team2Score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }
    }
}
