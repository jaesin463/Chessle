package com.xinxe.chessle.domain.usecase

import com.xinxe.chessle.domain.model.FeedbackType
import com.xinxe.chessle.domain.model.MoveAttempt

/**
 * 사용자가 제출한 5쌍의 수(MoveAttempt)를 정답과 비교하여
 * 각 수에 대한 피드백(CORRECT, PRESENT, ABSENT)을 판정합니다.
 */
object ProcessFeedbackUseCase {

    fun execute(
        currentInputMoves: List<MoveAttempt>,
        fullSolution: List<String>
    ): Pair<List<MoveAttempt>, Boolean> {
        // 1. 비교 대상 정답지 추출 (일반적으로 5쌍 = 10수)
        val targetSize = currentInputMoves.size * 2
        val effectiveSolution = fullSolution.take(targetSize)

        // 2. 색상별 정답 집합 (PRESENT 판정용: 해당 순서엔 없지만 내 색상의 다른 순서엔 있는 수)
        val whiteSolutions = effectiveSolution.filterIndexed { i, _ -> i % 2 == 0 }.toSet()
        val blackSolutions = effectiveSolution.filterIndexed { i, _ -> i % 2 != 0 }.toSet()

        // 3. 각 이동에 대한 피드백 계산
        val submittedResult = currentInputMoves.mapIndexed { attemptIdx, attempt ->
            val whiteIdx = attemptIdx * 2
            val blackIdx = whiteIdx + 1

            attempt.copy(
                whiteFeedback = getFeedback(
                    moveNotation = attempt.whiteMove?.notation ?: "",
                    correctNotation = effectiveSolution.getOrNull(whiteIdx),
                    colorSolutions = whiteSolutions
                ),
                blackFeedback = getFeedback(
                    moveNotation = attempt.blackMove?.notation ?: "",
                    correctNotation = effectiveSolution.getOrNull(blackIdx),
                    colorSolutions = blackSolutions
                )
            )
        }

        // 4. 승리 여부 확인 (모든 피드백이 CORRECT인지 확인)
        val allCorrect = submittedResult.all {
            it.whiteFeedback == FeedbackType.CORRECT && it.blackFeedback == FeedbackType.CORRECT
        }

        return Pair(submittedResult, allCorrect)
    }

    /**
     * 개별 기보(Notation)에 대한 피드백 유형을 결정합니다.
     */
    private fun getFeedback(
        moveNotation: String,
        correctNotation: String?,
        colorSolutions: Set<String>
    ): FeedbackType {
        return when {
            moveNotation.isEmpty() -> FeedbackType.ABSENT
            moveNotation == correctNotation -> FeedbackType.CORRECT
            colorSolutions.contains(moveNotation) -> FeedbackType.PRESENT
            else -> FeedbackType.ABSENT
        }
    }
}