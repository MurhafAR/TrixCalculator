package com.trix.calculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.trix.calculator.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppScreen {
    CONFIG,
    GAME_DASHBOARD,
    GAME_OVER
}

data class TrixUiState(
    val currentScreen: AppScreen = AppScreen.CONFIG,
    val config: GameConfig = GameConfig(),
    val rounds: List<RoundResult> = emptyList(),
    val undoneRounds: List<RoundResult> = emptyList(),
    val activeKingdomOwnerId: Int = 0,
    val isRoundInputActive: Boolean = false,
    val editingRoundIndex: Int? = null, // إذا كان يعدل جولة سابقة
    val draftInput: RoundInput = RoundInput(kingdomOwnerId = 0, contractType = ContractType.TRIX),
    val validationError: String? = null,
    val showHistoryDialog: Boolean = false,
    val showRulesDialog: Boolean = false,
    val showResetConfirmDialog: Boolean = false
) {
    // النقاط التراكمية لكل لاعب (0..3)
    val cumulativeScores: Map<Int, Int>
        get() = ScoringEngine.calculateCumulativeScores(rounds)

    // نقاط الفرق إذا كانت اللعبة شراكة
    val teamScores: TeamScores
        get() = ScoringEngine.calculateTeamScores(cumulativeScores)

    // العقود المنفذة لكل مملكة: معرف الملك -> مجموعة العقود المنتهية
    val completedContractsPerKingdom: Map<Int, Set<ContractType>>
        get() {
            val map = mutableMapOf<Int, MutableSet<ContractType>>(
                0 to mutableSetOf(),
                1 to mutableSetOf(),
                2 to mutableSetOf(),
                3 to mutableSetOf()
            )
            for (round in rounds) {
                map[round.kingdomOwnerId]?.add(round.contractType)
            }
            return map
        }

    // إجمالي الجولات المتوقعة في اللعبة (20 في العادي، 8 في الكومبلكس)
    val totalExpectedRounds: Int
        get() = 4 * config.gameType.contractsPerKingdom

    // هل انتهت اللعبة بالكامل؟
    val isGameCompleted: Boolean
        get() = rounds.size >= totalExpectedRounds
}

class TrixViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrixUiState())
    val uiState: StateFlow<TrixUiState> = _uiState.asStateFlow()

    // ----------------------------------------------------
    // إدارة الإعدادات الأولية (Configuration)
    // ----------------------------------------------------

    fun setGameMode(mode: GameMode) {
        _uiState.update { it.copy(config = it.config.copy(gameMode = mode)) }
    }

    fun setGameType(type: GameType) {
        _uiState.update { it.copy(config = it.config.copy(gameType = type)) }
    }

    fun setDoublingEnabled(enabled: Boolean) {
        _uiState.update { it.copy(config = it.config.copy(doublingEnabled = enabled)) }
    }

    fun setPartnerScoringRule(rule: PartnerScoringRule) {
        _uiState.update { it.copy(config = it.config.copy(partnerScoringRule = rule)) }
    }

    fun setPlayerName(index: Int, name: String) {
        _uiState.update { state ->
            val updatedNames = state.config.playerNames.toMutableList()
            if (index in updatedNames.indices) {
                updatedNames[index] = name
            }
            state.copy(config = state.config.copy(playerNames = updatedNames))
        }
    }

    fun startGame() {
        val defaultContract = if (_uiState.value.config.gameType == GameType.COMPLEX) {
            ContractType.COMPLEX
        } else {
            ContractType.KING_OF_HEARTS
        }
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME_DASHBOARD,
                activeKingdomOwnerId = 0,
                rounds = emptyList(),
                undoneRounds = emptyList(),
                draftInput = RoundInput(kingdomOwnerId = 0, contractType = defaultContract)
            )
        }
    }

    // ----------------------------------------------------
    // إدارة الجولات والتسجيل (Rounds & Scoring)
    // ----------------------------------------------------

    /**
     * إرجاع العقود المتاحة للمملكة المحددة (التي لم تُلعب بعد)
     */
    fun getAvailableContracts(kingdomOwnerId: Int): List<ContractType> {
        val allContracts = if (_uiState.value.config.gameType == GameType.COMPLEX) {
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
        val completed = _uiState.value.completedContractsPerKingdom[kingdomOwnerId] ?: emptySet()
        return allContracts.filter { it !in completed }
    }

    /**
     * فتح نافذة تسجيل دور جديد
     */
    fun openNewRoundInput(kingdomOwnerId: Int? = null) {
        val state = _uiState.value
        val targetOwner = kingdomOwnerId ?: state.activeKingdomOwnerId
        val available = getAvailableContracts(targetOwner)
        val initialContract = available.firstOrNull() ?: if (state.config.gameType == GameType.COMPLEX) ContractType.COMPLEX else ContractType.TRIX

        _uiState.update {
            it.copy(
                isRoundInputActive = true,
                editingRoundIndex = null,
                draftInput = RoundInput(
                    kingdomOwnerId = targetOwner,
                    contractType = initialContract
                ),
                validationError = null
            )
        }
    }

    /**
     * فتح نافذة تعديل جولة سابقة
     */
    fun openEditRound(roundIndex: Int) {
        val state = _uiState.value
        if (roundIndex !in state.rounds.indices) return
        val roundToEdit = state.rounds[roundIndex]

        _uiState.update {
            it.copy(
                isRoundInputActive = true,
                editingRoundIndex = roundIndex,
                draftInput = roundToEdit.inputDetails,
                validationError = null,
                showHistoryDialog = false
            )
        }
    }

    fun dismissRoundInput() {
        _uiState.update {
            it.copy(
                isRoundInputActive = false,
                editingRoundIndex = null,
                validationError = null
            )
        }
    }

    fun updateDraftInput(modifier: (RoundInput) -> RoundInput) {
        _uiState.update {
            it.copy(
                draftInput = modifier(it.draftInput),
                validationError = null
            )
        }
    }

    /**
     * حفظ الجولة الحالية (إما جديدة أو معدلة) بعد التحقق الصارم من القواعد
     */
    fun submitDraftRound(): Boolean {
        val state = _uiState.value
        val draft = state.draftInput
        val validation = ScoringEngine.validateRoundInput(draft, state.config.doublingEnabled, state.config.gameMode)

        if (!validation.isValid) {
            _uiState.update { it.copy(validationError = validation.errorMessage) }
            return false
        }

        // حساب نقاط الدور
        val calculatedScores = ScoringEngine.calculateRoundScores(draft, state.config.doublingEnabled, state.config.gameMode)

        if (state.editingRoundIndex != null) {
            // تعديل جولة سابقة في مكانها
            val updatedRounds = state.rounds.toMutableList()
            val existing = updatedRounds[state.editingRoundIndex]
            updatedRounds[state.editingRoundIndex] = existing.copy(
                kingdomOwnerId = draft.kingdomOwnerId,
                contractType = draft.contractType,
                playerScores = calculatedScores,
                inputDetails = draft
            )

            _uiState.update {
                it.copy(
                    rounds = updatedRounds,
                    isRoundInputActive = false,
                    editingRoundIndex = null,
                    validationError = null
                )
            }
        } else {
            // إضافة جولة جديدة
            val newRound = RoundResult(
                roundNumber = state.rounds.size + 1,
                kingdomOwnerId = draft.kingdomOwnerId,
                contractType = draft.contractType,
                playerScores = calculatedScores,
                inputDetails = draft
            )

            val updatedRounds = state.rounds + newRound

            // فحص الانتقال للمملكة التالية إذا أنهى الملك الحالي كل طلباته
            val completedForThisOwner = updatedRounds.count { it.kingdomOwnerId == draft.kingdomOwnerId }
            val nextOwner = if (completedForThisOwner >= state.config.gameType.contractsPerKingdom) {
                // الانتقال للاعب التالي (0 -> 1 -> 2 -> 3)
                (draft.kingdomOwnerId + 1) % 4
            } else {
                draft.kingdomOwnerId
            }

            val isOver = updatedRounds.size >= (4 * state.config.gameType.contractsPerKingdom)

            _uiState.update {
                it.copy(
                    rounds = updatedRounds,
                    undoneRounds = emptyList(), // مسح التراجع عند تسجيل جولة جديدة
                    activeKingdomOwnerId = nextOwner,
                    isRoundInputActive = false,
                    editingRoundIndex = null,
                    validationError = null,
                    currentScreen = if (isOver) AppScreen.GAME_OVER else AppScreen.GAME_DASHBOARD
                )
            }
        }
        return true
    }

    /**
     * التراجع عن الجولة الأخيرة (Undo)
     */
    fun undoLastRound() {
        val state = _uiState.value
        if (state.rounds.isEmpty()) return

        val lastRound = state.rounds.last()
        val remainingRounds = state.rounds.dropLast(1)

        _uiState.update {
            it.copy(
                rounds = remainingRounds,
                undoneRounds = it.undoneRounds + lastRound,
                activeKingdomOwnerId = lastRound.kingdomOwnerId,
                currentScreen = AppScreen.GAME_DASHBOARD
            )
        }
    }

    /**
     * إعادة الجولة المتراجع عنها (Redo)
     */
    fun redoLastUndoneRound() {
        val state = _uiState.value
        if (state.undoneRounds.isEmpty()) return

        val roundToRestore = state.undoneRounds.last()
        val remainingUndone = state.undoneRounds.dropLast(1)
        val updatedRounds = state.rounds + roundToRestore

        _uiState.update {
            it.copy(
                rounds = updatedRounds,
                undoneRounds = remainingUndone,
                currentScreen = if (updatedRounds.size >= it.totalExpectedRounds) AppScreen.GAME_OVER else AppScreen.GAME_DASHBOARD
            )
        }
    }

    /**
     * حذف جولة محددة من السجل
     */
    fun deleteRound(roundIndex: Int) {
        val state = _uiState.value
        if (roundIndex !in state.rounds.indices) return

        val updated = state.rounds.toMutableList()
        updated.removeAt(roundIndex)

        // إعادة ترقيم الجولات
        val renumbered = updated.mapIndexed { index, roundResult ->
            roundResult.copy(roundNumber = index + 1)
        }

        _uiState.update {
            it.copy(
                rounds = renumbered,
                currentScreen = AppScreen.GAME_DASHBOARD
            )
        }
    }

    // ----------------------------------------------------
    // النوافذ المنبثقة والقوائم المساعدة
    // ----------------------------------------------------

    fun setShowHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showHistoryDialog = show) }
    }

    fun setShowRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun setShowResetConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showResetConfirmDialog = show) }
    }

    fun resetGame() {
        _uiState.update {
            TrixUiState(
                currentScreen = AppScreen.CONFIG,
                config = it.config
            )
        }
    }

    fun restartWithSameConfig() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME_DASHBOARD,
                rounds = emptyList(),
                undoneRounds = emptyList(),
                activeKingdomOwnerId = 0,
                showResetConfirmDialog = false
            )
        }
    }
}
