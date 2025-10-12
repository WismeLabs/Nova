//File info: Data of the onboarding pages

package com.wisme.research.domain.model

import androidx.annotation.DrawableRes
import com.wisme.research.R

sealed class OnboardingModel(
    @DrawableRes val image: Int,
    val title: String,
    val description: String,
) {

    data object FirstPage : OnboardingModel(
        image = R.drawable.img_intro_1,
        title = "Welcome to\n" +
                "WISME",
        description = "Where learning stops feeling like a study session and starts feeling like a conversation."
    )

    data object SecondPage : OnboardingModel(
        image = R.drawable.img_intro_2,
        title = "WISME helps you",
        description = "Learn any topic, anytime, anywhere with bite sized content."
    )

    data object ThirdPage : OnboardingModel(
        image = R.drawable.img_intro_3,
        title = "WISME adapts",
        description = "to your learning style and preferences"
    )

    data object FourthPage : OnboardingModel(
        image = R.drawable.img_intro_4,
        title = "Right now on WISME, you can",
        description = "Experience a few pre generated content and provide valuable feedback that helps us polish our app"
    )


}
