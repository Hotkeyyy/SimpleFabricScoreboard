package de.hotkeyyy.simplefabricscoreboard.scoreboard

import de.hotkeyyy.simplefabricscoreboard.Simplefabricscoreboard
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.concurrent.ConcurrentHashMap

object ScoreboardManager {
    internal val playerBoard = ConcurrentHashMap<String, Simplescoreboard>()


    fun getPlayerScoreboard(player: ServerPlayerEntity): Simplescoreboard? {
        return playerBoard[player.uuidAsString]
    }

    fun removePlayerScoreboard(player: ServerPlayerEntity) {
        if (!playerBoard.containsKey(player.uuidAsString)) {
            Simplefabricscoreboard.logger.warn("Tried to remove a scoreboard from a player that doesn't have one!")
            return
        }
        playerBoard[player.uuidAsString]?.removePlayer(player)
        playerBoard.remove(player.uuidAsString)
    }

    fun setPlayerScoreboard(player: ServerPlayerEntity, board: Simplescoreboard) {
        if (playerBoard.contains(player.uuidAsString)) {
            Simplefabricscoreboard.logger.warn("Tried to add a scoreboard to a player that already has one!")
            return
        }
        board.addPlayer(player)
        playerBoard[player.uuidAsString] = board
    }

    fun createScoreboard(
        name: String,
        displayName: Text,
        vararg lines: Text,
        server: net.minecraft.server.MinecraftServer
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