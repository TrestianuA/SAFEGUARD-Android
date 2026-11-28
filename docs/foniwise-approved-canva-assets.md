# FoniWise — approved Canva icon asset mapping

Approved design: https://www.canva.com/d/CYPr0u4tJa3JEzQ
Branch: test/foniwise-073-integration

| Android drawable name | Canva media asset ID | Intended location | Source dimensions |
| --- | --- | --- | --- |
| foniwise_icon_security | MAHbodpO3eg | Quick access / Security scan | 99×100 |
| foniwise_icon_notifications | MAHboWZLpfg | Quick access / Notification inspector | 99×100 |
| foniwise_icon_battery | MAHbocgJ1pk | Quick access / Battery | 99×99 |
| foniwise_icon_apps | MAHbobKJqsw | Quick access / Apps | 99×100 |
| foniwise_icon_cleaning | MAHboUVFL-Q | Quick access / Manual cleaning | 99×100 |
| foniwise_nav_phone | MAHboeCfTg4 | Bottom navigation / My phone | 60×79 |
| foniwise_nav_learn | MAHboWmsbiQ | Bottom navigation / Learn | 80×60 |
| foniwise_nav_recommendations | MAHbod9XQOE | Bottom navigation / Recommendations | 79×60 |

## Integration gate

Export these **eight individual source images** from the approved Canva design as PNGs with transparency, preserving their exact artwork. Copy them into `app/src/main/res/drawable-nodpi/` using the drawable names above and `.png` extensions. Then replace the Material icon references in `MainActivity.kt` with `painterResource(R.drawable.<name>)` in the five quick-access cards and three navigation tabs. Preserve icon aspect ratios and colors; do not tint source bitmaps.

**Current status:** IDs verified in Canva; the signed thumbnail URLs cannot be retrieved reliably by the current build environment. These original PNG files have NOT yet been committed. Do not claim original icon integration or a successful APK build until the resources are present and build passes.

The original rounded brand logo is already referenced as `R.drawable.foniwise_symbol_original_approved`.
