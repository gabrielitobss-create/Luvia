package com.luvia.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.luvia.ai.ui.LuviaApp
import com.luvia.ai.ui.theme.LuviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LuviaTheme { LuviaApp() } }
    }
}
