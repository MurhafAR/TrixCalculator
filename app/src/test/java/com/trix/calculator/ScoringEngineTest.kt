package com.trix.calculator

import com.trix.calculator.data.*
import org.junit.Assert.*
import org.junit.Test

class ScoringEngineTest {

    @Test
    fun testTrixScoring() {
        // ترتيب اللاعبين: اللاعب 0 أول، 1 ثانٍ، 2 ثالث، 3 رابع
        val rankings = mapOf(0 to 1, 1 to 2, 2 to 3, 3 to 4)
        val scores = ScoringEngine.calculateTrixScores(rankings)

        assertEquals(200, scores[0])
        assertEquals(150, scores[1])
        assertEquals(100, scores[2])
        assertEquals(50, scores[3])

        // مجموع نقاط التركس الإيجابية يجب أن يساوي +500
        assertEquals(500, scores.values.sum())
    }

    @Test
    fun testKingOfHeartsWithoutDoubling() {
        // اللاعب 1 أكل الختيار بدون تدبيل
        val scores = ScoringEngine.calculateKingScores(takerId = 1, doublerId = null, doublingEnabled = false)

        assertEquals(0, scores[0])
        assertEquals(-75, scores[1])
        assertEquals(0, scores[2])
        assertEquals(0, scores[3])
        assertEquals(-75, scores.values.sum())
    }

    @Test
    fun testKingOfHeartsWithDoubling() {
        // اللاعب 0 دبل الختيار، واللاعب 2 أكله
        val scores = ScoringEngine.calculateKingScores(takerId = 2, doublerId = 0, doublingEnabled = true)

        assertEquals(75, scores[0])    // المدبل يحصل على +75
        assertEquals(0, scores[1])
        assertEquals(-150, scores[2])  // الآكل ينزل عليه -150
        assertEquals(0, scores[3])

        // الفارق الصافي: -150 + 75 = -75 (يحافظ على وعاء النقاط)
        assertEquals(-75, scores.values.sum())
    }

    @Test
    fun testKingOfHeartsDoublerEatsOwnKing() {
        // اللاعب 3 دبل الختيار وأكله بنفسه
        val scores = ScoringEngine.calculateKingScores(takerId = 3, doublerId = 3, doublingEnabled = true)

        assertEquals(0, scores[0])
        assertEquals(0, scores[1])
        assertEquals(0, scores[2])
        assertEquals(-150, scores[3]) // ينزل عليه -150 كاملة دون مكافأة
    }

    @Test
    fun testQueensWithoutDoubling() {
        // توزيع 4 بنات: اللاعب 0 أكل 2، واللاعب 1 أكل 1، واللاعب 3 أكل 1
        val queensTaken = mapOf(0 to 2, 1 to 1, 2 to 0, 3 to 1)
        val scores = ScoringEngine.calculateQueensScores(queensTaken, emptyList(), doublingEnabled = false)

        assertEquals(-50, scores[0])
        assertEquals(-25, scores[1])
        assertEquals(0, scores[2])
        assertEquals(-25, scores[3])

        // المجموع = -100
        assertEquals(-100, scores.values.sum())
    }

    @Test
    fun testQueensWithDoubling() {
        // اللاعب 0 أكل بنتين (واحدة منهما دبلها اللاعب 2)
        // اللاعب 1 أكل بنتين عاديتين
        val queensTaken = mapOf(0 to 2, 1 to 2, 2 to 0, 3 to 0)
        val doubledQueens = listOf(
            DoubledQueenInfo(doublerPlayerId = 2, takerPlayerId = 0)
        )
        val scores = ScoringEngine.calculateQueensScores(queensTaken, doubledQueens, doublingEnabled = true)

        // اللاعب 0: بنت عادية (-25) + بنت مدبلة (-50) = -75
        assertEquals(-75, scores[0])
        // اللاعب 1: بنتان عاديتان (-50)
        assertEquals(-50, scores[1])
        // اللاعب 2: مكافأة تدبيل البنت التي أكلها اللاعب 0 = +25
        assertEquals(25, scores[2])
        // اللاعب 3: 0
        assertEquals(0, scores[3])

        // المجموع الكلي: -75 - 50 + 25 = -100
        assertEquals(-100, scores.values.sum())
    }

    @Test
    fun testDiamondsScoring() {
        // 13 كرت ديناري: اللاعب 0 أكل 5، اللاعب 1 أكل 3، اللاعب 2 أكل 3، اللاعب 3 أكل 2
        val diamondsTaken = mapOf(0 to 5, 1 to 3, 2 to 3, 3 to 2)
        val scores = ScoringEngine.calculateDiamondsScores(diamondsTaken)

        assertEquals(-50, scores[0])
        assertEquals(-30, scores[1])
        assertEquals(-30, scores[2])
        assertEquals(-20, scores[3])

        // المجموع الكلي للديناري: 13 * -10 = -130
        assertEquals(-130, scores.values.sum())
    }

    @Test
    fun testLutoScoring() {
        // 13 أكلة لطوش: اللاعب 0 أكل 4، اللاعب 1 أكل 4، اللاعب 2 أكل 3، اللاعب 3 أكل 2
        val lutoTaken = mapOf(0 to 4, 1 to 4, 2 to 3, 3 to 2)
        val scores = ScoringEngine.calculateLutoScores(lutoTaken)

        assertEquals(-60, scores[0])
        assertEquals(-60, scores[1])
        assertEquals(-45, scores[2])
        assertEquals(-30, scores[3])

        // المجموع الكلي لللطوش: 13 * -15 = -195
        assertEquals(-195, scores.values.sum())
    }

    @Test
    fun testComplexScoringBase() {
        // شوط كومبلكس كامل بدون تدبيل
        val kingTaker = 0
        val queensTaken = mapOf(0 to 1, 1 to 1, 2 to 1, 3 to 1)
        val diamondsTaken = mapOf(0 to 4, 1 to 3, 2 to 3, 3 to 3)
        val lutoTaken = mapOf(0 to 4, 1 to 3, 2 to 3, 3 to 3)

        val scores = ScoringEngine.calculateComplexScores(
            kingTakerId = kingTaker,
            kingDoublerId = null,
            queensTaken = queensTaken,
            doubledQueens = emptyList(),
            diamondsTaken = diamondsTaken,
            lutoTricksTaken = lutoTaken,
            doublingEnabled = false
        )

        // التحقق من أن مجموع السلبيات في الكومبلكس يساوي بالضبط -500 نقطة
        // ختيار (-75) + بنات (-100) + ديناري (-130) + لطوش (-195) = -500
        assertEquals(-500, scores.values.sum())
    }

    @Test
    fun testPartnershipScoresAndNetDifference() {
        val playerTotals = mapOf(
            0 to 100,  // فريق 1
            1 to -50,  // فريق 2
            2 to 50,   // فريق 1
            3 to -25   // فريق 2
        )

        val teamScores = ScoringEngine.calculateTeamScores(playerTotals)

        // فريق 1 = اللاعب 0 (100) + اللاعب 2 (50) = 150
        assertEquals(150, teamScores.team1Score)
        // فريق 2 = اللاعب 1 (-50) + اللاعب 3 (-25) = -75
        assertEquals(-75, teamScores.team2Score)
        // الفارق = 150 - (-75) = +225
        assertEquals(225, teamScores.netDifference)
        assertEquals(1, teamScores.leadingTeam)
    }

    @Test
    fun testValidationRules() {
        // فحص صحة مجموع الديناري (يجب 13)
        val invalidDiamonds = RoundInput(
            kingdomOwnerId = 0,
            contractType = ContractType.DIAMONDS,
            diamondsTaken = mapOf(0 to 5, 1 to 5) // المجموع 10 فقط!
        )
        val resultDiamonds = ScoringEngine.validateRoundInput(invalidDiamonds, false)
        assertFalse(resultDiamonds.isValid)
        assertTrue(resultDiamonds.errorMessage?.contains("13") == true)

        // فحص صحة مجموع اللطوش (يجب 13)
        val invalidLuto = RoundInput(
            kingdomOwnerId = 0,
            contractType = ContractType.LUTO,
            lutoTricksTaken = mapOf(0 to 14) // 14 أكلة!
        )
        val resultLuto = ScoringEngine.validateRoundInput(invalidLuto, false)
        assertFalse(resultLuto.isValid)

        // فحص صحة مجموع البنات (يجب 4)
        val validQueens = RoundInput(
            kingdomOwnerId = 0,
            contractType = ContractType.QUEENS,
            queensTaken = mapOf(0 to 2, 1 to 2, 2 to 0, 3 to 0)
        )
        val resultQueens = ScoringEngine.validateRoundInput(validQueens, false)
        assertTrue(resultQueens.isValid)
    }

    @Test
    fun testPartnershipKingScoring() {
        // الفريق 1 (اللاعب 0) أكل الختيار بدون تدبيل
        val scoresRegular = ScoringEngine.calculateKingScores(
            takerId = 0,
            doublerId = null,
            doublingEnabled = false,
            gameMode = GameMode.PARTNERS
        )
        assertEquals(-75, scoresRegular[0])
        assertEquals(0, scoresRegular[1])
        assertEquals(-75, scoresRegular.values.sum())

        // الفريق 1 أكل الختيار مدبلاً (-150 على الفريق 1، و+75 للفريق 2)
        val scoresDoubled = ScoringEngine.calculateKingScores(
            takerId = 0,
            doublerId = 1,
            doublingEnabled = true,
            gameMode = GameMode.PARTNERS
        )
        assertEquals(-150, scoresDoubled[0]) // الفريق 1
        assertEquals(75, scoresDoubled[1])   // الفريق 2
        // المجموع الصافي: -150 + 75 = -75
        assertEquals(-75, scoresDoubled.values.sum())

        // لو الفريق 2 (اللاعب 3) أكل الختيار مدبلاً
        val scoresDoubledT2 = ScoringEngine.calculateKingScores(
            takerId = 3,
            doublerId = 0,
            doublingEnabled = true,
            gameMode = GameMode.PARTNERS
        )
        assertEquals(75, scoresDoubledT2[0])   // الفريق 1 يحصل على +75
        assertEquals(-150, scoresDoubledT2[1]) // الفريق 2 ينزل عليه -150
        assertEquals(-75, scoresDoubledT2.values.sum())
    }

    @Test
    fun testPartnershipQueensScoring() {
        // الفريق 1 أكل 3 بنات (واحدة منها مدبلة)، والفريق 2 أكل بنت واحدة (مدبلة أيضاً)
        val queensTaken = mapOf(0 to 3, 1 to 1)
        val doubledQueens = listOf(
            DoubledQueenInfo(doublerPlayerId = 1, takerPlayerId = 0), // أكلها الفريق 1 مدبلة
            DoubledQueenInfo(doublerPlayerId = 0, takerPlayerId = 1)  // أكلها الفريق 2 مدبلة
        )
        val scores = ScoringEngine.calculateQueensScores(
            queensTaken = queensTaken,
            doubledQueens = doubledQueens,
            doublingEnabled = true,
            gameMode = GameMode.PARTNERS
        )

        // حسابات الفريق 1:
        // بنتان عاديتان: 2 * -25 = -50
        // بنت مدبلة: 1 * -50 = -50
        // مكافأة أكل الفريق 2 لبنت مدبلة: 1 * +25 = +25
        // المجموع = -50 + -50 + 25 = -75
        assertEquals(-75, scores[0])

        // حسابات الفريق 2:
        // صفر بنات عادية: 0
        // بنت واحدة مدبلة: 1 * -50 = -50
        // مكافأة أكل الفريق 1 لبنت مدبلة: 1 * +25 = +25
        // المجموع = -50 + 25 = -25
        assertEquals(-25, scores[1])

        // المجموع الكلي للنقاط: -75 + (-25) = -100
        assertEquals(-100, scores.values.sum())
    }

    @Test
    fun testPartnershipValidation() {
        // التحقق من صحة اختيار الختيار في الشراكة عند عدم تحديده
        val emptyKing = RoundInput(
            kingdomOwnerId = 0,
            contractType = ContractType.KING_OF_HEARTS,
            kingTakerId = null
        )
        val resultKing = ScoringEngine.validateRoundInput(emptyKing, false, GameMode.PARTNERS)
        assertFalse(resultKing.isValid)
        assertTrue(resultKing.errorMessage?.contains("الفريق") == true)

        // التحقق من تدبيل بنات أكثر مما أكله الفريق
        val invalidDoubledQueens = RoundInput(
            kingdomOwnerId = 0,
            contractType = ContractType.QUEENS,
            queensTaken = mapOf(0 to 1, 1 to 3),
            doubledQueens = listOf(
                DoubledQueenInfo(1, 0),
                DoubledQueenInfo(1, 0) // الفريق 1 أكل بنت واحدة فقط، لا يمكن أن يكون أكل اثنتين مدبلتين!
            )
        )
        val resultQueens = ScoringEngine.validateRoundInput(invalidDoubledQueens, true, GameMode.PARTNERS)
        assertFalse(resultQueens.isValid)
        assertTrue(resultQueens.errorMessage?.contains("الفريق 1") == true)
    }
}
