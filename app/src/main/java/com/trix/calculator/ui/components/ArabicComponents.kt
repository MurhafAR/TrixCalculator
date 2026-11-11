package com.trix.calculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trix.calculator.ui.theme.*

/**
 * عداد رقمي مرن مع أزرار + و - مصمم خصيصاً لإدخال كروت التركس واللطوش
 */
@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 13,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = GoldPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // زر النقصان
            FilledIconButton(
                onClick = { if (value > minValue) onValueChange(value - 1) },
                enabled = value > minValue,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = DarkCardElevated,
                    contentColor = TextPrimary,
                    disabledContainerColor = DarkBackground.copy(alpha = 0.5f),
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "نقصان",
                    modifier = Modifier.size(18.dp)
                )
            }

            // عرض الرقم الحالي
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .border(1.dp, if (value > 0) accentColor.copy(alpha = 0.6f) else BorderDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (value > 0) accentColor else TextSecondary
                )
            }

            // زر الزيادة
            FilledIconButton(
                onClick = { if (value < maxValue) onValueChange(value + 1) },
                enabled = value < maxValue,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = accentColor.copy(alpha = 0.25f),
                    contentColor = accentColor,
                    disabledContainerColor = DarkBackground.copy(alpha = 0.5f),
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "زيادة",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * بطاقة فاخرة بإطار أنيق وخلفية معتمة
 */
@Composable
fun TrixCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderDark,
    backgroundColor: Color = DarkSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * عنوان قسم بتصميم عربي أنيق
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * شارة عرض النقاط (موجب أخضر / سالب أحمر / صفر رمادي)
 */
@Composable
fun ScoreBadge(
    score: Int,
    modifier: Modifier = Modifier,
    fontSize: Int = 16
) {
    val (bgColor, textColor, sign) = when {
        score > 0 -> Triple(EmeraldAccent.copy(alpha = 0.15f), EmeraldAccent, "+")
        score < 0 -> Triple(CrimsonHeart.copy(alpha = 0.15f), CrimsonHeart, "")
        else -> Triple(BorderDark.copy(alpha = 0.5f), TextSecondary, "")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$sign$score",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * خيار قابل للاختيار بنقرة واحدة بتصميم كروت عصرية
 */
@Composable
fun <T> SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) GoldPrimary else BorderDark,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) DarkCardElevated else DarkCard,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) GoldLight else TextPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // مؤشر الاختيار الدائري
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) GoldPrimary else Color.Transparent)
                    .border(2.dp, if (selected) GoldPrimary else BorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                    )
                }
            }
        }
    }
}
