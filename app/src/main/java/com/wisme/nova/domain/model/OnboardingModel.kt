//File info: Data of the onboarding pages

package com.wisme.nova.domain.model

import androidx.annotation.DrawableRes
import com.wisme.nova.R

sealed class OnboardingModel(
    @DrawableRes val image: Int,
    val title: String,
    val description: String,
) {

    data object FirstPage : OnboardingModel(
        image = R.drawable.img_intro_1,
        title = "Welcome to\n" +
                "WISME",
        description = "Choose from bite‑size episodes, series and playlists designed by educators"
    )

    data object SecondPage : OnboardingModel(
        image = R.drawable.img_intro_2,
        title = "WISME helps you",
        description = "Take interactive notes, key takeaways, and quick quizzes reinforce every episode."
    )

    data object ThirdPage : OnboardingModel(
        image = R.drawable.img_intro_3,
        title = "WISME let’s you",
        description = "Learn and understand topics explained like an engaging podcast"
    )

    data object FourthPage : OnboardingModel(
        image = R.drawable.img_intro_4,
        title = "On WISME, you can",
        description = "Learn anywhere, etc etc"
    )


}