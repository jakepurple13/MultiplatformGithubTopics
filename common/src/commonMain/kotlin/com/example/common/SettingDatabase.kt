package com.example.common

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.filterNotNull

class SettingInformation : RealmObject {
    var currentTopics: RealmList<String> = realmListOf()
    var topicList: RealmList<String> = realmListOf()
    var theme: Int = ThemeColors.Default.ordinal
    var isDarkMode: Boolean = true
}

class Database {
    val realm by lazy { Realm.open(RealmConfiguration.create(setOf(SettingInformation::class))) }

    val settingsInfo
        get() = realm.query<SettingInformation>(SettingInformation::class)
            .first()
            .asFlow()
            .filterNotNull()

    suspend fun addCurrentTopic(topic: String) {
        if (topic.isNotEmpty()) {
            updateInfo { it?.currentTopics?.add(topic) }
        }
    }

    suspend fun removeCurrentTopic(topic: String) {
        updateInfo { it?.currentTopics?.remove(topic) }
    }

    suspend fun addTopic(topic: String) {
        if (topic.isNotEmpty()) {
            updateInfo { it?.topicList?.add(topic) }
        }
    }

    suspend fun removeTopic(topic: String) {
        updateInfo { it?.topicList?.remove(topic) }
    }

    suspend fun changeTheme(colors: ThemeColors) {
        updateInfo { it?.theme = colors.ordinal }
    }

    suspend fun changeMode(isDarkMode: Boolean) {
        updateInfo { it?.isDarkMode = isDarkMode }
    }

    private suspend fun updateInfo(block: MutableRealm.(SettingInformation?) -> Unit) {
        realm.query<SettingInformation>(SettingInformation::class).first().find()?.also { info ->
            realm.write { block(findLatest(info)) }
        }
    }
}