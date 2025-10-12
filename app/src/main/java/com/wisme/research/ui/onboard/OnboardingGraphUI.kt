//File info: Image, title and description for each onboarding page

package com.wisme.research.ui.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.research.R
import com.wisme.research.domain.model.OnboardingModel
import com.wisme.research.ui.utils.*

val titleFont = FontFamily(
    Font(R.font.inter_18pt_bold,FontWeight.Bold)
)
val titleMediumFont = FontFamily(
    Font(R.font.inter_18pt_medium, FontWeight.Medium)
)

val descriptionFont = FontFamily(
    Font(R.font.inter_18pt_medium, FontWeight.Medium)
)


@Composable
fun OnboardingGraphUI(onboardingModel: OnboardingModel) {
    val titleText = onboardingModel.title
    val wismeIndex = titleText.indexOf("WISME")

    val annotatedTitleString = buildAnnotatedString {
        //Text before WISME
        if (wismeIndex > 0) {
            withStyle(
                style = SpanStyle(
                    color = Color.White,
                    fontFamily = titleMediumFont,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(titleText.substring(0, wismeIndex))
            }
        }
        //WISME
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontFamily = titleFont,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("WISME")
        }
        //Text after WISME
        if (wismeIndex+5<titleText.length) {
            withStyle(
                style = SpanStyle(
                    color = Color.White,
                    fontFamily = titleMediumFont,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(titleText.substring(wismeIndex+5))
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(
                horizontal = ResponsiveSpacing.horizontal(), 
                vertical = ResponsiveSpacing.vertical()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if(onboardingModel.image==R.drawable.img_intro_1){
            Text(
                text = annotatedTitleString,
                modifier = Modifier.fillMaxWidth(),
                style = ResponsiveTextStyles.headlineLarge().copy(
                    fontFamily = titleFont,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))

        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if(onboardingModel.image!=R.drawable.img_intro_1) 
                        ResponsiveSpacing.medium() 
                    else 
                        0.dp
                )
                .weight(1f),
            alignment = Alignment.Center
        )

        if(onboardingModel.image!=R.drawable.img_intro_1){
            Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
            Text(
                text = annotatedTitleString,
                modifier = Modifier.fillMaxWidth(),
                style = ResponsiveTextStyles.headlineMedium().copy(
                    fontFamily = titleFont,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
        Text(
            text = onboardingModel.description,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.White,
            style = ResponsiveTextStyles.bodyLarge().copy(
                fontFamily = descriptionFont
            )
        )
        Spacer(modifier = Modifier.height(ResponsiveSpacing.extraLarge()))
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview1() {
    OnboardingGraphUI(OnboardingModel.FirstPage)
}

@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview2() {
    OnboardingGraphUI(OnboardingModel.SecondPage)
}

@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview3() {
    OnboardingGraphUI(OnboardingModel.ThirdPage)
}

@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview4() {
    OnboardingGraphUI(OnboardingModel.FourthPage)
}
