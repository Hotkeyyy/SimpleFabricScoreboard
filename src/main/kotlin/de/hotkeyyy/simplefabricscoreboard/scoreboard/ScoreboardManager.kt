package de.hotkeyyy.simplefabricscoreboard.scoreboard

import de.hotkeyyy.simplefabricscoreboard.Simplefabricscoreboard
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.ConcurrentHashMap

object ScoreboardManager {
    internal val playerBoard = ConcurrentHashMap<String, Simplescoreboard>()


    fun getPlayerScoreboard(player: ServerPlayer): Simplescoreboard? {
        return playerBoard[player.stringUUID]
    }

    fun removePlayerScoreboard(player: ServerPlayer) {
        if (!playerBoard.containsKey(player.stringUUID)) {
            Simplefabricscoreboard.logger.warn("Tried to remove a scoreboard from a player that doesn't have one!")
            return
        }
        playerBoard[player.stringUUID]?.removePlayer(player)
        playerBoard.remove(player.stringUUID)
    }

    fun setPlayerScoreboard(player: ServerPlayer, board: Simplescoreboard) {
        if (playerBoard.contains(player.stringUUID)) {
            Simplefabricscoreboard.logger.warn("Tried to add a scoreboard to a player that already has one!")
            return
        }
        board.addPlayer(player)
        playerBoard[player.stringUUID] = board
    }

    fun createScoreboard(
        name: String,
        displayName: Component,
        server: net.minecraft.server.MinecraftServer,
        vararg lines: Component
    ): Simplescoreboard {
        val board = Simplescoreboard(name, displayName, server)
        board.setLines(*lines)
        return board

    }

    fun clearAllBoards() {
        playerBoard.forEach { (_, board) ->
            board.removeAllPlayers()
        }
        playerBoard.clear()
    }

}