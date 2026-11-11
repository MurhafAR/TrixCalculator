package com.trix.calculator.data

import java.util.UUID

/**
 * نظام اللعب: فردي (يهودي) أو شراكة
 */
enum class GameMode(val titleAr: String, val descriptionAr: String) {
    INDIVIDUAL("فردي (يهودي)", "كل لاعب يلعب لنفسه برصيد مستقل"),
    PARTNERS("شراكة", "كل لاعبين متقابلين يشكلان فريقاً واحداً")
}

/**
 * نوع اللعبة: عادي (5 طلبات لكل مملكة) أو كومبلكس (طلبان: كومبلكس وتركس)
 */
enum class GameType(val titleAr: String, val descriptionAr: String, val contractsPerKingdom: Int) {
    REGULAR("عادي", "5 طلبات لكل مملكة (ختيار، بنات، ديناري، لطوش، تركس)", 5),
    COMPLEX("كومبلكس", "طلبان لكل مملكة (كومبلكس وتركس)", 2)
}

/**
 * طريقة التسجيل في حالة الشراكة:
 * تسجيل لفريقين (حساب مجموع كل فريق بشكل منفصل)
 * أو تسجيل على فريق واحد (تسجيل الفارق التراكمي بين الفريقين)
 */
enum class PartnerScoringRule(val titleAr: String, val descriptionAr: String) {
    BOTH_TEAMS("تسجيل لفريقين", "عرض مجموع نقاط الفريق الأول والفريق الثاني جنباً إلى جنب"),
    SINGLE_TEAM_NET("تسجيل على فريق واحد (الفارق)", "حساب الفارق المباشر بين الفريقين (موجب أو سالب)")
}

/**
 * أنواع عقود وطلبات التركس السورية
 */
enum class ContractType(
    val titleAr: String,
    val iconEmoji: String,
    val basePointsDescriptionAr: String,
    val isTrix: Boolean = false
) {
    KING_OF_HEARTS("ختيار الكبة (الشيخ)", "♥️👑", "-75 نقطة (-150 مع التدبيل)"),
    QUEENS("البنات", "👸", "-25 نقطة لكل بنت (-50 مع التدبيل)"),
    DIAMONDS("الديناري", "♦️", "-10 نقاط لكل ديناري (13 كرت = -130)"),
    LUTO("اللطوش (اللطو)", "🃏", "-15 نقطة لكل أكلة (13 أكلة = -195)"),
    TRIX("تركس", "🏆", "الأول: +200، الثاني: +150، الثالث: +100، الرابع: +50", isTrix = true),
    COMPLEX("كومبلكس", "⚡", "يجمع الختيار والبنات والديناري واللطوش (-500 نقطة إجمالية)")
}

/**
 * تفاصيل تدبيل البنت: من قام بالتدبيل ومن أكل البنت المدبلة
 */
data class DoubledQueenInfo(
    val doublerPlayerId: Int, // اللاعب الذي دبل البنت
    val takerPlayerId: Int    // اللاعب الذي أكل البنت
)

/**
 * مدخلات الجولة الواحدة لحساب النقاط
 */
data class RoundInput(
    val kingdomOwnerId: Int, // صاحب المملكة (0..3)
    val contractType: ContractType, // نوع الطلب
    val trixRankings: Map<Int, Int> = emptyMap(), // في التركس: معرف اللاعب -> المركز (1..4)
    val kingTakerId: Int? = null, // من أكل ختيار الكبة
    val kingDoublerId: Int? = null, // من دبل ختيار الكبة (null إذا لم يدبل)
    val queensTaken: Map<Int, Int> = emptyMap(), // معرف اللاعب -> عدد البنات (0..4)
    val doubledQueens: List<DoubledQueenInfo> = emptyList(), // البنات المدبلة
    val diamondsTaken: Map<Int, Int> = emptyMap(), // معرف اللاعب -> عدد الديناري (0..13)
    val lutoTricksTaken: Map<Int, Int> = emptyMap() // معرف اللاعب -> عدد اللطوش (0..13)
)

/**
 * نتيجة الجولة المحسوبة
 */
data class RoundResult(
    val roundId: String = UUID.randomUUID().toString(),
    val roundNumber: Int,
    val kingdomOwnerId: Int,
    val contractType: ContractType,
    val playerScores: Map<Int, Int>, // معرف اللاعب -> النقاط المكتسبة أو المخصومة في هذا الدور
    val inputDetails: RoundInput,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * إعدادات اللعبة الأولية
 */
data class GameConfig(
    val gameMode: GameMode = GameMode.PARTNERS,
    val gameType: GameType = GameType.COMPLEX,
    val doublingEnabled: Boolean = true,
    val partnerScoringRule: PartnerScoringRule = PartnerScoringRule.BOTH_TEAMS,
    val playerNames: List<String> = listOf("اللاعب 1", "اللاعب 2", "اللاعب 3", "اللاعب 4")
) {
    /**
     * في الشراكة:
     * الفريق الأول: اللاعب 0 واللاعب 2 (المتقابلان)
     * الفريق الثاني: اللاعب 1 واللاعب 3 (المتقابلان)
     */
    fun getTeamName(teamNumber: Int): String {
        return if (teamNumber == 1) {
            "الفريق الأول (${playerNames.getOrElse(0) { "1" }} و ${playerNames.getOrElse(2) { "3" }})"
        } else {
            "الفريق الثاني (${playerNames.getOrElse(1) { "2" }} و ${playerNames.getOrElse(3) { "4" }})"
        }
    }
}

/**
 * نتائج فرق الشراكة
 */
data class TeamScores(
    val team1Score: Int, // مجموع نقاط اللاعب 0 واللاعب 2
    val team2Score: Int, // مجموع نقاط اللاعب 1 واللاعب 3
    val netDifference: Int, // الفارق: team1Score - team2Score
    val leadingTeam: Int // 1 أو 2 أو 0 (تعادل)
)

/**
 * نتيجة التحقق من صحة المدخلات
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
