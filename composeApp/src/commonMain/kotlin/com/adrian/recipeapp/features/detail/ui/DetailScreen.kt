package com.adrian.recipeapp.features.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.adrian.recipeapp.features.common.data.models.capitalizeFirstWord
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailRoute(
    recipeId: Long,
    onBackClick: () -> Unit,
    detailViewModel: RecipeDetailViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        detailViewModel.getRecipeDetail(id = recipeId)
    }
    val detailUiState = detailViewModel.detailUiState.collectAsState()
    val favUiState = detailViewModel.updateFavUiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val onWatchVideoClick: (String) -> Unit = { link ->
        if (link.isNotEmpty()) {
            uriHandler.openUri(link)
        }
    }
    val onFavClick: (RecipeItem) -> Unit = {
        detailViewModel.updateFavourite(it.id, !it.isFavorite)
    }

    DetailScreen(
        detailUiState.value,
        favUiState.value,
        onBackClick = onBackClick,
        onWatchVideoClick = onWatchVideoClick,
        onFavClick = onFavClick
    )
}

@Composable
fun DetailScreen(
    uiState: RecipeDetailUiState,
    favUiState: RecipeDetailUpdateFavUiState,
    onBackClick: () -> Unit,
    onWatchVideoClick: (String) -> Unit,
    onFavClick: (RecipeItem) -> Unit
) {
    Column(
        modifier =
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.recipesDetailIsLoading -> {
                LoadingScreen()
            }

            uiState.recipesDetailError != null -> {
                ErrorScreen(uiState.recipesDetailError, onBackClick = onBackClick)
            }

            uiState.recipeDetail != null -> {
                RecipeDetailContent(
                    uiState.recipeDetail,
                    onBackClick = onBackClick,
                    onWatchVideoClick = onWatchVideoClick,
                    onFavClick = onFavClick
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(errorMsg: String, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipeItem: RecipeItem,
    onBackClick: () -> Unit,
    onWatchVideoClick: (String) -> Unit,
    onFavClick: (RecipeItem) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        RecipeMainContent(
            recipeItem = recipeItem,
            onWatchVideoClick = onWatchVideoClick
        )

        // Back and Save Button UI
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
            Modifier.fillMaxWidth().padding(WindowInsets.statusBars.asPaddingValues())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                /*.padding(
                    vertical = 32.dp,
                    horizontal = 16.dp
                )*/
                .align(
                    Alignment.TopCenter
                )
        ) {
            IconButton(
                onClick = onBackClick,
                modifier =
                Modifier.padding(horizontal = 8.dp).size(30.dp)
                    .background(
                        color =
                        MaterialTheme.colorScheme.background.copy(
                            alpha = 0.8f
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = {
                    onFavClick(recipeItem)
                },
                modifier =
                Modifier.padding(horizontal = 8.dp).size(30.dp)
                    .background(
                        color =
                        MaterialTheme.colorScheme.background.copy(
                            alpha = 0.8f
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector =
                    if (recipeItem.isFavorite) {
                        Icons.Default.Bookmark
                    } else {
                        Icons.Default.BookmarkBorder
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun RecipeMainContent(recipeItem: RecipeItem, onWatchVideoClick: (String) -> Unit) {
    Column(
        modifier =
        Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = recipeItem.imageUrl,
            contentDescription = recipeItem.title,
            contentScale = ContentScale.Crop,
            modifier =
            Modifier.fillMaxWidth().height(250.dp).clip(
                RoundedCornerShape(
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            )
        )

        // Other Details
        RecipeDetails(recipeItem = recipeItem)

        // Desc
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
        ) {
            Text(
                "Description",
                style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                recipeItem.description,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Ingredients items
        IngredientsList(
            ingredients =
            recipeItem.ingredients.map {
                val item = it.split(":")
                if (item.isNotEmpty() && item.size == 2) {
                    Pair(item[0].trim().capitalizeFirstWord(), item[1].trim())
                } else {
                    Pair("", "")
                }
            }.filter {
                it.first.isNotEmpty() && it.second.isNotEmpty()
            }.filterNot {
                it.first.contains("null") || it.second.contains("null")
            }
        )

        // Instructions
        Instructions(instructions = recipeItem.instructions)

        WatchVideoBtn(
            youTubeLink = recipeItem.youtubeLink,
            onWatchVideoClick = onWatchVideoClick
        )
    }
}

@Composable
fun RecipeDetails(recipeItem: RecipeItem) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = recipeItem.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = recipeItem.duration,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint =
                MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                )
            )
            Text(
                text = "${recipeItem.rating} stars",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = recipeItem.difficulty,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun IngredientsList(ingredients: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            "Ingredients",
            style =
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        ingredients.forEach {
            IngredientsItem(
                name = it.first,
                quantity = it.second
            )
        }
    }
}

@Composable
fun IngredientsItem(name: String, quantity: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, style = MaterialTheme.typography.bodySmall)
        Text(text = quantity, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun Instructions(instructions: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Instructions",
            style =
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        instructions.forEachIndexed { index, value ->
            Text(
                text = "${index + 1} $value",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun WatchVideoBtn(youTubeLink: String, onWatchVideoClick: (String) -> Unit) {
    Button(
        onClick = {
            onWatchVideoClick(youTubeLink)
        },
        colors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Watch",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            "Watch Video",
            style =
            MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}
