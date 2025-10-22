# SimpleFabricScoreboard
A lightweight Kotlin module for Minecraft Fabric providing a simple and flexible API for managing scoreboard sideboards.

## Features
- Easy creation and management of scoreboard sideboards.
- Support for dynamic content updates.
- Packet based.
- Integration with Minecraft's native scoreboard system.
- Lightweight and efficient.
- Designed for Fabric modding environment.
- Kotlin-based for modern development practices.
- Good documentation and examples.
- Active maintenance and updates.
- Compatible with the latest Minecraft versions.
- Modular design for easy integration into existing projects.
- Support for multiple sideboards simultaneously.
- User-friendly API for developers of all skill levels.

## Installation
To use SimpleFabricScoreboard in your Fabric mod, add the following dependency to your `build.gradle.kts` file:
```kotlin
//build.gradle.kts
dependencies {
    modImplementation("de.hotkeyyy:simplefabricscoreboard:1.0.3")
}
```

## Usage (Kotlin Example)

register a sideboard and set its content:
```kotlin
val board = ScoreboardManager.createScoreboard(
                "Test Board", 
                server,
                Text.of("Line 1"),
                Text.of("Line 2"),
                Text.of("Line 3"),
                Text.of("Line 4"),
                )
            
ScoreboardManager.setPlayerScoreboard(player, board)
```

update content dynamically:
```kotlin
board.updateLine(3, Text.of("Updated Line 3"))
```
remove the sideboard from a player:
```kotlin
ScoreboardManager.removePlayerScoreboard(player)
```
remove all sideboards:
```kotlin
ScoreboardManager.clearAllBoards()
```

## Contributing
Issues and pull requests are welcome! Feel free to contribute to the project by submitting bug reports, feature requests, or code contributions.



