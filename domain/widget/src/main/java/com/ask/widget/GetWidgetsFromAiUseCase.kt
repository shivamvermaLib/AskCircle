package com.ask.widget

import com.ask.core.AppSharedPreference
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
    private val appSharedPreference: AppSharedPreference,
    @Named(DISPATCHER_DEFAULT) private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        totalQuestions: Int,
        myWords: String
    ) =
        withContext(dispatcher) {
            val prompt =
                "Generate a set of random $totalQuestions polls with options based on the latest ${myWords}. Each poll should have a minimum of 2 and a maximum of 4 options. The response should be in the following JSON format:\n" +
                    "\n" +
                    "[\n" +
                    "  { \n" +
                    "    \"widgetType\": \"${Widget.WidgetType.Poll.name}\",\n" +
                    "    \"category\": \"poll category\",\n" +
                    "    \"title\": \"poll title\",\n" +
                    "    \"options\": [\n" +
                    "      { \"text\": \"poll option\" },\n" +
                    "      { \"text\": \"poll option\" },\n" +
                    "      { \"text\": \"poll option\" }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]"
            val response = generativeModel.generateContent(prompt)
            response.text?.let { res ->
                println("res = [${res}]")
                val jsonElement =
                    Json.parseToJsonElement(res.replace("```json", "").replace("```", ""))
                val jsonArray = jsonElement.jsonArray
                jsonArray.map { json ->
//                    val widgetType =
//                        Widget.WidgetType.valueOf(json.jsonObject["widgetType"]!!.jsonPrimitive.content)
                    val question = json.jsonObject["title"]!!.jsonPrimitive.content
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
                        targetAudienceLocations = emptyList(),
                        comments = emptyList()
                    )
                }
            }.also {
                myWords.let { it1 -> appSharedPreference.setAiSearchPrompt(it1) }
            } ?: emptyList()
        }

}