package com.rafael.inclusimap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val crashStacktrace = intent.getStringExtra("crashStack") ?: "Unknown stacktrace"
        val crashMessage = intent.getStringExtra("crashMessage") ?: "Unknown message"

        setContent {
            var showCrashDetails by remember { mutableStateOf(false) }
            val context = LocalContext.current

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.fillMaxHeight(0.025f))
                            Text(
                                text = "Oops.\nOcorreu um erro inesperado",
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                            )
                            Icon(
                                imageVector = Icons.Default.SentimentVeryDissatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(65.dp),
                                tint = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                            )
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (showCrashDetails) {
                                            Modifier.fillMaxHeight(0.9f)
                                        } else {
                                            Modifier.clickable { showCrashDetails = true }
                                        },
                                    )
                                    .animateContentSize()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(4.dp),
                                elevation = CardDefaults.elevatedCardElevation(
                                    defaultElevation = 4.dp,
                                ),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                ),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                if (showCrashDetails) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxHeight(0.9f)
                                            .fillMaxWidth()
                                            .animateContentSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        item {
                                            Text(
                                                text = crashMessage,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                        item {
                                            Text(
                                                text = crashStacktrace,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showCrashDetails = false }
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Absolute.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Ocultar detalhes do erro",
                                            fontSize = 18.sp,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { showCrashDetails = false },
                                            modifier = Modifier.size(35.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ExpandLess,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.1f)
                                            .clickable { showCrashDetails = true }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Ver detalhes do erro",
                                            fontSize = 18.sp,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { showCrashDetails = true },
                                            modifier = Modifier.size(35.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth()
                                    .align(Alignment.End)
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    12.dp,
                                    Alignment.End,
                                ),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Button(
                                    onClick = { finishAffinity() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                ) { Text("Fechar") }
                                Button(
                                    onClick = {
                                        val intent = Intent(context, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finishAffinity()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                ) { Text("Reiniciar app") }
                            }
                        }
                    }
                }
            }
        }
    }
}
