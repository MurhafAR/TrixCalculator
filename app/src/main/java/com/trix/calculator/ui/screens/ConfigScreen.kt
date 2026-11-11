package com.trix.calculator.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
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
import com.trix.calculator.data.GameMode
import com.trix.calculator.data.GameType
import com.trix.calculator.data.PartnerScoringRule
import com.trix.calculator.ui.components.SectionHeader
import com.trix.calculator.ui.components.SelectableCard
import com.trix.calculator.ui.components.TrixCard
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * شاشة الإعدادات والتهيئة قبل بدء اللعبة
 * تحتوي على جميع خيارات اللعبة وتكشف خيار طريقة التسجيل شرطياً عند اختيار "شراكة"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    state: TrixUiState,
    viewModel: TrixViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "حاسبة التركس والكومبلكس",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "إعدادات وقواعد اللعبة السورية",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setShowRulesDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = "قواعد اللعبة",
                            tint = GoldLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.startGame() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بدء اللعبة",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ----------------------------------------------------
            // 1. نظام اللعب (Game Mode)
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "نظام اللعب (طريقة اللعب)",
                    subtitle = "اختر ما إذا كان اللعب فردياً أو بنظام الشراكة بين الفرق",
                    icon = Icons.Default.Group
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectableCard(
                    selected = state.config.gameMode == GameMode.INDIVIDUAL,
                    onClick = { viewModel.setGameMode(GameMode.INDIVIDUAL) },
                    title = GameMode.INDIVIDUAL.titleAr,
                    description = GameMode.INDIVIDUAL.descriptionAr,
                    badgeText = "4 لاعبين"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SelectableCard(
                    selected = state.config.gameMode == GameMode.PARTNERS,
                    onClick = { viewModel.setGameMode(GameMode.PARTNERS) },
                    title = GameMode.PARTNERS.titleAr,
                    description = GameMode.PARTNERS.descriptionAr,
                    badgeText = "فريقين متقابلين"
                )
            }

            // ----------------------------------------------------
            // 2. نوع اللعبة (Game Type)
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "نوع اللعبة (عادي أو كومبلكس)",
                    subtitle = "الكومبلكس يجمع جميع العقود السلبية في شوط واحد لكل مملكة",
                    icon = Icons.Default.Casino
                )
                Spacer(modifier = Modifier.height(12.dp))

                SelectableCard(
                    selected = state.config.gameType == GameType.REGULAR,
                    onClick = { viewModel.setGameType(GameType.REGULAR) },
                    title = GameType.REGULAR.titleAr,
                    description = GameType.REGULAR.descriptionAr,
                    badgeText = "20 جولة إجمالية"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SelectableCard(
                    selected = state.config.gameType == GameType.COMPLEX,
                    onClick = { viewModel.setGameType(GameType.COMPLEX) },
                    title = GameType.COMPLEX.titleAr,
                    description = GameType.COMPLEX.descriptionAr,
                    badgeText = "8 جولات سريعة"
                )
            }

            // ----------------------------------------------------
            // 3. التدبيل (Doubling)
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "التدبيل (تدبيل البنات والختايرة)",
                    subtitle = "السماح بمضاعفة ختيار الكبة والبنات (+75 / -150 و +25 / -50)",
                    icon = Icons.Default.Settings
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelectableCard(
                        selected = state.config.doublingEnabled,
                        onClick = { viewModel.setDoublingEnabled(true) },
                        title = "نعم (مع تدبيل)",
                        description = "مضاعفة الختيار والبنات",
                        modifier = Modifier.weight(1f)
                    )
                    SelectableCard(
                        selected = !state.config.doublingEnabled,
                        onClick = { viewModel.setDoublingEnabled(false) },
                        title = "لا (بدون تدبيل)",
                        description = "النقاط الأساسية فقط",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ----------------------------------------------------
            // 4. طريقة التسجيل في الشراكة (شرطياً تظهر فقط عند اختيار شراكة)
            // ----------------------------------------------------
            AnimatedVisibility(
                visible = state.config.gameMode == GameMode.PARTNERS,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                TrixCard(borderColor = SlateBlue.copy(alpha = 0.5f)) {
                    SectionHeader(
                        title = "طريقة تسجيل الشراكة",
                        subtitle = "كيفية حساب وتدوين نقاط الفريقين في لوحة النتيجة",
                        icon = Icons.Default.Group
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SelectableCard(
                        selected = state.config.partnerScoringRule == PartnerScoringRule.BOTH_TEAMS,
                        onClick = { viewModel.setPartnerScoringRule(PartnerScoringRule.BOTH_TEAMS) },
                        title = PartnerScoringRule.BOTH_TEAMS.titleAr,
                        description = PartnerScoringRule.BOTH_TEAMS.descriptionAr
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SelectableCard(
                        selected = state.config.partnerScoringRule == PartnerScoringRule.SINGLE_TEAM_NET,
                        onClick = { viewModel.setPartnerScoringRule(PartnerScoringRule.SINGLE_TEAM_NET) },
                        title = PartnerScoringRule.SINGLE_TEAM_NET.titleAr,
                        description = PartnerScoringRule.SINGLE_TEAM_NET.descriptionAr
                    )
                }
            }

            // ----------------------------------------------------
            // 5. أسماء اللاعبين
            // ----------------------------------------------------
            TrixCard {
                SectionHeader(
                    title = "أسماء اللاعبين",
                    subtitle = if (state.config.gameMode == GameMode.PARTNERS)
                        "ترتيب الجلوس: اللاعب 1 و 3 متقابلان (فريق 1)، اللاعب 2 و 4 متقابلان (فريق 2)"
                    else
                        "أدخل أسماء اللاعبين الأربعة حسب ترتيب الجلوس"
                )
                Spacer(modifier = Modifier.height(12.dp))

                state.config.playerNames.forEachIndexed { index, name ->
                    val teamBadge = if (state.config.gameMode == GameMode.PARTNERS) {
                        if (index == 0 || index == 2) "الفريق 1" else "الفريق 2"
                    } else null

                    val teamColor = if (index == 0 || index == 2) EmeraldAccent else SlateBlueLight

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.setPlayerName(index, it) },
                        label = { Text("اللاعب رقم ${index + 1}") },
                        trailingIcon = {
                            if (teamBadge != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = teamColor.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = teamBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = teamColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
