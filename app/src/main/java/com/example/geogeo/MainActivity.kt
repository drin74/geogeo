package com.example.geogeo

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide


data class LocationData(
    val country: String = "",
    val imageUrl: String = "",
    val altNames: List<String> = emptyList()
)

class MainActivity : AppCompatActivity() {

    private var currentRound = 0
    private var score = 0
    private var gameLocations = listOf<LocationData>() // Будет заполнено случайно


    private val allLocations = listOf(
        LocationData(
            "Франция",
            "https://avatars.mds.yandex.net/i?id=4333ce2c63596d7b084cb7d298f7b2d82ed72fdb-7059721-images-thumbs&n=13",
            listOf("France", "Париж", "Eiffel")
        ),
        LocationData(
            "США",
            "https://avatars.mds.yandex.net/i?id=d84309af14cc92457a876e664b3b5b466a4b9cea-16499538-images-thumbs&n=13",
            listOf("USA", "America", "New York", "Америка", "Штаты")
        ),
        LocationData(
            "Италия",
            "https://yandex-images.clstorage.net/Qpp5u0188/38e477C5ka/CvHzb8K25NfbI93VVQAktqgYGG6FWeY8B8hRAVPgQFOILE6rT5DP5hi8o5_7ZLXziu38nyjupr6vbK3ofULXVk2jtTuC8P-X2TGA2BkHLbK3UwUtkBv_OACRXQBtjzEi-kMwwGK0tptTvv7OrCr6tsrTlIkbp-Sq90fslgdu8ig2w9d_05Wf8OsDhthYFBWggXIUXYQR9lEs7QpwWLMmJvNrLvRug7uMTK_Z8IcVlo7y_ojEH7LdkNRC6osfQPAzNvPTbdinsu_aIZ3dYgs0p5dlJ12GFNhIC8QVX36dOTn8bDblQrKqrUmp5uOsA5CiyOusxwGS9pnKQN-9JV74LBLlyFz6tLDIzCa3_0E-FLeFdDhmtA7iZT3qPlV6uzwwylo59VqJkL5Fpufcvj2_qsfErOArmfOW517fnylP-Aok2dd8-rGp_vsSouBHIy2YnX4nbLQg2Hsr2ShfcLI_E-t2EdBpv4ulUqf7z5g6nbXW-5zHL73BoPNwzoU-cMABC8ffT9WBkPLPFYDYfjoQh4J8GHmFM8tgBM44ZF2FEhbCVC3UaIe6l1ya1-C9IYyt6fua2zue9p_NaMy8IWrCKArj7E_hopz88R-h9Ww4CKmWVTpaoC7mbC_qG35eoiYZ1kk8-2O3i4F5utX9gTG9ks_Zjd8du_Kcx1rdpCVJ1wsY58504r2r_fI3q_ZiDxGmiVMvXLsRxGUC3gN_XIEhKvVXDOVBpq2ke6b3_KoilaPPw6PCEJLJjvlj8IEHT9UcM9bBTeewg9vZAJncXgAuspdDDGS1Ds1GHtgodWC4BBXwcRHGf6S-g0292OeKAaGC3PqP0SOn4LDzQvqmBVfVEBXExF3WrLLrxQCU5VsFNZaHRDh7sD7KdyTFKVZgqQcE9HYYxEKLnL1ejfzIkRuzkd38o9gIltWYzE_kgj5M-i8I3t1f4bKwwMsBtPRcOCeKk1MBXIsO3FUI6wlXd6M",
            listOf("Italy", "Roma", "Рим", "Колизей")
        ),
        LocationData(
            "Япония",
            "https://avatars.mds.yandex.net/i?id=44593c9e4a15fc362cd48be78caf011964c1374e-4246833-images-thumbs&n=13",
            listOf("Japan", "Nippon", "Токио", "Фудзи")
        ),
        LocationData(
            "Великобритания",
            "https://avatars.mds.yandex.net/i?id=e937decaf6614121f83d6f943ae257a45a2defce-5480364-images-thumbs&n=13",
            listOf("UK", "England", "London", "Англия", "Лондон")
        ),
        LocationData(
            "Египет",
            "https://avatars.mds.yandex.net/i?id=173753bad98eb676fa5a6081300d13b5bc9b84e6-5281296-images-thumbs&n=13",
            listOf("Egypt", "Cairo", "Пирамиды", "Каир")
        ),
        LocationData(
            "Индия",
            "https://avatars.mds.yandex.net/i?id=f780c045f165f8a1f730219db1da8977f71ab883-3940630-images-thumbs&n=13",
            listOf("India", "Taj Mahal", "Тадж-Махал")
        ),
        LocationData(
            "Китай",
            "https://yandex-images.clstorage.net/Qpp5u0188/38e477C5ka/CvHzb8K25NfbI93VVQAktqgYGG6FWeY8B8hRClSzQFaIfkmgGpXK5B_sp8n5ZeX2j-7ymHri9ej4O6a1LB2OTU3xviOF8_uMiWTFgV0DIbS1RVt_mQPiYR2KDV5C6x5eiS8gzWqyvYpljdntgBytpdHortFkpeSFwSrPoTpU3zYO189E9JaA6e8hkPxhJBSApUIacaQF3kcM-hlUS4IyIt5fFfF9trSie7nV2o0LsrT21oXuCJ3cqN1ny7QDbvwzCMrXavaNgNnrM5vOTDslor5HIG6FAshQP_obUWWbFQvBcDPfYqm7pWOo-eucKreyycWu-A6x5r35ZNWUKGfiMT387XT-kqTP8AK53U4cM6WUZQNbpRLaQSDBKUtftiMl6no3-WegjZNsuOHQpjm7vcj6vMYhuemS-UP_ny1ozy893uFl_rSS8-8iudhXKgSRgXoFRpw9wks_yyllZag1FvxwPfBkg6y_RJ3S5r0irLzU6ZrBA6b4rclk-7sfXtsBLMT2Wsa_ve3nEZbobiYwlqNCPF-hIt1JAN4YdnazGgHsSxDHSYaFnXiZ096MEKKB-Ma_2hK-x63PeO-YKGHlGS796UDElrzJ1BGiy2IoDpyCby5duQzJYCL9PWtbvzElz1ET4H2JkYBpttjuvQaHofrbg8ojgeGP2HzJvSxi7ys_5e1j0bWuzfYqodRVKQ20iGwgaqAD1mY3-yZUebYcEddQEcN6urKeWaj6_5sjhb3Y2qjZC7DhlfZhxJQXcNESEsDPZdOihfz5E6TZfQ8mg7pGHGWfF8h2FMMoS3CVABf8Wi36X4W3uESLxP2IBLOm98GI-j-R8qzacvehPk7bMxjnzX3TrqPu_AOVxUw_OoO3fxlghDPYcSDcLXt3pDoK_Xkt61yUs79NifnSqjqgltPuk-AtgvOq9nvckAtm5D4Gw_Zm_JG-9OUBottOFDSCl24eXZYvzGYZ-xZLYYY",
            listOf("China", "Great Wall", "Великая стена")
        ),
        LocationData(
            "Бразилия",
            "https://avatars.mds.yandex.net/i?id=dd38a60082170ac20ecb468b4bdb614eb9801aee-2451806-images-thumbs&n=13",
            listOf("Brazil", "Rio", "Рио")
        ),
        LocationData(
            "Австралия",
            "https://yandex-images.clstorage.net/Qpp5u0188/38e477C5ka/CvHzb8K25NfbI93VVQAktqgYGG6FWeY8B8hRUFbiR1TbfkuhSpXL50i7qZOrZ-ShirX_nH3v8-39a6vhdxaGGRn2tyOF9_mNiGTAgV0DIbS1RVt_mQPiYR2KDV5C6x5eiS8gzWqyvYpljdntgBytpdHortFkpeSFwSrPoTpU3zYO189E9JaA6e8hkPxhJBSApUIacaQF3kcM-hlUS4IyIt5fFfF9trSie7nV2o0LsrT21oXuCJ3cqN1ny7QDbvwzCMrXavaNgNnrM5vOTDslor5HIG6FAshQP_obUWWbFQvBcDPfYqm7pWOo-eucKreyycWu-A6x5r35ZNWUKGfiMT387XT-kqTP8AK53U4cM6WUZQNbpRLaQSDBKUtftiMl6no3-WegjZNsuOHQpjm7vcj6vMYhuemS-UP_ny1ozy893uFl_rSS8-8iudhXKgSRgXoFRpw9wks_yyllZag1FvxwPfBkg6y_RJ3S5r0irLzU6ZrBA6b4rclk-7sfXtsBLMT2Wsa_ve3nEZbobiYwlqNCPF-hIt1JAN4YdnazGgHsSxDHSYaFnXiZ096MEKKB-Ma_2hK-x63PeO-YKGHlGS796UDElrzJ1BGiy2IoDpyCby5duQzJYCL9PWtbvzElz1ET4H2JkYBpttjuvQaHofrbg8ojgeGP2HzJvSxi7ys_5e1j0bWuzfYqodRVKQ20iGwgaqAD1mY3-yZUebYcEddQEcN6urKeWaj6_5sjhb3Y2qjZC7DhlfZhxJQXcNESEsDPZdOihfz5E6TZfQ8mg7pGHGWfF8h2FMMoS3CVABf8Wi36X4W3uESLxP2IBLOm98GI-j-R8qzacvehPk7bMxjnzX3TrqPu_AOVxUw_OoO3fxlghDPYcSDcLXt3pDoK_Xkt61yUs79NifnSqjqgltPuk-AtgvOq9nvckAtm5D4Gw_Zm_JG-9OUBottOFDSCl24eXZYvzGYZ-xZLYYY",
            listOf("Australia", "Sydney", "Сидней")
        ),
        LocationData(
            "Россия",
            "https://avatars.mds.yandex.net/i?id=3c9b8e7a77f3114490c3e1714f299d54bddbc790-5337839-images-thumbs&n=13",
            listOf("Russia", "Moscow", "Москва", "Кремль", "РФ")
        ),
        LocationData(
            "Испания",
            "https://yandex-images.clstorage.net/Qpp5u0188/38e477C5ka/CvHzb8K25NfbI93VVQAktqgYGG6FWeY8B8hRAwC0SAaAJUn3HsGeth7qopv_NOby3L_zminuoO2mbK-ydhyPGE-hsiOE9fmOimfGgV0DIbS1RVt_mQPiYR2KDV5C6x5eiS8gzWqyvYpljdntgBytpdHortFkpeSFwSrPoTpU3zYO189E9JaA6e8hkPxhJBSApUIacaQF3kcM-hlUS4IyIt5fFfF9trSie7nV2o0LsrT21oXuCJ3cqN1ny7QDbvwzCMrXavaNgNnrM5vOTDslor5HIG6FAshQP_obUWWbFQvBcDPfYqm7pWOo-eucKreyycWu-A6x5r35ZNWUKGfiMT387XT-kqTP8AK53U4cM6WUZQNbpRLaQSDBKUtftiMl6no3-WegjZNsuOHQpjm7vcj6vMYhuemS-UP_ny1ozy893uFl_rSS8-8iudhXKgSRgXoFRpw9wks_yyllZag1FvxwPfBkg6y_RJ3S5r0irLzU6ZrBA6b4rclk-7sfXtsBLMT2Wsa_ve3nEZbobiYwlqNCPF-hIt1JAN4YdnazGgHsSxDHSYaFnXiZ096MEKKB-Ma_2hK-x63PeO-YKGHlGS796UDElrzJ1BGiy2IoDpyCby5duQzJYCL9PWtbvzElz1ET4H2JkYBpttjuvQaHofrbg8ojgeGP2HzJvSxi7ys_5e1j0bWuzfYqodRVKQ20iGwgaqAD1mY3-yZUebYcEddQEcN6urKeWaj6_5sjhb3Y2qjZC7DhlfZhxJQXcNESEsDPZdOihfz5E6TZfQ8mg7pGHGWfF8h2FMMoS3CVABf8Wi36X4W3uESLxP2IBLOm98GI-j-R8qzacvehPk7bMxjnzX3TrqPu_AOVxUw_OoO3fxlghDPYcSDcLXt3pDoK_Xkt61yUs79NifnSqjqgltPuk-AtgvOq9nvckAtm5D4Gw_Zm_JG-9OUBottOFDSCl24eXZYvzGYZ-xZLYYY",
            listOf("Spain", "Barcelona", "Барселона")
        ),
        LocationData(
            "Турция",
            "https://yandex-images.clstorage.net/Qpp5u0188/38e477C5ka/CvHzb8K25NfbI93VVQAktqgYGG6FWeY8B8hRVlfkQ1LcLU2gE8vHt0m6oZn4M7fwiO79mHvl9bmtb6fkfhKETBii4yOF9_6CiGbDgV0DIbS1RVt_mQPiYR2KDV5C6x5eiS8gzWqyvYpljdntgBytpdHortFkpeSFwSrPoTpU3zYO189E9JaA6e8hkPxhJBSApUIacaQF3kcM-hlUS4IyIt5fFfF9trSie7nV2o0LsrT21oXuCJ3cqN1ny7QDbvwzCMrXavaNgNnrM5vOTDslor5HIG6FAshQP_obUWWbFQvBcDPfYqm7pWOo-eucKreyycWu-A6x5r35ZNWUKGfiMT387XT-kqTP8AK53U4cM6WUZQNbpRLaQSDBKUtftiMl6no3-WegjZNsuOHQpjm7vcj6vMYhuemS-UP_ny1ozy893uFl_rSS8-8iudhXKgSRgXoFRpw9wks_yyllZag1FvxwPfBkg6y_RJ3S5r0irLzU6ZrBA6b4rclk-7sfXtsBLMT2Wsa_ve3nEZbobiYwlqNCPF-hIt1JAN4YdnazGgHsSxDHSYaFnXiZ096MEKKB-Ma_2hK-x63PeO-YKGHlGS796UDElrzJ1BGiy2IoDpyCby5duQzJYCL9PWtbvzElz1ET4H2JkYBpttjuvQaHofrbg8ojgeGP2HzJvSxi7ys_5e1j0bWuzfYqodRVKQ20iGwgaqAD1mY3-yZUebYcEddQEcN6urKeWaj6_5sjhb3Y2qjZC7DhlfZhxJQXcNESEsDPZdOihfz5E6TZfQ8mg7pGHGWfF8h2FMMoS3CVABf8Wi36X4W3uESLxP2IBLOm98GI-j-R8qzacvehPk7bMxjnzX3TrqPu_AOVxUw_OoO3fxlghDPYcSDcLXt3pDoK_Xkt61yUs79NifnSqjqgltPuk-AtgvOq9nvckAtm5D4Gw_Zm_JG-9OUBottOFDSCl24eXZYvzGYZ-xZLYYY",
            listOf("Turkey", "Istanbul", "Стамбул")
        ),
        LocationData(
            "Греция",
            "https://avatars.mds.yandex.net/i?id=110498c324f01fea271c7171f12969f2cedc9c22-3924502-images-thumbs&n=13",
            listOf("Greece", "Athens", "Афины")
        ),
        LocationData(
            "Мексика",
            "https://avatars.mds.yandex.net/i?id=e1f42b620331954d0fc17893aed8e46678e03e60-4893408-images-thumbs&n=13",
            listOf("Mexico", "Мехико")
        ),
        LocationData(
            "Германия",
            "https://avatars.mds.yandex.net/i?id=3be8b0736ccba34688b7f216abfde4061981e0ae-12420972-images-thumbs&n=13",
            listOf("Germany", "Berlin", "Берлин")
        ),
        LocationData(
            "Аргентина",
            "https://avatars.mds.yandex.net/i?id=58af387546551ad44c023ec8c8acf6882aae1b2b-5277757-images-thumbs&n=13",
            listOf("Argentina", "Buenos Aires", "Буэнос-Айрес")
        ),
        LocationData(
            "ЮАР",
            "https://avatars.mds.yandex.net/i?id=c0347f15cfec70795b5c3c125530ecadd46eb9bf-5288655-images-thumbs&n=13",
            listOf("South Africa", "Cape Town", "Кейптаун", "Столовая гора")
        ),
        LocationData(
            "Таиланд",
            "https://avatars.mds.yandex.net/i?id=a65f180197236315d85f62b4b50fe466981f548e-10992423-images-thumbs&n=13",
            listOf("Thailand", "Bangkok", "Бангкок")
        ),
        LocationData(
            "Канада",
            "https://avatars.mds.yandex.net/i?id=d9c836d516f81d0bf481d92f012eb4a1387c2b85-5258573-images-thumbs&n=13",
            listOf("Canada", "Toronto", "Торонто")
        ),
        LocationData(
            "Перу",
            "https://avatars.mds.yandex.net/i?id=3453cfec2016002ca9a1c27052562d6020c3c089-5257451-images-thumbs&n=13",
            listOf("Peru", "Machu Picchu", "Мачу-Пикчу")
        ),
        LocationData(
            "Марокко",
            "https://avatars.mds.yandex.net/i?id=85f92d555709e1778ff2d4a56ad41a8921a68d11-4239917-images-thumbs&n=13",
            listOf("Morocco", "Marrakech", "Марракеш")
        ),
        LocationData(
            "Вьетнам",
            "https://avatars.mds.yandex.net/i?id=753c2bba975f9a0c2447e7c130f843306f11f5ae-5221753-images-thumbs&n=13",
            listOf("Vietnam", "Ha Long", "Халонг")
        ),
        LocationData(
            "Южная Корея",
            "https://avatars.mds.yandex.net/i?id=818be6a8e29adc0caffb8c174561f58c16c3d7e3-4774717-images-thumbs&n=13",
            listOf("South Korea", "Seoul", "Сеул", "Корея")
        ),
        LocationData(
            "Норвегия",
            "https://avatars.mds.yandex.net/i?id=acfe6fe0a5d587a55d1c54f76cc2971f7c6a4a80-8699590-images-thumbs&n=13",
            listOf("Norway", "Oslo", "Осло", "Фьорды")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGame()
    }

    private fun initGame() {
        val ivLocation: ImageView = findViewById(R.id.ivLocation)
        val etCountry: EditText = findViewById(R.id.etCountry)
        val btnGuess: Button = findViewById(R.id.btnGuess)
        val tvResult: TextView = findViewById(R.id.tvResult)
        val tvRoundScore: TextView = findViewById(R.id.tvRoundScore)
        val tvTotalScore: TextView = findViewById(R.id.tvTotalScore)
        val btnNext: Button = findViewById(R.id.btnNext)
        val tvRound: TextView = findViewById(R.id.tvRound)
        val cardResult: CardView = findViewById(R.id.cardResult)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        startNewGame()


        loadRound(ivLocation)

        btnGuess.setOnClickListener {
            val userCountry = etCountry.text.toString().trim()

            if (userCountry.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_country, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val target = gameLocations[currentRound]
            val isCorrect = checkCountry(userCountry, target)

            val roundScore = if (isCorrect) 1000 else 0
            score += roundScore

            if (isCorrect) {
                tvResult.text = getString(R.string.correct_answer, target.country)
                tvResult.setTextColor(getColor(android.R.color.holo_green_dark))
            } else {
                tvResult.text = getString(R.string.wrong_answer, target.country)
                tvResult.setTextColor(getColor(android.R.color.holo_red_dark))
            }

            tvRoundScore.text = getString(R.string.round_score_format, roundScore)
            cardResult.visibility = View.VISIBLE
            tvTotalScore.text = getString(R.string.score_format, score)

            hideKeyboard()
            btnGuess.isEnabled = false
            btnNext.visibility = View.VISIBLE

            if (currentRound >= gameLocations.size - 1) {
                btnNext.text = getString(R.string.btn_restart)
                showGameOver(tvResult, tvRoundScore)
            } else {
                btnNext.text = getString(R.string.btn_next)
            }
        }

        btnNext.setOnClickListener {
            if (currentRound >= gameLocations.size - 1) {
                startNewGame()
                score = 0
                btnNext.text = getString(R.string.btn_next)
            } else {
                currentRound++
            }

            etCountry.text.clear()
            cardResult.visibility = View.GONE
            btnGuess.isEnabled = true
            btnNext.visibility = View.GONE
            tvTotalScore.text = getString(R.string.score_format, score)
            updateRoundDisplay(tvRound)

            loadRound(ivLocation)
        }
    }


    private fun startNewGame() {
        currentRound = 0

        gameLocations = allLocations.shuffled().take(5)
        Toast.makeText(this, "Новая игра! Угадывайте страны 🌍", Toast.LENGTH_SHORT).show()
    }

    private fun checkCountry(userCountry: String, target: LocationData): Boolean {
        val normalizedUser = userCountry.lowercase().trim()
        val normalizedTarget = target.country.lowercase().trim()

        if (normalizedUser == normalizedTarget) return true

        for (altName in target.altNames) {
            if (normalizedUser == altName.lowercase().trim()) return true
        }

        if (normalizedTarget.contains(normalizedUser) || normalizedUser.contains(normalizedTarget)) {
            return true
        }

        return false
    }

    private fun loadRound(iv: ImageView) {
        val loc = gameLocations[currentRound]
        Glide.with(this)
            .load(loc.imageUrl)
            .placeholder(android.R.drawable.ic_dialog_info)
            .error(android.R.drawable.ic_dialog_alert)
            .into(iv)
    }

    private fun updateRoundDisplay(tvRound: TextView) {
        tvRound.text = getString(R.string.round_format, currentRound + 1, gameLocations.size)
    }

    private fun showGameOver(tvResult: TextView, tvRoundScore: TextView) {
        tvResult.text = getString(R.string.game_over)
        val maxScore = gameLocations.size * 1000
        tvRoundScore.text = getString(R.string.final_score_format, score, maxScore)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}