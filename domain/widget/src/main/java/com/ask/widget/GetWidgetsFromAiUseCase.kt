package com.ask.widget

import com.ask.core.DISPATCHER_DEFAULT
import com.ask.user.User
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Named

class GetWidgetsFromAiUseCase @Inject constructor(
    private val generativeModel: GenerativeModel,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        totalQuestions: Int,
        categories: List<String>,
        myWords: String? = null
    ) =
        withContext(dispatcher) {
            val prompt =
                "provide latest and fresh $totalQuestions ${Widget.WidgetType.entries.joinToString("/")} with options (min 2 and max 4 and max 5 words each option) based on latest ${
                    myWords ?: categories.joinToString(",")
                } in json format like " +
                    "[ " +
                    "{ \"widgetType\": <${Widget.WidgetType.entries.joinToString("|")}> , \"category\": <category> , \"question\" : <your question>, \"options\" : [ { \"text\" : <your option>, isCorrect: <true,false> (add this only if widgetType is ${Widget.WidgetType.Quiz.name}) }, { \"text\" : <your option> } ] }, " +
                    "]" +
                    " only"
            val response = generativeModel.generateContent(prompt)
            response.text?.let { res ->
                println("res = [${res}]")
                val jsonElement =
                    Json.parseToJsonElement(res.replace("```json", "").replace("```", ""))
                val jsonArray = jsonElement.jsonArray
                jsonArray.map { json ->
                    val widgetType =
                        Widget.WidgetType.valueOf(json.jsonObject["widgetType"]!!.jsonPrimitive.content)
                    val question = json.jsonObject["question"]!!.jsonPrimitive.content
                    val category = json.jsonObject["category"]!!.jsonPrimitive.content
                    val options = json.jsonObject["options"]!!.jsonArray.map {
                        val text = it.jsonObject["text"]!!.jsonPrimitive.content
//                    val isCorrect = it.jsonObject["isCorrect"]!!.jsonPrimitive.boolean
                        WidgetWithOptionsAndVotesForTargetAudience.OptionWithVotes(
                            option = Widget.Option(text = text),
                            votes = emptyList()
                        )
                    }
                    val widget = Widget(title = question)
                    WidgetWithOptionsAndVotesForTargetAudience(
                        widget = widget,
                        options = options,
                        user = User(),
                        isBookmarked = false,
                        categories = listOf(
                            Widget.WidgetCategory(
                                category = category,
                                widgetId = widget.id
                            )
                        ),
                        targetAudienceGender = Widget.TargetAudienceGender(),
                        targetAudienceAgeRange = Widget.TargetAudienceAgeRange(),
                        targetAudienceLocations = emptyList()
                    )
                }
            } ?: emptyList()
        }

}