package com.rafael.inclusimap.feature.map.map.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rafael.inclusimap.core.ui.components.OverlayText
import com.rafael.inclusimap.feature.map.map.domain.model.RevealKeys
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.revealable
import com.svenjacobs.reveal.shapes.balloon.Arrow

@Composable
fun BoxScope.AddPlaceRevelation(
    revealState: RevealState,
    modifier: Modifier = Modifier,
) {
    Revelation(
        revealState,
        RevealKeys.ADD_PLACE_TIP,
        Alignment.TopEnd,
        RevealShape.Circle,
        modifier
            .navigationBarsPadding()
            .padding(top = 200.dp, end = 90.dp)
            .size(50.dp)
            .clip(CircleShape),
    )
}

@Composable
fun BoxScope.PlaceDetailsRevelation(
    revealState: RevealState,
    modifier: Modifier = Modifier,
) {
    Revelation(
        revealState,
        RevealKeys.PLACE_DETAILS_TIP,
        Alignment.Center,
        RevealShape.RoundRect(16.dp),
        modifier
            .padding(bottom = 70.dp)
            .fillMaxWidth(0.55f)
            .height(200.dp),
    )
}

@Composable
fun RevealOverlayScope.OverlayContent(
    key: Any,
    modifier: Modifier = Modifier,
) {
    when (key) {
        RevealKeys.ADD_PLACE_TIP -> OverlayText(
            text = "Clique e segure para\nadicionar novos locais",
            arrow = Arrow.bottom(),
            modifier = modifier
                .align(verticalArrangement = RevealOverlayArrangement.Top),
        )

        RevealKeys.PLACE_DETAILS_TIP -> OverlayText(
            text = "Clique no marcador para\nver os detalhes do local",
            arrow = Arrow.bottom(),
            modifier = Modifier
                .align(verticalArrangement = RevealOverlayArrangement.Top),
        )
    }
}

@Composable
fun BoxScope.Revelation(
    revealState: RevealState,
    key: RevealKeys,
    alignment: Alignment,
    shape: RevealShape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .align(alignment)
            .statusBarsPadding()
            .revealable(
                key,
                revealState,
                shape,
            ),
    )
}
