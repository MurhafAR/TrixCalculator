package com.trix.calculator.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trix.calculator.data.*
import com.trix.calculator.ui.components.NumberStepper
import com.trix.calculator.ui.components.ScoreBadge
import com.trix.calculator.ui.components.SectionHeader
import com.trix.calculator.ui.components.TrixCard
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * نافذة إدخال وتعديل نتائج الجولة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundInputDialog(
    state: TrixUiState,
    viewModel: TrixViewModel
) {
    if (!state.isRoundInputActive) return

    val draft = state.draftInput
    val config = state.config
    val playerNames = config.playerNames
    val scrollState = rememberScrollState()

    val isPartners = config.gameMode == GameMode.PARTNERS

    // معاينة النقاط المباشرة
    val previewScores = remember(draft, config.doublingEnabled, config.gameMode) {
        ScoringEngine.calculateRoundScores(draft, config.doublingEnabled, config.gameMode)
    }

    Dialog(
        onDismissRequest = { viewModel.dismissRoundInput() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = DarkSurface
        ) {
            Scaffold(
                containerColor = DarkSurface,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (state.editingRoundIndex != null)
                                    "تعديل الجولة رقم ${state.editingRoundIndex + 1}"
                                else
                                    "تسجيل جولة جديدة",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.dismissRoundInput() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = TextSecondary
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
                        color = DarkCard,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // تنبيه الخطأ إن وُجد
                            if (state.validationError != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DangerRed.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = DangerRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = state.validationError,
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // شريط معاينة النقاط السريع
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPartners) {
                                    val t1Score = (previewScores[0] ?: 0) + (previewScores[2] ?: 0)
                                    val t2Score = (previewScores[1] ?: 0) + (previewScores[3] ?: 0)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "الفريق 1 (${playerNames.getOrElse(0) { "" }} و ${playerNames.getOrElse(2) { "" }})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        ScoreBadge(score = t1Score, fontSize = 14)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "الفريق 2 (${playerNames.getOrElse(1) { "" }} و ${playerNames.getOrElse(3) { "" }})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateBlueLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        ScoreBadge(score = t2Score, fontSize = 14)
                                    }
                                } else {
                                    (0..3).forEach { pId ->
                                        val pts = previewScores[pId] ?: 0
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                            ScoreBadge(score = pts, fontSize = 13)
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.submitDraftRound() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.editingRoundIndex != null) "حفظ التعديلات" else "اعتماد وتسجيل الدور",
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ----------------------------------------------------
                    // 1. اختيار صاحب المملكة (الملك)
                    // ----------------------------------------------------
                    TrixCard {
                        SectionHeader(
                            title = "صاحب المملكة (الملك)",
                            subtitle = "حدد اللاعب صاحب المملكة الحالية",
                            icon = Icons.Default.Person
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (0..3).forEach { pId ->
                                val selected = draft.kingdomOwnerId == pId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) GoldPrimary.copy(alpha = 0.2f) else DarkBackground)
                                        .border(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) GoldPrimary else BorderDark,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.updateDraftInput { it.copy(kingdomOwnerId = pId) }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "👑 ${pId + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (selected) GoldPrimary else TextSecondary
                                        )
                                        Text(
                                            text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) GoldLight else TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // 2. اختيار نوع العقد / الطلب
                    // ----------------------------------------------------
                    TrixCard {
                        SectionHeader(
                            title = "نوع الطلب",
                            subtitle = "اختر العقد المطلوب حسابه لهذه المملكة",
                            icon = Icons.Default.Casino
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val availableContracts = if (config.gameType == GameType.COMPLEX) {
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

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableContracts.forEach { contract ->
                                val selected = draft.contractType == contract
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) DarkCardElevated else DarkBackground)
                                        .border(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) GoldPrimary else BorderDark,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.updateDraftInput { it.copy(contractType = contract) }
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = contract.iconEmoji, fontSize = 20.sp)
                                            Column {
                                                Text(
                                                    text = contract.titleAr,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selected) GoldLight else TextPrimary
                                                )
                                                Text(
                                                    text = contract.basePointsDescriptionAr,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        RadioButton(
                                            selected = selected,
                                            onClick = {
                                                viewModel.updateDraftInput { it.copy(contractType = contract) }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = GoldPrimary,
                                                unselectedColor = BorderLight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // 3. محتويات المدخلات الخاصة بكل عقد
                    // ----------------------------------------------------
                    when (draft.contractType) {
                        ContractType.TRIX -> {
                            TrixInputSection(draft, viewModel, playerNames)
                        }
                        ContractType.KING_OF_HEARTS -> {
                            KingInputSection(draft, viewModel, playerNames, config.doublingEnabled, isPartners)
                        }
                        ContractType.QUEENS -> {
                            QueensInputSection(draft, viewModel, playerNames, config.doublingEnabled, isPartners)
                        }
                        ContractType.DIAMONDS -> {
                            DiamondsInputSection(draft, viewModel, playerNames)
                        }
                        ContractType.LUTO -> {
                            LutoInputSection(draft, viewModel, playerNames)
                        }
                        ContractType.COMPLEX -> {
                            ComplexInputSection(draft, viewModel, playerNames, config.doublingEnabled, isPartners)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * إدخال ترتيب التركس (الأول +200، الثاني +150، الثالث +100، الرابع +50)
 */
@Composable
private fun TrixInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>
) {
    TrixCard {
        SectionHeader(
            title = "ترتيب نزول اللاعبين في التركس",
            subtitle = "حدد مركز كل لاعب من 1 إلى 4 حسب انتهائه من الورق",
            icon = Icons.Default.EmojiEvents
        )
        Spacer(modifier = Modifier.height(12.dp))

        (0..3).forEach { pId ->
            val currentRank = draft.trixRankings[pId]
            val playerName = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..4).forEach { rank ->
                        val isSelected = currentRank == rank
                        val rankColor = when (rank) {
                            1 -> GoldPrimary
                            2 -> EmeraldAccent
                            3 -> SlateBlueLight
                            else -> TextSecondary
                        }
                        val pointsText = when (rank) {
                            1 -> "+200"
                            2 -> "+150"
                            3 -> "+100"
                            else -> "+50"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) rankColor else DarkCard)
                                .border(1.dp, if (isSelected) rankColor else BorderDark, RoundedCornerShape(8.dp))
                                .clickable {
                                    val updated = draft.trixRankings.toMutableMap()
                                    // إذا كان هناك لاعب آخر يحمل هذا الترتيب، نبدلهما أو نزيله
                                    val previousHolder = updated.entries.firstOrNull { it.value == rank }?.key
                                    if (previousHolder != null && previousHolder != pId) {
                                        if (currentRank != null) {
                                            updated[previousHolder] = currentRank
                                        } else {
                                            updated.remove(previousHolder)
                                        }
                                    }
                                    updated[pId] = rank
                                    viewModel.updateDraftInput { it.copy(trixRankings = updated) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "المركز $rank",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkBackground else TextPrimary
                                )
                                Text(
                                    text = pointsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (isSelected) DarkBackground else rankColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * إدخال ختيار الكبة (الشيخ)
 */
@Composable
private fun KingInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>,
    doublingEnabled: Boolean,
    isPartners: Boolean = false
) {
    TrixCard {
        SectionHeader(
            title = if (isPartners) "أي فريق أكل ختيار الكبة (الشيخ)؟" else "من أكل ختيار الكبة (الشيخ)؟",
            subtitle = if (doublingEnabled) {
                if (isPartners)
                    "الآكل ينزل عليه -75 (أو -150 إذا كان مدبلاً ويحصل الفريق الآخر على +75)"
                else
                    "يخصم منه -75 نقطة (أو -150 إذا كان مدبلاً)"
            } else {
                "يخصم من الفريق الآكل -75 نقطة"
            },
            icon = Icons.Default.Favorite
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isPartners) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isTeam1 = draft.kingTakerId == 0 || draft.kingTakerId == 2
                val isTeam2 = draft.kingTakerId == 1 || draft.kingTakerId == 3

                // الفريق 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTeam1) EmeraldAccent.copy(alpha = 0.2f) else DarkBackground)
                        .border(
                            if (isTeam1) 2.dp else 1.dp,
                            if (isTeam1) EmeraldAccent else BorderDark,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.updateDraftInput {
                                it.copy(
                                    kingTakerId = 0,
                                    kingDoublerId = if (it.kingDoublerId != null) 1 else null
                                )
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الفريق 1",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isTeam1) EmeraldAccent else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${playerNames.getOrElse(0) { "1" }} و ${playerNames.getOrElse(2) { "3" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // الفريق 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTeam2) SlateBlueLight.copy(alpha = 0.2f) else DarkBackground)
                        .border(
                            if (isTeam2) 2.dp else 1.dp,
                            if (isTeam2) SlateBlueLight else BorderDark,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.updateDraftInput {
                                it.copy(
                                    kingTakerId = 1,
                                    kingDoublerId = if (it.kingDoublerId != null) 0 else null
                                )
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الفريق 2",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isTeam2) SlateBlueLight else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${playerNames.getOrElse(1) { "2" }} و ${playerNames.getOrElse(3) { "4" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..3).forEach { pId ->
                    val isTaker = draft.kingTakerId == pId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTaker) CrimsonHeart.copy(alpha = 0.2f) else DarkBackground)
                            .border(
                                if (isTaker) 2.dp else 1.dp,
                                if (isTaker) CrimsonHeart else BorderDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                viewModel.updateDraftInput { it.copy(kingTakerId = pId) }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isTaker) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTaker) CrimsonHeart else TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // التدبيل إن كان مفعلاً
        if (doublingEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderDark)
            Spacer(modifier = Modifier.height(12.dp))

            SectionHeader(
                title = "تدبيل الختيار",
                subtitle = if (isPartners)
                    "المدبل ينزل -150 على الفريق الآكل، و+75 للفريق الآخر الذي لم يأكل"
                else
                    "المدبل يحصل على +75، والآكل ينزل عليه -150 (إلا إذا أكلها المدبل فينزل عليه -150)",
                icon = Icons.Default.Bolt
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isPartners) {
                val isDoubled = draft.kingDoublerId != null
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // خيار بدون تدبيل
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isDoubled) GoldPrimary.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (!isDoubled) GoldPrimary else BorderDark, RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.updateDraftInput { it.copy(kingDoublerId = null) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يُدبَّل (عادي -75)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (!isDoubled) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isDoubled) GoldPrimary else TextSecondary
                        )
                    }

                    // خيار مدبل
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDoubled) CrimsonHeart.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (isDoubled) CrimsonHeart else BorderDark, RoundedCornerShape(10.dp))
                            .clickable {
                                val currentTaker = draft.kingTakerId ?: 0
                                val otherTeam = if (currentTaker == 0 || currentTaker == 2) 1 else 0
                                viewModel.updateDraftInput { it.copy(kingDoublerId = otherTeam) }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مدبَّل ⚡ (-150 / +75)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDoubled) CrimsonHeart else TextSecondary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // خيار بدون تدبيل
                    val isNotDoubled = draft.kingDoublerId == null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isNotDoubled) GoldPrimary.copy(alpha = 0.2f) else DarkBackground)
                            .border(1.dp, if (isNotDoubled) GoldPrimary else BorderDark, RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.updateDraftInput { it.copy(kingDoublerId = null) }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يُدبَّل",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNotDoubled) GoldPrimary else TextSecondary
                        )
                    }

                    // خيارات اللاعبين المدبلين
                    (0..3).forEach { pId ->
                        val isDoubler = draft.kingDoublerId == pId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDoubler) GoldPrimary.copy(alpha = 0.2f) else DarkBackground)
                                .border(1.dp, if (isDoubler) GoldPrimary else BorderDark, RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.updateDraftInput { it.copy(kingDoublerId = pId) }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "دبّله ${playerNames.getOrElse(pId) { "${pId + 1}" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDoubler) GoldPrimary else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
}

/**
 * إدخال كروت البنات (4 بنات) مع التدبيل
 */
@Composable
private fun QueensInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>,
    doublingEnabled: Boolean,
    isPartners: Boolean = false
) {
    val totalQueens = (0..3).sumOf { draft.queensTaken[it] ?: 0 }
    val remainingQueens = 4 - totalQueens

    TrixCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = if (isPartners) "البنات (4 بنات إجمالاً)" else "البنات (4 بنات إجمالاً)",
                subtitle = if (doublingEnabled) {
                    if (isPartners)
                        "-25 لكل بنت (-50 إذا دُبّلت ويحصل الفريق الآخر على +25)"
                    else
                        "-25 نقطة لكل بنت (-50 للبنت المدبلة)"
                } else {
                    "-25 نقطة لكل بنت"
                },
                icon = Icons.Default.Face
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (remainingQueens == 0) EmeraldAccent.copy(alpha = 0.2f) else CrimsonHeart.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (remainingQueens == 0) "المجموع مكتمل (4/4)" else "المتبقي: $remainingQueens من 4",
                    color = if (remainingQueens == 0) EmeraldAccent else CrimsonHeart,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (isPartners) {
            val team1Queens = (draft.queensTaken[0] ?: 0) + (draft.queensTaken[2] ?: 0)
            val team2Queens = (draft.queensTaken[1] ?: 0) + (draft.queensTaken[3] ?: 0)

            NumberStepper(
                value = team1Queens,
                onValueChange = { newVal ->
                    val updated = draft.queensTaken.toMutableMap()
                    updated[0] = newVal
                    updated[2] = 0
                    val curDoubledT1 = draft.doubledQueens.count { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                    val newDoubled = if (curDoubledT1 > newVal) {
                        val t2Doubled = draft.doubledQueens.filter { it.takerPlayerId == 1 || it.takerPlayerId == 3 }
                        t2Doubled + List(newVal) { DoubledQueenInfo(doublerPlayerId = 1, takerPlayerId = 0) }
                    } else draft.doubledQueens
                    viewModel.updateDraftInput { it.copy(queensTaken = updated, doubledQueens = newDoubled) }
                },
                minValue = 0,
                maxValue = 4,
                label = "الفريق 1 (${playerNames.getOrElse(0) { "1" }} و ${playerNames.getOrElse(2) { "3" }})",
                accentColor = EmeraldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))

            NumberStepper(
                value = team2Queens,
                onValueChange = { newVal ->
                    val updated = draft.queensTaken.toMutableMap()
                    updated[1] = newVal
                    updated[3] = 0
                    val curDoubledT2 = draft.doubledQueens.count { it.takerPlayerId == 1 || it.takerPlayerId == 3 }
                    val newDoubled = if (curDoubledT2 > newVal) {
                        val t1Doubled = draft.doubledQueens.filter { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                        t1Doubled + List(newVal) { DoubledQueenInfo(doublerPlayerId = 0, takerPlayerId = 1) }
                    } else draft.doubledQueens
                    viewModel.updateDraftInput { it.copy(queensTaken = updated, doubledQueens = newDoubled) }
                },
                minValue = 0,
                maxValue = 4,
                label = "الفريق 2 (${playerNames.getOrElse(1) { "2" }} و ${playerNames.getOrElse(3) { "4" }})",
                accentColor = SlateBlueLight
            )
        } else {
            (0..3).forEach { pId ->
                val count = draft.queensTaken[pId] ?: 0
                NumberStepper(
                    value = count,
                    onValueChange = { newVal ->
                        val updated = draft.queensTaken.toMutableMap()
                        updated[pId] = newVal
                        viewModel.updateDraftInput { it.copy(queensTaken = updated) }
                    },
                    minValue = 0,
                    maxValue = 4,
                    label = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                    accentColor = CrimsonHeart
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // تدبيل البنات
        if (doublingEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderDark)
            Spacer(modifier = Modifier.height(12.dp))

            if (isPartners) {
                val team1Queens = (draft.queensTaken[0] ?: 0) + (draft.queensTaken[2] ?: 0)
                val team2Queens = (draft.queensTaken[1] ?: 0) + (draft.queensTaken[3] ?: 0)
                val doubledT1 = draft.doubledQueens.count { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                val doubledT2 = draft.doubledQueens.count { it.takerPlayerId == 1 || it.takerPlayerId == 3 }

                SectionHeader(
                    title = "تدبيل البنات (+25 للفريق الآخر / -50 للآكل)",
                    subtitle = "حدد كم بنت مدبلة أكلها كل فريق من بناته",
                    icon = Icons.Default.Bolt
                )
                Spacer(modifier = Modifier.height(8.dp))

                NumberStepper(
                    value = doubledT1,
                    onValueChange = { newVal ->
                        val t2Doubled = draft.doubledQueens.filter { it.takerPlayerId == 1 || it.takerPlayerId == 3 }
                        val newT1Doubled = List(newVal) { DoubledQueenInfo(doublerPlayerId = 1, takerPlayerId = 0) }
                        viewModel.updateDraftInput { it.copy(doubledQueens = t2Doubled + newT1Doubled) }
                    },
                    minValue = 0,
                    maxValue = team1Queens,
                    label = "بنات مدبلة أكلها الفريق 1",
                    accentColor = EmeraldAccent
                )
                Spacer(modifier = Modifier.height(6.dp))

                NumberStepper(
                    value = doubledT2,
                    onValueChange = { newVal ->
                        val t1Doubled = draft.doubledQueens.filter { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                        val newT2Doubled = List(newVal) { DoubledQueenInfo(doublerPlayerId = 0, takerPlayerId = 1) }
                        viewModel.updateDraftInput { it.copy(doubledQueens = t1Doubled + newT2Doubled) }
                    },
                    minValue = 0,
                    maxValue = team2Queens,
                    label = "بنات مدبلة أكلها الفريق 2",
                    accentColor = SlateBlueLight
                )
            } else {
                SectionHeader(
                    title = "تدبيل البنات (+25 للمدبل / -50 للآكل)",
                    subtitle = "حدد أي بنت تم تدبيلها ومن دبلها ومن أكلها"
                )
                Spacer(modifier = Modifier.height(8.dp))

                draft.doubledQueens.forEachIndexed { index, info ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "بنت ${index + 1}: دبّلها ${playerNames.getOrElse(info.doublerPlayerId) { "" }} وأكلها ${playerNames.getOrElse(info.takerPlayerId) { "" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = {
                                val updated = draft.doubledQueens.toMutableList()
                                updated.removeAt(index)
                                viewModel.updateDraftInput { it.copy(doubledQueens = updated) }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = CrimsonHeart)
                        }
                    }
                }

                if (draft.doubledQueens.size < 4) {
                    var showAddDoubledSheet by remember { mutableStateOf(false) }
                    var selectedDoubler by remember { mutableStateOf(0) }
                    var selectedTaker by remember { mutableStateOf(0) }

                    OutlinedButton(
                        onClick = { showAddDoubledSheet = !showAddDoubledSheet },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "إضافة بنت مدبلة")
                    }

                    if (showAddDoubledSheet) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "من قام بتدبيل البنت؟", style = MaterialTheme.typography.labelSmall, color = GoldLight)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..3).forEach { pId ->
                                    FilterChip(
                                        selected = selectedDoubler == pId,
                                        onClick = { selectedDoubler = pId },
                                        label = { Text(playerNames.getOrElse(pId) { "${pId + 1}" }) }
                                    )
                                }
                            }

                            Text(text = "من أكل هذه البنت المدبلة؟", style = MaterialTheme.typography.labelSmall, color = CrimsonHeart)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..3).forEach { pId ->
                                    FilterChip(
                                        selected = selectedTaker == pId,
                                        onClick = { selectedTaker = pId },
                                        label = { Text(playerNames.getOrElse(pId) { "${pId + 1}" }) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val updated = draft.doubledQueens + DoubledQueenInfo(selectedDoubler, selectedTaker)
                                    viewModel.updateDraftInput { it.copy(doubledQueens = updated) }
                                    showAddDoubledSheet = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                            ) {
                                Text("تأكيد تدبيل البنت", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * إدخال كروت الديناري (13 كرت)
 */
@Composable
private fun DiamondsInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>
) {
    val totalDiamonds = (0..3).sumOf { draft.diamondsTaken[it] ?: 0 }
    val remaining = 13 - totalDiamonds

    TrixCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "الديناري (13 كرت إجمالاً)",
                subtitle = "-10 نقاط لكل كرت ديناري",
                icon = Icons.Default.Diamond
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (remaining == 0) EmeraldAccent.copy(alpha = 0.2f) else CrimsonHeart.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (remaining == 0) "المجموع مكتمل (13/13)" else "المتبقي: $remaining من 13",
                    color = if (remaining == 0) EmeraldAccent else CrimsonHeart,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        (0..3).forEach { pId ->
            val count = draft.diamondsTaken[pId] ?: 0
            NumberStepper(
                value = count,
                onValueChange = { newVal ->
                    val updated = draft.diamondsTaken.toMutableMap()
                    updated[pId] = newVal
                    viewModel.updateDraftInput { it.copy(diamondsTaken = updated) }
                },
                minValue = 0,
                maxValue = 13,
                label = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                accentColor = SlateBlueLight
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * إدخال أكلات اللطوش (13 لطش / أكلة)
 */
@Composable
private fun LutoInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>
) {
    val totalLuto = (0..3).sumOf { draft.lutoTricksTaken[it] ?: 0 }
    val remaining = 13 - totalLuto

    TrixCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "اللطوش (13 أكلة إجمالاً)",
                subtitle = "-15 نقطة لكل أكلة لطوش",
                icon = Icons.Default.Layers
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (remaining == 0) EmeraldAccent.copy(alpha = 0.2f) else CrimsonHeart.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (remaining == 0) "المجموع مكتمل (13/13)" else "المتبقي: $remaining من 13",
                    color = if (remaining == 0) EmeraldAccent else CrimsonHeart,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        (0..3).forEach { pId ->
            val count = draft.lutoTricksTaken[pId] ?: 0
            NumberStepper(
                value = count,
                onValueChange = { newVal ->
                    val updated = draft.lutoTricksTaken.toMutableMap()
                    updated[pId] = newVal
                    viewModel.updateDraftInput { it.copy(lutoTricksTaken = updated) }
                },
                minValue = 0,
                maxValue = 13,
                label = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                accentColor = EmeraldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * إدخال الكومبلكس (يجمع الختيار + البنات + الديناري + اللطوش)
 */
@Composable
private fun ComplexInputSection(
    draft: RoundInput,
    viewModel: TrixViewModel,
    playerNames: List<String>,
    doublingEnabled: Boolean,
    isPartners: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // بطاقة تنبيه مجموع الكومبلكس
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldPrimary.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "جولة الكومبلكس الشاملة (-500 نقطة سلبية أساسية)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = if (isPartners)
                            "أدخل كروت الختيار والبنات بحسب الفريق، بالإضافة إلى الديناري (13) واللطوش (13)."
                        else
                            "أدخل كروت الختيار، البنات الأربعة، الديناري (13)، واللطوش (13).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 1. ختيار الكومبلكس
        KingInputSection(draft, viewModel, playerNames, doublingEnabled, isPartners)

        // 2. بنات الكومبلكس
        QueensInputSection(draft, viewModel, playerNames, doublingEnabled, isPartners)

        // 3. ديناري الكومبلكس
        DiamondsInputSection(draft, viewModel, playerNames)

        // 4. لطوش الكومبلكس
        LutoInputSection(draft, viewModel, playerNames)
    }
}
