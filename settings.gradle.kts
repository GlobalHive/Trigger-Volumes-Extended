import dev.scaffoldit.hytale.Patchline

rootProject.name = "trigger-camera-extended"

plugins {
    id("dev.scaffoldit") version "0.2.+"
}

hytale {
    usePatchline(Patchline.PRE_RELEASE.name)
}