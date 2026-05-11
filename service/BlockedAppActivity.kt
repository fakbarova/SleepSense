package com.circadianx.sleepsense.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.circadianx.sleepsense.data.local.db.dao.AppBlockOverrideDao
import com.circadianx.sleepsense.data.local.db.entity.AppBlockOverrideEntity
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BlockedAppActivity : ComponentActivity() {
    @Inject lateinit var overrideDao: AppBlockOverrideDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val blockedPackage = intent.getStringExtra(EXTRA_PACKAGE) ?: run {
            finish()
            return
        }

        setContent {
            SleepSenseTheme {
                val colors = SleepSenseTheme.colors
                var reason by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgDeep)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Wind-down time",
                        fontFamily = DmSerifDisplay,
                        fontSize = 28.sp,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "You planned to sleep soon. Want to keep this app blocked?",
                        fontFamily = DmSans,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Override reason (required)", fontFamily = DmSans) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { finish() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.bgBase)
                    ) {
                        Text("Go back", fontFamily = DmSans, color = colors.textPrimary)
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val trimmed = reason.trim()
                            if (trimmed.isEmpty()) return@Button
                            lifecycleScope.launch {
                                overrideDao.insert(
                                    AppBlockOverrideEntity(
                                        timestampMs = System.currentTimeMillis(),
                                        packageName = blockedPackage,
                                        reason = trimmed
                                    )
                                )
                            }

                            packageManager.getLaunchIntentForPackage(blockedPackage)?.let { launch ->
                                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(launch)
                            }
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                        enabled = reason.trim().isNotEmpty()
                    ) {
                        Text("Override and open app", fontFamily = DmSans)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
    }
}

