package com.trix.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trix.calculator.ui.components.SectionHeader
import com.trix.calculator.ui.components.TrixCard
import com.trix.calculator.ui.theme.*
import com.trix.calculator.ui.viewmodel.TrixUiState
import com.trix.calculator.ui.viewmodel.TrixViewModel

/**
 * نافذة مرجع قواعد التركس السورية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesHelpDialog(
    state: TrixUiState,
    viewModel: TrixViewModel
) {
    if (!state.showRulesDialog) return

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { viewModel.setShowRulesDialog(false) },
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
                                text = "دليل القواعد السورية وحساب النقاط",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.setShowRulesDialog(false) }) {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // جدول النقاط الأساسي
                    TrixCard {
                        SectionHeader(
                            title = "جدول النقاط الأساسي (التركس العادي)",
                            subtitle = "قوانين التركس السورية الأصيلة"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        RuleRow("♥️ ختيار الكبة (الشيخ)", "-75 نقطة (-150 مع التدبيل)")
                        RuleRow("👸 البنات (4 بنات)", "-25 نقطة لكل بنت (-50 مع التدبيل)، المجموع -100")
                        RuleRow("♦️ الديناري (13 كرت)", "-10 نقاط لكل كرت ديناري، المجموع -130")
                        RuleRow("🃏 اللطوش (13 أكلة)", "-15 نقطة لكل أكلة، المجموع -195")
                        RuleRow("🏆 التركس", "الأول: +200 | الثاني: +150 | الثالث: +100 | الرابع: +50")

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 ملاحظة: مجموع نقاط كل مملكة في اللعبة يساوي صفراً (مجموع السلبيات -500 ومجموع التركس +500).",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight
                        )
                    }

                    // نظام الكومبلكس
                    TrixCard {
                        SectionHeader(
                            title = "نظام الكومبلكس (Complex)",
                            subtitle = "لعبة سريعة تتكون من شوطين فقط لكل مملكة"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "في الكومبلكس، يلعب صاحب المملكة شوطين فقط:\n" +
                                    "1. شوط الكومبلكس: يجمع الختيار (-75) + البنات الأربعة (-100) + الديناري (-130) + اللطوش (-195) بمجموع سالب قدره -500 نقطة.\n" +
                                    "2. شوط التركس: ترتيب النزول (+200، +150، +100، +50) بمجموع إيجابي +500 نقطة.\n" +
                                    "تكتمل اللعبة كاملة بـ 8 جولات فقط (شوطان لكل لاعب من اللاعبين الأربعة).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    // قواعد التدبيل
                    TrixCard {
                        SectionHeader(
                            title = "قواعد التدبيل (المضاعفة)",
                            subtitle = "تدبيل ختيار الكبة والبنات"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• تدبيل الختيار:\n" +
                                    "إذا دبل لاعب ختيار الكبة وأكله لاعب آخر: ينزل على الآكل -150 نقطة، ويحصل المدبل على +75 نقطة.\n" +
                                    "إذا أكل المدبل الختيار بنفسه: ينزل عليه -150 نقطة دون أي مكافأة.\n\n" +
                                    "• تدبيل البنات:\n" +
                                    "البنت المدبلة تخصم -50 نقطة على من يأكلها، ويحصل مدبلها على +25 نقطة (إلا إذا أكلها المدبل بنفسه فيخصم عليه -50 فقط).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    // الشراكة وطرق التسجيل
                    TrixCard {
                        SectionHeader(
                            title = "الشراكة وطرق التسجيل",
                            subtitle = "اللاعب 1 و 3 (فريق 1) مقابل اللاعب 2 و 4 (فريق 2)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• تسجيل لفريقين:\n" +
                                    "يتم جمع نقاط كل لاعب مع شريكه المتقابل، وتُعرض النتيجة الإجمالية للفريقين جنباً إلى جنب مع ترتيب اللاعبين الفردي.\n\n" +
                                    "• تسجيل على فريق واحد:\n" +
                                    "طريقة شعبية شائعة تعتمد على حساب فارق النقاط بين الفريقين مباشرة، لمعرفة الفريق المتقدم وكم نقطة يتقدم بها على منافسه.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RuleRow(title: String, points: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(text = points, style = MaterialTheme.typography.bodyMedium, color = GoldLight, fontWeight = FontWeight.Bold)
    }
}
