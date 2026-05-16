# ✦ CaroAI

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Java%20Swing-GUI-5C5CFF?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=for-the-badge)

**A desktop Gomoku (Caro) game built with Java Swing.**
Play against a heuristic AI or challenge a friend in local 2-player mode — on an infinite scrollable board with a fully custom-painted UI.

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
  - [Windows](#windows)
  - [macOS](#macos)
  - [Linux](#linux)
- [Running the Project](#running-the-project)
  - [Command Line](#command-line)
  - [IntelliJ IDEA](#intellij-idea)
  - [Eclipse](#eclipse)
  - [VS Code](#vs-code)
- [How to Play](#how-to-play)
- [Project Structure](#project-structure)
- [AI Algorithm](#ai-algorithm)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Caro is a two-player strategy game where players take turns placing **X** and **O** pieces on a grid. The first player to align **5 consecutive pieces** — horizontally, vertically, or diagonally — wins.

This implementation features:
- A fully custom-painted UI (no stock Swing look-and-feel)
- An animated star-field start menu with mode selection
- A glassmorphism sidebar with real-time turn indicator
- A cinematic win/loss overlay with particle confetti
- Infinite board panning with WASD camera controls

**Technologies:** Java · Java Swing · AWT · Greedy Heuristic AI · CardLayout · JLayeredPane

---

## Features

| Feature | Description |
|---|---|
| **Animated Start Menu** | Star-field background, mode selection cards, X/O symbol toggle |
| **vs AI** | Play against a heuristic AI; choose to be X (first) or O (second) |
| **2-Player Local** | Hotseat multiplayer on the same screen |
| **Infinite Board** | 50×50 grid, pan the camera freely with WASD |
| **Hover Preview** | Ghost piece preview before committing a move |
| **Move Highlight** | Last move highlighted with a green border |
| **Win Animation** | Winning line highlighted and struck through in gold |
| **Win/Loss Overlay** | Full-screen cinematic overlay — confetti on win, clean card on loss |
| **Undo** | Take back the last move (2 moves at once when vs AI) |
| **Restart / Main Menu** | Reset the game or return to mode selection at any time |

---

## Prerequisites

Before running the project, ensure your machine has the following installed:

| Tool | Minimum Version | Purpose |
|---|---|---|
| **JDK** (Java Development Kit) | 11 | Compile and run the application |
| **Git** | Any | Clone the repository |

> **Note:** You only need a JDK — no build tools (Maven, Gradle) or external libraries are required. This project uses only the Java standard library.

---

## Environment Setup

### Verify Existing Installation

Open a terminal and run:

```bash
java -version
javac -version
```

If both commands return version 11 or higher, skip to [Running the Project](#running-the-project).

---

### Windows

#### — Using winget (Windows 10/11)

```powershell
# Install Microsoft OpenJDK 21 (LTS)
winget install Microsoft.OpenJDK.21
```

Restart your terminal after installation.


```cmd
java -version
javac -version
```

#### Set JAVA_HOME manually (if needed)

```cmd
# Replace the path with your actual JDK location
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

---

### Linux

#### Ubuntu / Debian

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk

# Verify
java -version
javac -version
```

#### Set JAVA_HOME on Linux (if needed)

```bash
# Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH

# Reload shell
source ~/.bashrc
```

---

## Running the Project

### Clone the Repository

```bash
git clone https://github.com/ToanNB1204/CaroAI.git
cd caro-game
```

---

### Command Line

This is the simplest method and works on all operating systems.

```bash
# Step 1 — Navigate to the source root
cd src

# Step 2 — Compile all Java files
javac scr/*.java

# Step 3 — Run the game
java scr.Main
```

The game window will open immediately.

> **Windows users:** Use Command Prompt or PowerShell. If using PowerShell and the wildcard `*.java` doesn't work, run:
> ```powershell
> javac (Get-ChildItem scr\*.java | % { $_.FullName })
> java scr.Main
> ```

---

## How to Play

### Start Menu

1. Select a game mode:
   - **vs AI** — play against the computer
   - **2 Players** — play against a friend on the same machine
2. If playing vs AI, choose your symbol:
   - **X** — you go first
   - **O** — AI goes first
3. Click **▶ START**

### In-Game Controls

| Action | Input |
|---|---|
| Place a piece | Left-click an empty cell |
| Pan the camera | `W` `A` `S` `D` |
| Undo last move | **Undo** button (sidebar) |
| Restart the game | **Restart** button (sidebar) |
| Quit | **Exit** button (sidebar) |

> **Tip:** Click once on the board area after the game starts to give it keyboard focus for WASD panning to work.

### Win / Loss Screen

| Action | Input |
|---|---|
| Play Again | Click **Play Again** or press `R` |
| Return to Main Menu | Click **Main Menu** or press `Esc` |

---

## Project Structure

```
caro-game/
├── src/
│   └── scr/
│       ├── Main.java               # Entry point — launches GameFrame
│       ├── GameFrame.java          # Main window — manages CardLayout and win overlay
│       ├── StartMenuPanel.java     # Animated start screen with mode/symbol selection
│       ├── MenuPanel.java          # In-game sidebar (turn badge, action buttons)
│       ├── BoardPanel.java         # Board rendering, piece drawing, input handling
│       ├── WinOverlayPanel.java    # Full-screen win/loss overlay with confetti
│       ├── GameEngine.java         # Core logic — moves, win detection, turn switching
│       ├── AI.java                 # Greedy heuristic AI engine
│       ├── Player.java             # Player model (name, symbol, isAI)
│       ├── Move.java               # Move record (point + player)
│       ├── Board.java              # (Legacy) early JButton board prototype
│       └── Menu.java               # (Legacy) early JMenuBar prototype
├── assets/                         # Images and icons (if any)
├── screenshots/                    # Screenshots for README
└── README.md
```

---

## AI Algorithm

The AI uses a **Greedy Heuristic** — no look-ahead tree. On each turn it scores every candidate cell and immediately plays the highest-scoring one.

### Candidate Selection

Only empty cells with **at least one occupied neighbour** within 1 cell are considered. This filters out isolated empty cells and keeps the search fast on the large board.

### Scoring

Each candidate is evaluated in **4 directions**: horizontal `→`, vertical `↓`, diagonal `↘`, anti-diagonal `↗`. The AI counts consecutive friendly and enemy pieces and assigns scores:

| Sequence | AI attack | Player block |
|---|---|---|
| 5 in a row (win now) | 100,000 | 90,000 |
| 4 in a row | 10,000 | 9,000 |
| 3 in a row | 1,000 | 900 |
| 2 in a row | 100 | 90 |

The AI will **always win immediately** if it can (100,000), then **always block** a player about to win (90,000), then extend its own sequences.

---

## Troubleshooting

**The game window doesn't open**
- Confirm `javac` and `java` are on your PATH: `java -version`
- Make sure you compiled from inside the `src/` directory before running `java scr.Main`

**`error: package scr does not exist`**
- You must run `java scr.Main` from inside the `src/` directory, not from the project root

**`javac: command not found`**
- You have a JRE installed but not a JDK — install a full JDK (see [Environment Setup](#environment-setup))

**Board doesn't respond to WASD**
- Click once on the board area to give it keyboard focus

**Display looks blurry on high-DPI screens (Windows)**
- Add the following JVM flag when running:
  ```bash
  java -Dsun.java2d.uiScale=1.0 scr.Main
  ```
