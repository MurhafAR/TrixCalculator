package com.trix.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trix.calculator.ui.components.ScoreBadge
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * نافذة سجل الجولات والتحكم في تعديل أو حذف أي جولة سابقة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    state: TrixUiState,
    viewModel: TrixViewModel
) {
    if (!state.showHistoryDialog) return

    val playerNames = state.config.playerNames

    Dialog(
        onDismissRequest = { viewModel.setShowHistoryDialog(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = DarkSurface
        ) {
            Scaffold(
                containerColor = DarkSurface,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "سجل الجولات وتعديل الأدوار (${state.rounds.size} جولة)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.setShowHistoryDialog(false) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                    )
                }
            ) { innerPadding ->
                if (state.rounds.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يتم تسجيل أي جولة بعد.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.rounds) { index, round ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // رأس بطاقة الجولة
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GoldPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "الجولة ${round.roundNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = "${round.contractType.iconEmoji} ${round.contractType.titleAr}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }

                                        // أزرار التعديل والحذف
                                        Row {
                                            IconButton(
                                                onClick = { viewModel.openEditRound(index) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "تعديل",
                                                    tint = SlateBlueLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteRound(index) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = DangerRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "المملكة: ${playerNames.getOrElse(round.kingdomOwnerId) { "لاعب ${round.kingdomOwnerId + 1}" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // تفصيل نقاط اللاعبين في هذا الدور
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkBackground)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        (0..3).forEach { pId ->
                                            val score = round.playerScores[pId] ?: 0
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = playerNames.getOrElse(pId) { "لاعب ${pId + 1}" },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 11.sp,
                                                    color = TextMuted,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                ScoreBadge(score = score, fontSize = 12)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
