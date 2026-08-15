package com.ehan.chesstimer.data

data class TimeControl(
    val name: String,
    val initialTimeMillis: Long,
    val incrementMillis: Long = 0L,
    val category: TimeCategory = TimeCategory.BLITZ
) {
    val initialMinutes: Int get() = (initialTimeMillis / 60_000L).toInt()
    val initialSeconds: Int get() = ((initialTimeMillis % 60_000L) / 1000L).toInt()
    val incrementSeconds: Int get() = (incrementMillis / 1000L).toInt()

    val formattedDescription: String
        get() = if (incrementMillis > 0) {
            "${initialMinutes}m + ${incrementSeconds}s"
        } else {
            "${initialMinutes}m"
        }

    companion object {
        val PRESETS = listOf(
            // Bullet
            TimeControl("Bullet 1+0", 1 * 60_000L, 0L, TimeCategory.BULLET),
            TimeControl("Bullet 1+1", 1 * 60_000L, 1_000L, TimeCategory.BULLET),
            TimeControl("Bullet 2+1", 2 * 60_000L, 1_000L, TimeCategory.BULLET),
            
            // Blitz
            TimeControl("Blitz 3+0", 3 * 60_000L, 0L, TimeCategory.BLITZ),
            TimeControl("Blitz 3+2", 3 * 60_000L, 2_000L, TimeCategory.BLITZ),
            TimeControl("Blitz 5+0", 5 * 60_000L, 0L, TimeCategory.BLITZ),
            TimeControl("Blitz 5+3", 5 * 60_000L, 3_000L, TimeCategory.BLITZ),
            TimeControl("Blitz 5+5", 5 * 60_000L, 5_000L, TimeCategory.BLITZ),
            
            // Rapid
            TimeControl("Rapid 10+0", 10 * 60_000L, 0L, TimeCategory.RAPID),
            TimeControl("Rapid 10+5", 10 * 60_000L, 5_000L, TimeCategory.RAPID),
            TimeControl("Rapid 15+10", 15 * 60_000L, 10_000L, TimeCategory.RAPID),
            
            // Classical
            TimeControl("Classical 30+0", 30 * 60_000L, 0L, TimeCategory.CLASSICAL),
            TimeControl("Classical 60+0", 60 * 60_000L, 0L, TimeCategory.CLASSICAL)
        )

        val DEFAULT = PRESETS.first { it.name == "Blitz 5+0" }
    }
}

enum class TimeCategory(val label: String) {
    BULLET("Bullet"),
    BLITZ("Blitz"),
    RAPID("Rapid"),
    CLASSICAL("Classical"),
    CUSTOM("Custom")
}

enum class ChessColor(val label: String, val enLabel: String, val symbol: String) {
    WHITE("Putih", "White", "♔"),
    BLACK("Hitam", "Black", "♚");

    val opponent: ChessColor
        get() = if (this == WHITE) BLACK else WHITE
}

enum class PlayerSide {
    PLAYER_1, // Bottom / Slot 1
    PLAYER_2  // Top / Slot 2
}

enum class GameStatus {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    GAME_OVER
}

data class PlayerState(
    val side: PlayerSide,
    val name: String,
    val color: ChessColor = if (side == PlayerSide.PLAYER_1) ChessColor.WHITE else ChessColor.BLACK,
    val timeRemainingMillis: Long,
    val moveCount: Int = 0,
    val isFlagged: Boolean = false
)
