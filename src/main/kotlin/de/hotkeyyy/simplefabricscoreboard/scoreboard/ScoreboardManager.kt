package de.hotkeyyy.simplefabricscoreboard.scoreboard

import de.hotkeyyy.simplefabricscoreboard.Simplefabricscoreboard
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ScoreboardManager {
    internal val playerBoards = ConcurrentHashMap<UUID, Simplescoreboard>()


    fun getPlayerScoreboard(player: ServerPlayer): Simplescoreboard? {
        return playerBoards[player.uuid]
    }

    fun removePlayerScoreboard(player: ServerPlayer) {
        val board = playerBoards[player.uuid]
        if (board == null) {
            Simplefabricscoreboard.logger.warn("Tried to remove a scoreboard from a player that doesn't have one!")
            return
        }
        board.removePlayer(player)
    }

    fun setPlayerScoreboard(player: ServerPlayer, board: Simplescoreboard) {
        val currentBoard = playerBoards[player.uuid]
        if (currentBoard === board) {
            board.refreshPlayer(player)
            return
        }

        currentBoard?.removePlayer(player)
        board.addPlayer(player)
    }

    internal fun registerBoard(player: ServerPlayer, board: Simplescoreboard) {
        playerBoards[player.uuid] = board
    }

    internal fun unregisterBoard(player: ServerPlayer, board: Simplescoreboard) {
        playerBoards.remove(player.uuid, board)
    }

    fun clearAllBoards() {
        playerBoards.values.toSet().forEach { board ->
            board.removeAllPlayers()
        }
        playerBoards.clear()
    }

}
