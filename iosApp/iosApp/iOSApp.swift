import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinIOSKt.doInitKoinIOS()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}