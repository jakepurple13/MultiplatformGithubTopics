package com.example.common.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.common.ChipLayout
import com.example.common.GitHubTopic
import com.example.common.LocalAppActions
import com.example.common.viewmodels.FavoritesVM
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(
    item: GitHubTopic,
    favoritesVM: FavoritesVM,
    savedTopics: List<String>,
    currentTopics: List<String>,
    onCardClick: (GitHubTopic) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = LocalAppActions.current
    OutlinedCard(
        onClick = { onCardClick(item) },
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            ListItem(
                headlineContent = { Text(item.name) },
                overlineContent = {
                    Text(
                        item.fullName,
                        textDecoration = TextDecoration.Underline
                    )
                },
                supportingContent = { item.description?.let { Text(it) } },
                leadingContent = {
                    Surface(shape = CircleShape) {
                        KamelImage(
                            lazyPainterResource(item.owner.avatarUrl.orEmpty()),
                            modifier = Modifier.size(48.dp),
                            contentDescription = null,
                            animationSpec = tween()
                        )
                    }
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconsButton(onClick = { actions.onShareClick(item) }, icon = Icons.Default.Share)

                        val isFavorite by remember {
                            derivedStateOf { favoritesVM.items.any { it.htmlUrl == item.htmlUrl } }
                        }

                        IconsButton(
                            onClick = {
                                if (isFavorite) favoritesVM.removeFavorite(item)
                                else favoritesVM.addFavorite(item)
                            },
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Text(item.stars.toString())
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForkLeft, contentDescription = null)
                            Text(item.forks.toString())
                        }
                    }
                }
            )

            ChipLayout(modifier = Modifier.padding(4.dp)) {
                item.topics.forEach {
                    AssistChip(
                        label = { Text(it) },
                        modifier = Modifier.padding(2.dp),
                        onClick = { onTopicClick(it) },
                        leadingIcon = if (it in currentTopics) {
                            { Icon(Icons.Default.CatchingPokemon, null, modifier = Modifier.rotate(180f)) }
                        } else null,
                        border = AssistChipDefaults.assistChipBorder(
                            borderColor = when (it) {
                                in savedTopics -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    )
                }
            }

            item.license?.let {
                Text(
                    it.name,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Row {
                Text(
                    text = item.pushedAt,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )

                Text(
                    text = item.language,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )
            }
        }
    }
}