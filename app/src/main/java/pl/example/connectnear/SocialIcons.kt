package pl.example.connectnear

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SocialIcons {
    val Facebook: ImageVector
        get() {
            if (_facebook != null) return _facebook!!
            _facebook = ImageVector.Builder(
                name = "Facebook",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color(0xFF1877F2))) {
                    moveTo(12f, 2.04f)
                    curveTo(6.5f, 2.04f, 2f, 6.53f, 2f, 12.06f)
                    curveTo(2f, 17.06f, 5.66f, 21.21f, 10.44f, 21.96f)
                    verticalLineTo(14.96f)
                    horizontalLineTo(7.9f)
                    verticalLineTo(12.06f)
                    horizontalLineTo(10.44f)
                    verticalLineTo(9.85f)
                    curveTo(10.44f, 7.34f, 11.96f, 5.97f, 14.13f, 5.97f)
                    curveTo(15.17f, 5.97f, 16.26f, 6.15f, 16.26f, 6.15f)
                    verticalLineTo(8.61f)
                    horizontalLineTo(15.06f)
                    curveTo(13.82f, 8.61f, 13.46f, 9.38f, 13.46f, 10.17f)
                    verticalLineTo(12.06f)
                    horizontalLineTo(16.24f)
                    lineTo(15.8f, 14.96f)
                    horizontalLineTo(13.46f)
                    verticalLineTo(21.96f)
                    curveTo(18.24f, 21.21f, 22f, 17.06f, 22f, 12.06f)
                    curveTo(22f, 6.53f, 17.5f, 2.04f, 12f, 2.04f)
                    close()
                }
            }.build()
            return _facebook!!
        }
    private var _facebook: ImageVector? = null

    val Instagram: ImageVector
        get() {
            if (_instagram != null) return _instagram!!
            _instagram = ImageVector.Builder(
                name = "Instagram",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) { // Color handled by caller or gradient
                    moveTo(7.8f, 2f)
                    horizontalLineTo(16.2f)
                    curveTo(19.4f, 2f, 22f, 4.6f, 22f, 7.8f)
                    verticalLineTo(16.2f)
                    curveTo(22f, 19.4f, 19.4f, 22f, 16.2f, 22f)
                    horizontalLineTo(7.8f)
                    curveTo(4.6f, 22f, 2f, 19.4f, 2f, 16.2f)
                    verticalLineTo(7.8f)
                    curveTo(2f, 4.6f, 4.6f, 2f, 7.8f, 2f)
                    close()
                    moveTo(7.6f, 4f)
                    curveTo(5.61f, 4f, 4f, 5.61f, 4f, 7.6f)
                    verticalLineTo(16.4f)
                    curveTo(4f, 18.39f, 5.61f, 20f, 7.6f, 20f)
                    horizontalLineTo(16.4f)
                    curveTo(18.39f, 20f, 20f, 18.39f, 20f, 16.4f)
                    verticalLineTo(7.6f)
                    curveTo(20f, 5.61f, 18.39f, 4f, 16.4f, 4f)
                    horizontalLineTo(7.6f)
                    close()
                    moveTo(12f, 7f)
                    curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                    curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                    curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                    curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                    close()
                    moveTo(12f, 9f)
                    curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                    curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                    curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                    curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                    close()
                    moveTo(16.5f, 6.3f)
                    curveTo(16.5f, 7.13f, 15.83f, 7.8f, 15f, 7.8f)
                    curveTo(14.17f, 7.8f, 13.5f, 7.13f, 13.5f, 6.3f)
                    curveTo(13.5f, 5.47f, 14.17f, 4.8f, 15f, 4.8f)
                    curveTo(15.83f, 4.8f, 16.5f, 5.47f, 16.5f, 6.3f)
                    close()
                }
            }.build()
            return _instagram!!
        }
    private var _instagram: ImageVector? = null

    val TikTok: ImageVector
        get() {
            if (_tiktok != null) return _tiktok!!
            _tiktok = ImageVector.Builder(
                name = "TikTok",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(19.59f, 6.69f)
                    curveTo(18.07f, 5.61f, 16.94f, 3.93f, 16.57f, 2f)
                    horizontalLineTo(13.12f)
                    verticalLineTo(15.67f)
                    curveTo(13.12f, 17.52f, 11.62f, 19.02f, 9.77f, 19.02f)
                    curveTo(7.92f, 19.02f, 6.42f, 17.52f, 6.42f, 15.67f)
                    curveTo(6.42f, 13.82f, 7.92f, 12.32f, 9.77f, 12.32f)
                    curveTo(10.06f, 12.32f, 10.34f, 12.36f, 10.61f, 12.44f)
                    verticalLineTo(8.94f)
                    curveTo(10.34f, 8.9f, 10.06f, 8.88f, 9.77f, 8.88f)
                    curveTo(6.02f, 8.88f, 2.98f, 11.92f, 2.98f, 15.67f)
                    curveTo(2.98f, 19.42f, 6.02f, 22.46f, 9.77f, 22.46f)
                    curveTo(13.52f, 22.46f, 16.56f, 19.42f, 16.56f, 15.67f)
                    verticalLineTo(8.67f)
                    curveTo(18.21f, 9.84f, 20.21f, 10.64f, 22.4f, 10.92f)
                    verticalLineTo(7.42f)
                    curveTo(21.41f, 7.33f, 20.46f, 7.08f, 19.59f, 6.69f)
                    close()
                }
            }.build()
            return _tiktok!!
        }
    private var _tiktok: ImageVector? = null

    val Messenger: ImageVector
        get() {
            if (_messenger != null) return _messenger!!
            _messenger = ImageVector.Builder(
                name = "Messenger",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color(0xFF0084FF))) {
                    moveTo(12f, 2f)
                    curveTo(6.48f, 2f, 2f, 6.03f, 2f, 11.05f)
                    curveTo(2f, 13.88f, 3.39f, 16.39f, 5.55f, 18f)
                    lineTo(5f, 22f)
                    lineTo(8.62f, 20.09f)
                    curveTo(9.67f, 20.37f, 10.79f, 20.53f, 12f, 20.53f)
                    curveTo(17.52f, 20.53f, 22f, 16.49f, 22f, 11.05f)
                    curveTo(22f, 6.03f, 17.52f, 2f, 12f, 2f)
                    close()
                    moveTo(13f, 16f)
                    lineTo(10.5f, 13.5f)
                    lineTo(6f, 16f)
                    lineTo(11f, 10f)
                    lineTo(13.5f, 12.5f)
                    lineTo(18f, 10f)
                    lineTo(13f, 16f)
                    close()
                }
            }.build()
            return _messenger!!
        }
    private var _messenger: ImageVector? = null
}
