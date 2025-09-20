//Figma Link: https://www.figma.com/design/I4rKnkjrCvQyJ8kWbZvouz/Research-App?node-id=282-417&t=ZBdVxSS39cbi6UzE-0
//File info: Assembles all the onboarding components together
package com.wisme.nova.ui.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.domain.model.OnboardingModel
import com.wisme.nova.ui.common.IndicatorUI
import kotlinx.coroutines.launch
import com.wisme.nova.R
import com.wisme.nova.ui.common.GradientButtonUi
import kotlin.collections.lastIndex

val Gradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFE4FFC2), Color(0xFFC1FF72))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingModel.FirstPage,
        OnboardingModel.SecondPage,
        OnboardingModel.ThirdPage,
        OnboardingModel.FourthPage
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }

    val buttonState = remember {
        derivedStateOf {
            when (pagerState.currentPage) {
                pages.lastIndex -> "Get Started"
                0 -> "Continue"
                else -> "Next"
            }
        }
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            if(pagerState.currentPage!=0){
            Text(
                text="Skip >",
                modifier = Modifier.fillMaxWidth().padding(top=65.dp,end=20.dp),
                textAlign = TextAlign.End,
                color=MaterialTheme.colorScheme.primary,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.inter_18pt_medium, FontWeight.Medium)),
                    fontSize = 16.sp
                )
            )}
            else{
                Spacer(modifier = Modifier.height(85.dp))
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical =40.dp,horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IndicatorUI(
                    pageSize = pages.size,
                    currentPage = pagerState.currentPage
                )
                GradientButtonUi(
                    text = buttonState.value,
                    gradient = Gradient,
                    textColor = Color.Black,
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.inter_18pt_semibold, FontWeight.Medium)),
                        fontSize = 16.sp
                    )

                ) {
                    scope.launch {
                        if (pagerState.currentPage < pages.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onFinished()
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(state = pagerState) { index ->
                OnboardingGraphUI(onboardingModel = pages[index])
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen { }
}
