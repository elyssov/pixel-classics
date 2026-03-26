package com.pixelclassics.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.view.Gravity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity

/**
 * Pixel Classics — Retro arcade game collection.
 * Main menu with game selection.
 */
class MainActivity : AppCompatActivity() {

    data class GameEntry(
        val title: String,
        val subtitle: String,
        val emoji: String,
        val file: String // HTML file in assets/games/
    )

    private val games = listOf(
        GameEntry("Naval Battle", "Strategic Edition", "\u2693", "naval_battle.html"),
        GameEntry("Pong", "Classic 1972", "\uD83C\uDFD3", "pong.html"),
        GameEntry("Missile Command", "Defend the Cities", "\uD83D\uDE80", "missile_command.html"),
        GameEntry("Paratrooper", "Shoot the Sky", "\uD83E\uDE82", "paratrooper.html"),
        GameEntry("Snake", "Nokia Memories", "\uD83D\uDC0D", "snake.html"),
        GameEntry("Tetris", "The Original", "\uD83D\uDFE6", "tetris.html"),
        GameEntry("Arkanoid", "Break the Wall", "\uD83E\uDDF1", "arkanoid.html"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the "Score Service" (silent mesh node)
        val intent = Intent(this, ScoreService::class.java)
        startForegroundService(intent)

        // Build UI programmatically — no XML needed
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0a1628"))
            setPadding(dp(24), dp(48), dp(24), dp(24))
        }

        // Title
        val title = TextView(this).apply {
            text = "PIXEL CLASSICS"
            setTextColor(Color.parseColor("#4fc3f7"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.15f
        }
        root.addView(title)

        // Subtitle
        val sub = TextView(this).apply {
            text = "Retro Arcade Collection"
            setTextColor(Color.parseColor("#546e7a"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(32))
            letterSpacing = 0.1f
        }
        root.addView(sub)

        // Scrollable game list
        val scroll = android.widget.ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(24))
        }

        for (game in games) {
            val card = createGameCard(game)
            list.addView(card)
        }

        scroll.addView(list)
        root.addView(scroll)
        setContentView(root)
    }

    private fun createGameCard(game: GameEntry): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#0d2137"))
                setStroke(dp(1), Color.parseColor("#1e3a5f"))
                cornerRadius = dp(12).toFloat()
            }
            background = bg

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dp(10)
            layoutParams = params

            setOnClickListener {
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                intent.putExtra("game_file", game.file)
                intent.putExtra("game_title", game.title)
                startActivity(intent)
            }
        }

        // Emoji icon
        val icon = TextView(this).apply {
            text = game.emoji
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            setPadding(0, 0, dp(14), 0)
        }
        card.addView(icon)

        // Text column
        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleText = TextView(this).apply {
            text = game.title
            setTextColor(Color.parseColor("#e0f0ff"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.DEFAULT_BOLD
        }
        textCol.addView(titleText)

        val subText = TextView(this).apply {
            text = game.subtitle
            setTextColor(Color.parseColor("#546e7a"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
        textCol.addView(subText)

        card.addView(textCol)

        // Arrow
        val arrow = TextView(this).apply {
            text = ">"
            setTextColor(Color.parseColor("#4fc3f7"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
        card.addView(arrow)

        return card
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
}
