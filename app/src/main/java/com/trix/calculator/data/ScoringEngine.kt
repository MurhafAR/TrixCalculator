package com.trix.calculator.data

/**
 * محرك حساب النقاط حسب القواعد السورية الأصيلة للعبة التركس والكومبلكس
 */
object ScoringEngine {

    const val KING_BASE_PENALTY = -75
    const val KING_DOUBLED_PENALTY = -150
    const val KING_DOUBLER_REWARD = 75

    const val QUEEN_BASE_PENALTY = -25
    const val QUEEN_DOUBLED_PENALTY = -50
    const val QUEEN_DOUBLER_REWARD = 25

    const val DIAMOND_PENALTY = -10
    const val TOTAL_DIAMONDS = 13

    const val LUTO_TRICK_PENALTY = -15
    const val TOTAL_LUTO_TRICKS = 13

    val TRIX_RANK_SCORES = mapOf(
        1 to 200,
        2 to 150,
        3 to 100,
        4 to 50
    )

    /**
     * حساب نقاط التركس (إيجابية دائماً: 200، 150، 100، 50)
     */
    fun calculateTrixScores(trixRankings: Map<Int, Int>): Map<Int, Int> {
        val result = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        for (playerId in 0..3) {
            val rank = trixRankings[playerId] ?: 4
            result[playerId] = TRIX_RANK_SCORES[rank] ?: 50
        }
        return result
    }

    /**
     * حساب نقاط ختيار الكبة (الشيخ)
     * مع تطبيق قواعد التدبيل السورية:
     * - في الفردي:
     *   - إذا لم يدبل: الآكل -75
     *   - إذا دبل: الآكل -150، والمدبل +75
     *   - إذا دبل اللاعب الختيار وأكله بنفسه: ينزل عليه -150 كاملة
     * - في الشراكة:
     *   - إذا لم يدبل: الفريق الآكل -75
     *   - إذا دبل: الفريق الآكل ينزل عليه -150، والفريق الآخر الذي لم يأكل يحصل على +75
     */
    fun calculateKingScores(
        takerId: Int,
        doublerId: Int?,
        doublingEnabled: Boolean,
        gameMode: GameMode = GameMode.INDIVIDUAL
    ): Map<Int, Int> {
        val scores = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)

        if (gameMode == GameMode.PARTNERS) {
            val takerTeam = if (takerId == 0 || takerId == 2) 1 else 2
            val takerPrimaryId = if (takerTeam == 1) 0 else 1
            val otherPrimaryId = if (takerTeam == 1) 1 else 0

            if (!doublingEnabled || doublerId == null) {
                // بدون تدبيل: -75 للفريق الآكل
                scores[takerPrimaryId] = KING_BASE_PENALTY
            } else {
                // مع التدبيل: -150 للفريق الآكل، و+75 للفريق الآخر الذي لم يأكل
                scores[takerPrimaryId] = KING_DOUBLED_PENALTY
                scores[otherPrimaryId] = KING_DOUBLER_REWARD
            }
            return scores
        }

        if (!doublingEnabled || doublerId == null) {
            // بدون تدبيل
            scores[takerId] = KING_BASE_PENALTY
        } else {
            // مع التدبيل
            scores[takerId] = KING_DOUBLED_PENALTY
            if (doublerId != takerId) {
                scores[doublerId] = (scores[doublerId] ?: 0) + KING_DOUBLER_REWARD
            }
        }

        return scores
    }

    /**
     * حساب نقاط البنات
     * 4 بنات، -25 لكل بنت
     * مع التدبيل:
     * - في الفردي: البنت المدبلة -50 على الآكل، و+25 للمدبل (إلا إذا أكلها المدبل بنفسه فتكون -50 عليه)
     * - في الشراكة: البنت المدبلة -50 على الفريق الآكل، و+25 للفريق الآخر الذي لم يأكلها
     */
    fun calculateQueensScores(
        queensTaken: Map<Int, Int>,
        doubledQueens: List<DoubledQueenInfo>,
        doublingEnabled: Boolean,
        gameMode: GameMode = GameMode.INDIVIDUAL
    ): Map<Int, Int> {
        val scores = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)

        if (gameMode == GameMode.PARTNERS) {
            val team1Queens = (queensTaken[0] ?: 0) + (queensTaken[2] ?: 0)
            val team2Queens = (queensTaken[1] ?: 0) + (queensTaken[3] ?: 0)

            if (!doublingEnabled || doubledQueens.isEmpty()) {
                scores[0] = team1Queens * QUEEN_BASE_PENALTY
                scores[1] = team2Queens * QUEEN_BASE_PENALTY
                return scores
            }

            // حساب البنات المدبلة التي أكلها كل فريق
            var doubledTeam1 = 0
            var doubledTeam2 = 0
            for (info in doubledQueens) {
                if (info.takerPlayerId == 0 || info.takerPlayerId == 2) {
                    doubledTeam1++
                } else if (info.takerPlayerId == 1 || info.takerPlayerId == 3) {
                    doubledTeam2++
                }
            }

            val undoubledTeam1 = (team1Queens - doubledTeam1).coerceAtLeast(0)
            val undoubledTeam2 = (team2Queens - doubledTeam2).coerceAtLeast(0)

            // الفريق 1: بناته العادية (-25) + بناته المدبلة (-50) + مكافأة البنات المدبلة التي أكلها الفريق 2 (+25)
            val team1Score = (undoubledTeam1 * QUEEN_BASE_PENALTY) +
                    (doubledTeam1 * QUEEN_DOUBLED_PENALTY) +
                    (doubledTeam2 * QUEEN_DOUBLER_REWARD)

            // الفريق 2: بناته العادية (-25) + بناته المدبلة (-50) + مكافأة البنات المدبلة التي أكلها الفريق 1 (+25)
            val team2Score = (undoubledTeam2 * QUEEN_BASE_PENALTY) +
                    (doubledTeam2 * QUEEN_DOUBLED_PENALTY) +
                    (doubledTeam1 * QUEEN_DOUBLER_REWARD)

            scores[0] = team1Score
            scores[1] = team2Score
            return scores
        }

        if (!doublingEnabled || doubledQueens.isEmpty()) {
            for (playerId in 0..3) {
                val count = queensTaken[playerId] ?: 0
                scores[playerId] = count * QUEEN_BASE_PENALTY
            }
            return scores
        }

        // حساب عدد البنات المدبلة التي أكلها كل لاعب
        val doubledCountPerTaker = mutableMapOf<Int, Int>()
        for (info in doubledQueens) {
            doubledCountPerTaker[info.takerPlayerId] =
                (doubledCountPerTaker[info.takerPlayerId] ?: 0) + 1
        }

        for (playerId in 0..3) {
            val totalTaken = queensTaken[playerId] ?: 0
            val doubledTaken = doubledCountPerTaker[playerId] ?: 0
            val undoubledTaken = (totalTaken - doubledTaken).coerceAtLeast(0)

            // البنات العادية (-25) + البنات المدبلة (-50)
            scores[playerId] = (undoubledTaken * QUEEN_BASE_PENALTY) + (doubledTaken * QUEEN_DOUBLED_PENALTY)
        }

        // إضافة مكافآت المدبلين (+25) لمن لم يأكل بنته بنفسه
        for (info in doubledQueens) {
            if (info.doublerPlayerId != info.takerPlayerId) {
                scores[info.doublerPlayerId] = (scores[info.doublerPlayerId] ?: 0) + QUEEN_DOUBLER_REWARD
            }
        }

        return scores
    }

    /**
     * حساب نقاط الديناري (13 كرت ديناري، -10 لكل كرت)
     */
    fun calculateDiamondsScores(diamondsTaken: Map<Int, Int>): Map<Int, Int> {
        val scores = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        for (playerId in 0..3) {
            val count = diamondsTaken[playerId] ?: 0
            scores[playerId] = count * DIAMOND_PENALTY
        }
        return scores
    }

    /**
     * حساب نقاط اللطوش (13 أكلة، -15 لكل أكلة)
     */
    fun calculateLutoScores(lutoTricksTaken: Map<Int, Int>): Map<Int, Int> {
        val scores = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        for (playerId in 0..3) {
            val count = lutoTricksTaken[playerId] ?: 0
            scores[playerId] = count * LUTO_TRICK_PENALTY
        }
        return scores
    }

    /**
     * حساب نقاط الكومبلكس (دمج الختيار، البنات، الديناري، واللطوش)
     * المجموع الإجمالي السلبي للقاعدة = -500 نقطة
     */
    fun calculateComplexScores(
        kingTakerId: Int?,
        kingDoublerId: Int?,
        queensTaken: Map<Int, Int>,
        doubledQueens: List<DoubledQueenInfo>,
        diamondsTaken: Map<Int, Int>,
        lutoTricksTaken: Map<Int, Int>,
        doublingEnabled: Boolean,
        gameMode: GameMode = GameMode.INDIVIDUAL
    ): Map<Int, Int> {
        val scores = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)

        // 1. ختيار الكبة
        if (kingTakerId != null) {
            val kingScores = calculateKingScores(kingTakerId, kingDoublerId, doublingEnabled, gameMode)
            for (playerId in 0..3) {
                scores[playerId] = (scores[playerId] ?: 0) + (kingScores[playerId] ?: 0)
            }
        }

        // 2. البنات
        val queensScores = calculateQueensScores(queensTaken, doubledQueens, doublingEnabled, gameMode)
        for (playerId in 0..3) {
            scores[playerId] = (scores[playerId] ?: 0) + (queensScores[playerId] ?: 0)
        }

        // 3. الديناري
        val diamondsScores = calculateDiamondsScores(diamondsTaken)
        for (playerId in 0..3) {
            scores[playerId] = (scores[playerId] ?: 0) + (diamondsScores[playerId] ?: 0)
        }

        // 4. اللطوش
        val lutoScores = calculateLutoScores(lutoTricksTaken)
        for (playerId in 0..3) {
            scores[playerId] = (scores[playerId] ?: 0) + (lutoScores[playerId] ?: 0)
        }

        return scores
    }

    /**
     * حساب نقاط الجولة العامة بناءً على نوع العقد والمدخلات
     */
    fun calculateRoundScores(
        input: RoundInput,
        doublingEnabled: Boolean,
        gameMode: GameMode = GameMode.INDIVIDUAL
    ): Map<Int, Int> {
        return when (input.contractType) {
            ContractType.TRIX -> calculateTrixScores(input.trixRankings)
            ContractType.KING_OF_HEARTS -> {
                val taker = input.kingTakerId ?: 0
                calculateKingScores(taker, input.kingDoublerId, doublingEnabled, gameMode)
            }
            ContractType.QUEENS -> {
                calculateQueensScores(input.queensTaken, input.doubledQueens, doublingEnabled, gameMode)
            }
            ContractType.DIAMONDS -> {
                calculateDiamondsScores(input.diamondsTaken)
            }
            ContractType.LUTO -> {
                calculateLutoScores(input.lutoTricksTaken)
            }
            ContractType.COMPLEX -> {
                calculateComplexScores(
                    kingTakerId = input.kingTakerId,
                    kingDoublerId = input.kingDoublerId,
                    queensTaken = input.queensTaken,
                    doubledQueens = input.doubledQueens,
                    diamondsTaken = input.diamondsTaken,
                    lutoTricksTaken = input.lutoTricksTaken,
                    doublingEnabled = doublingEnabled,
                    gameMode = gameMode
                )
            }
        }
    }

    /**
     * حساب النقاط التراكمية لكل لاعب عبر جميع الجولات
     */
    fun calculateCumulativeScores(rounds: List<RoundResult>): Map<Int, Int> {
        val totals = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        for (round in rounds) {
            for (playerId in 0..3) {
                totals[playerId] = (totals[playerId] ?: 0) + (round.playerScores[playerId] ?: 0)
            }
        }
        return totals
    }

    /**
     * حساب نقاط فرق الشراكة
     * الفريق الأول = اللاعب 0 + اللاعب 2
     * الفريق الثاني = اللاعب 1 + اللاعب 3
     */
    fun calculateTeamScores(playerTotals: Map<Int, Int>): TeamScores {
        val team1 = (playerTotals[0] ?: 0) + (playerTotals[2] ?: 0)
        val team2 = (playerTotals[1] ?: 0) + (playerTotals[3] ?: 0)
        val diff = team1 - team2
        val leader = when {
            diff > 0 -> 1
            diff < 0 -> 2
            else -> 0
        }
        return TeamScores(
            team1Score = team1,
            team2Score = team2,
            netDifference = diff,
            leadingTeam = leader
        )
    }

    /**
     * التحقق الصارم من صحة المدخلات حسب قواعد التركس السورية
     */
    fun validateRoundInput(
        input: RoundInput,
        doublingEnabled: Boolean,
        gameMode: GameMode = GameMode.INDIVIDUAL
    ): ValidationResult {
        when (input.contractType) {
            ContractType.TRIX -> {
                val ranks = input.trixRankings.values
                if (ranks.size != 4 || ranks.toSet().size != 4) {
                    return ValidationResult(false, "يجب تحديد ترتيب فريد (1 إلى 4) لكل لاعب من اللاعبين الأربعة في التركس.")
                }
                for (rank in 1..4) {
                    if (!ranks.contains(rank)) {
                        return ValidationResult(false, "المركز $rank غير محدد لأي لاعب.")
                    }
                }
            }
            ContractType.KING_OF_HEARTS -> {
                if (input.kingTakerId == null || input.kingTakerId !in 0..3) {
                    val msg = if (gameMode == GameMode.PARTNERS) "يرجى تحديد الفريق الذي أكل ختيار الكبة." else "يرجى تحديد اللاعب الذي أكل ختيار الكبة."
                    return ValidationResult(false, msg)
                }
                if (doublingEnabled && input.kingDoublerId != null && input.kingDoublerId !in 0..3) {
                    return ValidationResult(false, "جهة التدبيل غير صالحة.")
                }
            }
            ContractType.QUEENS -> {
                val totalQueens = (0..3).sumOf { input.queensTaken[it] ?: 0 }
                if (totalQueens != 4) {
                    return ValidationResult(false, "مجموع البنات يجب أن يساوي 4 بالضبط. المجموع الحالي: $totalQueens")
                }
                if (doublingEnabled && input.doubledQueens.isNotEmpty()) {
                    if (input.doubledQueens.size > 4) {
                        return ValidationResult(false, "لا يمكن تدبيل أكثر من 4 بنات.")
                    }
                    if (gameMode == GameMode.PARTNERS) {
                        val team1Queens = (input.queensTaken[0] ?: 0) + (input.queensTaken[2] ?: 0)
                        val team2Queens = (input.queensTaken[1] ?: 0) + (input.queensTaken[3] ?: 0)
                        val doubledTeam1 = input.doubledQueens.count { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                        val doubledTeam2 = input.doubledQueens.count { it.takerPlayerId == 1 || it.takerPlayerId == 3 }
                        if (doubledTeam1 > team1Queens) {
                            return ValidationResult(false, "الفريق 1 أكل $team1Queens بنات فقط، فلا يمكن تدبيل $doubledTeam1 بنات له!")
                        }
                        if (doubledTeam2 > team2Queens) {
                            return ValidationResult(false, "الفريق 2 أكل $team2Queens بنات فقط، فلا يمكن تدبيل $doubledTeam2 بنات له!")
                        }
                    } else {
                        val doubledPerTaker = input.doubledQueens.groupingBy { it.takerPlayerId }.eachCount()
                        for ((takerId, count) in doubledPerTaker) {
                            val taken = input.queensTaken[takerId] ?: 0
                            if (count > taken) {
                                return ValidationResult(
                                    false,
                                    "اللاعب رقم ${takerId + 1} أكل $taken بنات فقط، فلا يمكن أن يأكل $count بنات مدبلة!"
                                )
                            }
                        }
                    }
                }
            }
            ContractType.DIAMONDS -> {
                val totalDiamonds = (0..3).sumOf { input.diamondsTaken[it] ?: 0 }
                if (totalDiamonds != TOTAL_DIAMONDS) {
                    return ValidationResult(false, "مجموع كروت الديناري يجب أن يساوي 13 بالضبط. المجموع الحالي: $totalDiamonds")
                }
            }
            ContractType.LUTO -> {
                val totalLuto = (0..3).sumOf { input.lutoTricksTaken[it] ?: 0 }
                if (totalLuto != TOTAL_LUTO_TRICKS) {
                    return ValidationResult(false, "مجموع اللطوش (الأكلات) يجب أن يساوي 13 بالضبط. المجموع الحالي: $totalLuto")
                }
            }
            ContractType.COMPLEX -> {
                // في الكومبلكس: فحص الختيار
                if (input.kingTakerId == null || input.kingTakerId !in 0..3) {
                    val msg = if (gameMode == GameMode.PARTNERS) "يرجى تحديد الفريق الذي أكل ختيار الكبة في الكومبلكس." else "يرجى تحديد اللاعب الذي أكل ختيار الكبة في الكومبلكس."
                    return ValidationResult(false, msg)
                }
                // فحص البنات
                val totalQueens = (0..3).sumOf { input.queensTaken[it] ?: 0 }
                if (totalQueens != 4) {
                    return ValidationResult(false, "مجموع كروت البنات في الكومبلكس يجب أن يساوي 4. المجموع الحالي: $totalQueens")
                }
                // فحص تدبيل البنات إن وُجد
                if (doublingEnabled && input.doubledQueens.isNotEmpty()) {
                    if (input.doubledQueens.size > 4) {
                        return ValidationResult(false, "لا يمكن تدبيل أكثر من 4 بنات في الكومبلكس.")
                    }
                    if (gameMode == GameMode.PARTNERS) {
                        val team1Queens = (input.queensTaken[0] ?: 0) + (input.queensTaken[2] ?: 0)
                        val team2Queens = (input.queensTaken[1] ?: 0) + (input.queensTaken[3] ?: 0)
                        val doubledTeam1 = input.doubledQueens.count { it.takerPlayerId == 0 || it.takerPlayerId == 2 }
                        val doubledTeam2 = input.doubledQueens.count { it.takerPlayerId == 1 || it.takerPlayerId == 3 }
                        if (doubledTeam1 > team1Queens) {
                            return ValidationResult(false, "في الكومبلكس: الفريق 1 أكل $team1Queens بنات فقط، فلا يمكن تدبيل $doubledTeam1 بنات له!")
                        }
                        if (doubledTeam2 > team2Queens) {
                            return ValidationResult(false, "في الكومبلكس: الفريق 2 أكل $team2Queens بنات فقط، فلا يمكن تدبيل $doubledTeam2 بنات له!")
                        }
                    } else {
                        val doubledPerTaker = input.doubledQueens.groupingBy { it.takerPlayerId }.eachCount()
                        for ((takerId, count) in doubledPerTaker) {
                            val taken = input.queensTaken[takerId] ?: 0
                            if (count > taken) {
                                return ValidationResult(
                                    false,
                                    "في الكومبلكس: اللاعب رقم ${takerId + 1} أكل $taken بنات فقط، فلا يمكن أن يأكل $count بنات مدبلة!"
                                )
                            }
                        }
                    }
                }
                // فحص الديناري
                val totalDiamonds = (0..3).sumOf { input.diamondsTaken[it] ?: 0 }
                if (totalDiamonds != TOTAL_DIAMONDS) {
                    return ValidationResult(false, "مجموع كروت الديناري في الكومبلكس يجب أن يساوي 13. المجموع الحالي: $totalDiamonds")
                }
                // فحص اللطوش
                val totalLuto = (0..3).sumOf { input.lutoTricksTaken[it] ?: 0 }
                if (totalLuto != TOTAL_LUTO_TRICKS) {
                    return ValidationResult(false, "مجموع اللطوش في الكومبلكس يجب أن يساوي 13. المجموع الحالي: $totalLuto")
                }
            }
        }
        return ValidationResult(true)
    }
}
