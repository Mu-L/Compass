/*
 * This file is part of Compass.
 * Copyright (C) 2026 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bobek.compass

import android.Manifest
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.ui.TestConstants
import org.junit.Rule
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule as createAndroidComposeTestRule

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
abstract class AbstractAndroidTest {

    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        createAndroidComposeTestRule(
            activityRule = ActivityScenarioRule<MainActivity>(
                Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                    .putExtra(OPTION_INSTRUMENTED_TEST, true)
            ),
            activityProvider = { rule ->
                var activity: MainActivity? = null
                rule.scenario.onActivity { activity = it }
                activity ?: throw IllegalStateException("Activity not found")
            }
        )

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    protected fun waitUntilCompassIsDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 15_000L) { onCompassRose().isDisplayed() }
        composeTestRule.waitForIdle()
    }

    protected fun openSettings() {
        composeTestRule.waitForIdle()
        onSettingsButton().performClick()
        composeTestRule.waitForIdle()
    }

    protected fun selectNightMode(@StringRes labelResId: Int) {
        composeTestRule.waitForIdle()
        onNightModeListItem().performClick()
        composeTestRule.waitForIdle()
        onNightModeOption(labelResId).performClick()
        composeTestRule.waitForIdle()
    }

    protected fun onTopBarTitle(@StringRes titleResId: Int = R.string.compass): SemanticsNodeInteraction =
        onTopBarTitle(getString(titleResId))

    protected fun onTopBarTitle(title: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(title)

    protected fun onSettingsButton(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithContentDescription(getString(R.string.settings))

    protected fun setAzimuth(degrees: Float) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setAzimuth(Azimuth(degrees))
        }
        composeTestRule.waitForIdle()
    }

    protected fun setAccuracy(accuracy: SensorAccuracy) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setSensorAccuracy(accuracy)
        }
        composeTestRule.waitForIdle()
    }

    protected fun setTrueNorth(trueNorth: Boolean) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setTrueNorth(trueNorth)
        }
        composeTestRule.waitForIdle()
    }

    protected fun setScreenOrientationLocked(screenOrientationLocked: Boolean) {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.compassViewModel.setScreenOrientationLocked(screenOrientationLocked)
        }
        composeTestRule.waitForIdle()
    }

    protected fun SemanticsNodeInteraction.assertStateDescription(value: String) {
        assert(hasStateDescription(value))
    }

    protected fun onCompassRose(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.COMPASS_ROSE)

    protected fun onSensorStatusButton(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.SENSOR_STATUS_BUTTON)

    protected fun onSensorAccuracyText(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(TestConstants.SENSOR_ACCURACY_TEXT)

    protected fun onLicenseListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.license))

    protected fun onThirdPartyLicensesListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.third_party_licenses))

    protected fun onNightModeListItem(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(R.string.night_mode))

    protected fun onNightModeOption(@StringRes labelResId: Int): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(getString(labelResId))

    protected fun onListItem(text: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(text)

    protected fun scrollToListItem(text: String) {
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }

    protected fun waitUntilTextExists(text: String, timeoutMillis: Long = 5_000) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(text, substring = true), timeoutMillis = timeoutMillis)
    }

    protected fun getString(@StringRes resId: Int): String = composeTestRule.activity.getString(resId)
}
